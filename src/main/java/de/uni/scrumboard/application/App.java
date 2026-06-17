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
import de.uni.scrumboard.data.ResetTokenRepository;
import de.uni.scrumboard.data.ResetTokenRepositoryImpl;
import de.uni.scrumboard.data.TaskRepository;
import de.uni.scrumboard.data.UserStoryRepository;
import de.uni.scrumboard.data.interfaces.IUserStoryRepository;
import de.uni.scrumboard.presentation.AuthController;
import de.uni.scrumboard.presentation.BacklogController;
import de.uni.scrumboard.presentation.HomeController;
import de.uni.scrumboard.presentation.ProfileController;
import de.uni.scrumboard.presentation.TaskController;
import de.uni.scrumboard.presentation.UserStoryController;
import de.uni.scrumboard.presentation.interfaces.IUserStoryController;
import de.uni.scrumboard.service.AccountService;
import de.uni.scrumboard.service.BacklogService;
import de.uni.scrumboard.service.PasswordResetService;
import de.uni.scrumboard.service.ProfileService;
import de.uni.scrumboard.service.TaskService;
import de.uni.scrumboard.service.UserStoryService;
import de.uni.scrumboard.service.interfaces.IUserStoryService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsConnectContext;

public class App implements IApp {

    private static ProfileController profileController;
    private static AuthController authController;
    private static HomeController homeController;
    private static TaskController taskController;
    private static BacklogController backlogController;
    private static IUserStoryController userStoryController;

    private static ProfileService profileService;
    private static AccountService accountService;
    private static TaskService taskService;
    private static BacklogService backlogService;
    private static IUserStoryService userStoryService;

    private static ResetTokenRepository resetTokenRepository;
    private static PasswordResetService passwordResetService;

    private static ProfileRepository profileRepository;
    private static AccountRepository accountRepository;
    private static TaskRepository taskRepository;
    private static BacklogRepository backlogRepository;
    private static IUserStoryRepository userStoryRepository;

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

                // HTTP HANDLEERS

                .get("/forgot-password", authController::getForgotPassword)
                .post("/forgot-password", authController::postForgotPassword)
                .get("/reset-password", authController::getResetPassword)
                .post("/reset-password", authController::postResetPassword)

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

                .get("/backlogs", backlogController::getBacklogs)
                .get("/backlog/{id}", backlogController::getBacklog)
                .get("/backlog/{id}/last-update", backlogController::getLastUpdate)

                .get("/backlog/{id}/assignees", backlogController::getBacklogAssignees)
                .get("/backlog/{id}/assignees/add", backlogController::getAddBacklogAssignee)
                .post("/backlog/{id}/assignees/add", backlogController::postAddBacklogAssignee)
                .get("/backlog/{id}/assignees/remove/{assigneeId}", backlogController::postRemoveBacklogAssignee)   

                
                .get("/create-backlog", backlogController::getCreateBacklog)
                .post("/create-backlog", backlogController::postCreateBacklog)

                .get("/edit-task/{taskId}", taskController::getEditTask)
                .post("/edit-task/{taskId}", taskController::postEditTask)

                .get("/create-user-story", userStoryController::getCreateUserStory)
                .post("/create-user-story", userStoryController::postCreateUserStory)
                .get("/edit-user-story/{id}", userStoryController::getEditUserStory)
                .post("/edit-user-story/{id}", userStoryController::postEditUserStory)
                .get("/search-tasks", taskController::SearchTask)

                .get("/showmessage", taskController::showMessage)

                // WEBSOCKET
                .ws("*", (WsConfig t) -> {
                    t.onConnect(this::connectHandler);
                })

                .start(port);
    }

    private void connectHandler(WsConnectContext ctx) {
        System.out.println("Hallo!");
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
            userStoryRepository = new UserStoryRepository(databaseConnection);

            profileService = new ProfileService(profileRepository);
            accountService = new AccountService(accountRepository);
            taskService = new TaskService(taskRepository, accountRepository);
            backlogService = new BacklogService(backlogRepository);
            userStoryService = new UserStoryService(userStoryRepository);

            resetTokenRepository = new ResetTokenRepositoryImpl(databaseConnection);
            passwordResetService = new PasswordResetService(accountRepository, resetTokenRepository);

            profileController = new ProfileController(profileService);
            
            // ========== HIER: DEINE VERSION MIT passwordResetService BEHALTEN ==========
            authController = new AuthController(accountService, profileService, passwordResetService);
            
            // ========== HIER: DIE NEUEN VERSIONEN VON main BEHALTEN ==========
            homeController = new HomeController(backlogService);
            taskController = new TaskController(taskService, accountService, backlogService);
            backlogController = new BacklogController(backlogService, userStoryService, taskService);
            userStoryController = new UserStoryController(userStoryService, backlogService, taskService);
            
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
                            "priorities INTEGER," +
                            "lastUpdated INTEGER NOT NULL," +
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

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_stories (
                        id INTEGER PRIMARY KEY,
                        titel VARCHAR(255) NOT NULL,
                        beschreibung TEXT,
                        creatorId INTEGER NOT NULL,
                        backlogId INTEGER NOT NULL,
                        status INTEGER NOT NULL DEFAULT 0,
                        priorities INTEGER,
                        lastUpdated INTEGER NOT NULL,
                        FOREIGN KEY(creatorId) REFERENCES accounts(accountId),
                        FOREIGN KEY(backlogId) REFERENCES backlogs(id)
                    );""");

        
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS password_reset_tokens (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_id INTEGER NOT NULL,
                    token VARCHAR(255) NOT NULL UNIQUE,
                    expiry_date TIMESTAMP NOT NULL,
                    used BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(account_id) REFERENCES accounts(accountId)
                );
            """);

            statement.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_reset_token ON password_reset_tokens(token);
            """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS backlog_assignees (
                        backlogId INTEGER NOT NULL,
                        assigneeid INTEGER NOT NULL,
                        roles VARCHAR(255) NOT NULL, 
                        FOREIGN KEY (assigneeid) references accounts(accountId),
                        FOREIGN KEY(backlogId) REFERENCES backlogs(id)
            );""");
           
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_story_tasks (
                        userStoryId INTEGER NOT NULL,
                        taskId INTEGER NOT NULL,
                        PRIMARY KEY(userStoryId, taskId),
                        FOREIGN KEY(userStoryId) REFERENCES user_stories(id) ON DELETE CASCADE,
                        FOREIGN KEY(taskId) REFERENCES tasks(taskId) ON DELETE CASCADE
                    );""");

            try {
                statement.executeUpdate("ALTER TABLE user_stories ADD COLUMN status INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Erzeugen der Datenbankschemata", e);
        }
    }

}
