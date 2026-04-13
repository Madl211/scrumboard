package de.uni.scrumboard.application;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import de.uni.scrumboard.application.interfaces.IApp;
import de.uni.scrumboard.data.AccountRepository;
import de.uni.scrumboard.data.ProfileRepository;
import de.uni.scrumboard.presentation.AuthController;
import de.uni.scrumboard.presentation.HomeController;
import de.uni.scrumboard.presentation.ProfileController;
import de.uni.scrumboard.service.AccountService;
import de.uni.scrumboard.service.ProfileService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;

public class App implements IApp {

    private static ProfileController profileController;
    private static AuthController authController;
    private static HomeController homeController;
    private static ProfileService profileService;
    private static AccountService accountService;
    private static ProfileRepository profileRepository;
    private static AccountRepository accountRepository;

    @Override
    public void run(int port) {
        bootstrapApplication();

        // TODO /robots.txt, /sitemap.xml
        Javalin.create(config -> config.fileRenderer(new JavalinMustache()))
                .get("/", ctx -> {
                    Integer accountId = ctx.sessionAttribute("accountId");
                    if (accountId == null) {
                        ctx.redirect("/login");
                    } else {
                        ctx.redirect("/home");
                    }
                })
                .get("/login", authController::getLogin)
                .post("/login", authController::postLogin)
                .get("/register", authController::getRegister)
                .post("/register", authController::postRegister)
                .get("/logout", authController::logout)
                .get("/profile", profileController::getProfile)
                .post("/profile", profileController::saveProfile)
                .get("/home", homeController::showHome)
                .start(port);
    }

    private static void bootstrapApplication() {
        Properties applicationProperties = new Properties();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream propertiesInputStream = classLoader.getResourceAsStream("application.properties");

            applicationProperties.load(propertiesInputStream);

            Connection databaseConnection = DriverManager
                    .getConnection(applicationProperties.getProperty("database.url"));

            createDatabaseSchema(databaseConnection);

            profileRepository = new ProfileRepository(databaseConnection);
            accountRepository = new AccountRepository(databaseConnection);

            profileService = new ProfileService(profileRepository);
            accountService = new AccountService(accountRepository);

            profileController = new ProfileController(profileService);
            authController = new AuthController(accountService, profileService);
            homeController = new HomeController();
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Konfiguration:");
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Fehler beim Aufbauen der Datenbankverbindung:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void createDatabaseSchema(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS accounts (" +
                            "accountId INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "email TEXT NOT NULL UNIQUE," +
                            "passwordHash TEXT NOT NULL," +
                            "username TEXT" +
                            ")");

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS profiles (" +
                            "accountId INTEGER PRIMARY KEY," +
                            "name TEXT," +
                            "eMail TEXT," +
                            "telefonnummer TEXT," +
                            "postleitzahl TEXT," +
                            "ort TEXT," +
                            "strasse TEXT," +
                            "hausnummer TEXT," +
                            "FOREIGN KEY(accountId) REFERENCES accounts(accountId) ON DELETE CASCADE" +
                            ")");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Erzeugen der Datenbankschemata", e);
        }
    }

}
