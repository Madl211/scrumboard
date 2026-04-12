package de.uni.scrumboard;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import de.uni.scrumboard.data.ProfileRepository;
import de.uni.scrumboard.presentation.ProfileController;
import de.uni.scrumboard.service.ProfileService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;

public class App {

    private static ProfileController profileController;
    private static ProfileService profileService;
    private static ProfileRepository profileRepository;

    public static void main(String[] args) {
        bootstrapApplication();

        Javalin.create(config -> config.fileRenderer(new JavalinMustache()))
            .get("/profile", profileController::getProfile)
        .start(8000);
    }

    private static void bootstrapApplication() {
        Properties applicationProperties = new Properties();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream propertiesInputStream = classLoader.getResourceAsStream("application.properties");

            applicationProperties.load(propertiesInputStream);
            
            Connection databaseConnection = DriverManager.getConnection(applicationProperties.getProperty("database.url"));
            
            profileRepository = new ProfileRepository(databaseConnection);
            profileService = new ProfileService(profileRepository);
            profileController = new ProfileController(profileService);
        } catch(IOException e) {
            System.err.println("Fehler beim Laden der Konfiguration:");
            e.printStackTrace();
            System.exit(1);
        } catch(SQLException e) {
            System.err.println("Fehler beim Aufbauen der Datenbankverbindung:");
            e.printStackTrace();
            System.exit(1);
        }        
    }

}
