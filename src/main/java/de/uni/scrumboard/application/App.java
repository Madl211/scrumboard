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
import de.uni.scrumboard.data.BacklogRepository;
import de.uni.scrumboard.data.ProfileRepository;
import de.uni.scrumboard.data.TaskRepository;
import de.uni.scrumboard.presentation.AuthController;
import de.uni.scrumboard.presentation.BacklogController;
import de.uni.scrumboard.presentation.HomeController;
import de.uni.scrumboard.presentation.ProfileController;
import de.uni.scrumboard.presentation.TaskController;
import de.uni.scrumboard.service.AccountService;
import de.uni.scrumboard.service.BacklogService;
import de.uni.scrumboard.service.ProfileService;
import de.uni.scrumboard.service.TaskService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;

public class App implements IApp {

    private static ProfileController profileController;
    private static AuthController authController;
    private static HomeController homeController;
    private static TaskController taskController;
    private static BacklogController backlogController;
    private static ProfileService profileService;
    private static AccountService accountService;
    private static TaskService taskService;
    private static BacklogService backlogService;
    private static ProfileRepository profileRepository;
    private static AccountRepository accountRepository;
    private static TaskRepository taskRepository;
    private static BacklogRepository backlogRepository;

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
                .get("/create-task", taskController::getCreateTask)
                .post("/create-task", taskController::postCreateTask)
                .get("/create-backlog", backlogController::getCreateBacklog)
                .post("/create-backlog", backlogController::postCreateBacklog)
                .get("/edit-task/{taskId}", taskController::getEditTask)
                .post("/edit-task/{taskId}", taskController::postEditTask)
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
            taskRepository = new TaskRepository(databaseConnection);
            backlogRepository = new BacklogRepository(databaseConnection);

            profileService = new ProfileService(profileRepository);
            accountService = new AccountService(accountRepository);
            taskService = new TaskService(taskRepository, accountRepository);
            backlogService = new BacklogService(backlogRepository);

            profileController = new ProfileController(profileService);
            authController = new AuthController(accountService, profileService);
            homeController = new HomeController(accountService, taskService);
            taskController = new TaskController(taskService, accountService);
            backlogController = new BacklogController(backlogService);
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

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tasks (" +
                            "taskId INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT NOT NULL," +
                            "description TEXT," +
                            "creatorId INTEGER NOT NULL," +
                            "startDate DATE," +
                            "dueDate DATE," +
                            "createdDate DATE NOT NULL," +
                            "estimatedTime INTEGER," +
                            "actualTime INTEGER," +
                            "status INTEGER," +
                            "FOREIGN KEY(creatorId) REFERENCES accounts(accountId)" +
                            ")");

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS task_assignees (" +
                            "taskId INTEGER NOT NULL," +
                            "accountId INTEGER NOT NULL," +
                            "PRIMARY KEY(taskId, accountId)," +
                            "FOREIGN KEY(taskId) REFERENCES tasks(taskId) ON DELETE CASCADE," +
                            "FOREIGN KEY(accountId) REFERENCES accounts(accountId) ON DELETE CASCADE" +
                            ")");

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS backlogs (
                        id INTEGER PRIMARY KEY,
                        bezeichnung VARCHAR(255) UNIQUE NOT NULL
                    );""");

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS backlog_tasks (
                        backlogId INTEGER NOT NULL,
                        taskId INTEGER NOT NULL,
                        PRIMARY KEY(backlogId, taskId),
                        FOREIGN KEY(backlogId) REFERENCES backlogs(id),
                        FOREIGN KEY(taskId) REFERENCES tasks(taskId)
                    );""");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Erzeugen der Datenbankschemata", e);
        }
    }

}
