package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.data.interfaces.ITaskRepository;

public record TaskRepository(Connection connection) implements ITaskRepository {

    @Override
    public Optional<Task> getTaskById(int taskId) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "SELECT taskId, title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status, priorities, lastUpdated FROM tasks WHERE taskId = ?")) {
            sqlStatement.setInt(1, taskId);

            try (ResultSet result = sqlStatement.executeQuery()) {
                if (result.next()) {
                    int id = result.getInt("taskId");
                    String title = result.getString("title");
                    String desc = result.getString("description");
                    int creatorId = result.getInt("creatorId");
                    Date start = result.getDate("startDate");
                    Date due = result.getDate("dueDate");
                    Date created = result.getDate("createdDate");
                    int estimatedTime = result.getInt("estimatedTime");
                    int actualTime = result.getInt("actualTime");
                    int priorities = result.getInt("priorities");  
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                    long lastUpdated = result.getLong("lastUpdated");

                    Account creator = getAccountById(creatorId);
                    List<Account> assignees = getAssigneesForTask(taskId);

                    Task task = new Task(id, title, desc, assignees, creator, start, due, created, estimatedTime,
                            actualTime, status,priorities, lastUpdated);
                    return Optional.of(task);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden der Task mit ID " + taskId, e);
        }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "SELECT taskId, title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status, priorities, lastUpdated FROM tasks")) {

            try (ResultSet result = sqlStatement.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("taskId");
                    String title = result.getString("title");
                    String desc = result.getString("description");
                    int creatorId = result.getInt("creatorId");
                    Date start = result.getDate("startDate");
                    Date due = result.getDate("dueDate");
                    Date created = result.getDate("createdDate");
                    int estimatedTime = result.getInt("estimatedTime");
                    int actualTime = result.getInt("actualTime");
                    int priorities = result.getInt("priorities");  
                    TaskStatus status = TaskStatus.itoStatus(result.getInt(("status")));
                    long lastUpdated = result.getLong("lastUpdated");

                    Account creator = getAccountById(creatorId);
                    List<Account> assignees = getAssigneesForTask(id);

                    Task task = new Task(id, title, desc, assignees, creator, start, due, created, estimatedTime,
                            actualTime, status, priorities,  lastUpdated);
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden aller Tasks", e);
        }
        return tasks;
    }

    @Override
    public boolean saveTask(Task task, int taskId) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "INSERT INTO tasks(taskId, title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status, priorities, lastUpdated) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) "
                        + "ON CONFLICT(taskId) DO UPDATE SET "
                        + "title = excluded.title, "
                        + "description = excluded.description, "
                        + "startDate = excluded.startDate, "
                        + "dueDate = excluded.dueDate, "
                        + "estimatedTime = excluded.estimatedTime, "
                        + "actualTime = excluded.actualTime, "
                        + "status = excluded.status, "
                        + "priorities = excluded.priorities, " 
                        + "lastUpdated = excluded.lastUpdated")) {

            sqlStatement.setInt(1, taskId);
            sqlStatement.setString(2, task.title());
            sqlStatement.setString(3, task.desc());
            sqlStatement.setInt(4, task.creator().accountId());
            sqlStatement.setDate(5, task.start() != null ? new java.sql.Date(task.start().getTime()) : null);
            sqlStatement.setDate(6, task.due() != null ? new java.sql.Date(task.due().getTime()) : null);
            sqlStatement.setDate(7, task.created() != null ? new java.sql.Date(task.created().getTime()) : null);
            sqlStatement.setInt(8, task.estimatedTime());
            sqlStatement.setInt(9, task.actualTime());
            sqlStatement.setInt(10, task.status().val);
            sqlStatement.setInt(11, task.priorities());  
            sqlStatement.setLong(12, Instant.now().getEpochSecond());

            sqlStatement.executeUpdate();

            // Save assignees
            saveAssigneesForTask(taskId, task.assignees());

            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Speichern der Task mit ID " + taskId, e);
        }
    }

    @Override
    public Task createTask(int taskId, String title, String desc, List<Account> assignees, Account creator, Date start,
            Date due, Date created, int estimatedTime, int actualTime, TaskStatus status) {

        long timestamp_of_now = Instant.now().getEpochSecond();

        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "INSERT INTO tasks(title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status, priorities, lastUpdated) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            sqlStatement.setString(1, title);
            sqlStatement.setString(2, desc);
            sqlStatement.setInt(3, creator.accountId());
            sqlStatement.setDate(4, start != null ? new java.sql.Date(start.getTime()) : null);
            sqlStatement.setDate(5, due != null ? new java.sql.Date(due.getTime()) : null);
            sqlStatement.setDate(6, created != null ? new java.sql.Date(created.getTime()) : null);
            sqlStatement.setInt(7, estimatedTime);
            sqlStatement.setInt(8, actualTime);
            sqlStatement.setInt(9, status.val);
            sqlStatement.setInt(10, 0); 
            sqlStatement.setLong(11, timestamp_of_now);

            int affectedRows = sqlStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Konnte Task nicht anlegen");
            }

            try (ResultSet generatedKeys = sqlStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedTaskId = generatedKeys.getInt(1);
                    Task task = new Task(generatedTaskId, title, desc, assignees, creator, start, due, created,
                            estimatedTime, actualTime, status, 0, timestamp_of_now);
                    saveAssigneesForTask(generatedTaskId, assignees);
                    return task;
                }
            }

            throw new RuntimeException("Konnte generierte Task-ID nicht lesen");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Anlegen der Task", e);
        }
    }

    private Account getAccountById(int accountId) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "SELECT accountId, email, passwordHash, username FROM accounts WHERE accountId = ?")) {
            sqlStatement.setInt(1, accountId);

            try (ResultSet result = sqlStatement.executeQuery()) {
                if (result.next()) {
                    return new Account(result.getInt("accountId"), result.getString("email"),
                            result.getString("passwordHash"), result.getString("username"));
                }
                throw new RuntimeException("Account mit ID " + accountId + " nicht gefunden");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden des Accounts mit ID " + accountId, e);
        }
    }

    private List<Account> getAssigneesForTask(int taskId) {
        List<Account> assignees = new ArrayList<>();
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "SELECT a.accountId, a.email, a.passwordHash, a.username FROM accounts a " +
                        "JOIN task_assignees ta ON a.accountId = ta.accountId WHERE ta.taskId = ?")) {
            sqlStatement.setInt(1, taskId);

            try (ResultSet result = sqlStatement.executeQuery()) {
                while (result.next()) {
                    assignees.add(new Account(result.getInt("accountId"), result.getString("email"),
                            result.getString("passwordHash"), result.getString("username")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden der Assignees für Task " + taskId, e);
        }
        return assignees;
    }

    // Sinn, korrekte Anzeige bei Checkboxen. Hier werden alle Accounts angezeigt
    // und mit der Eigenschaft ob diese Assigned sind oder nicht
    @Override
    public List<Map<String, Object>> getAccountsWithAssignmentForTask(int taskId) {
        List<Map<String, Object>> accounts = new ArrayList<>();

        String sql = """
                SELECT a.accountId,
                       a.email,
                       a.username,
                       CASE WHEN ta.accountId IS NULL THEN 0 ELSE 1 END AS isAssigned
                FROM accounts a
                LEFT JOIN task_assignees ta
                       ON ta.accountId = a.accountId
                      AND ta.taskId = ?
                ORDER BY a.username
                """;

        try (PreparedStatement sqlStatement = connection.prepareStatement(sql)) {
            sqlStatement.setInt(1, taskId);

            try (ResultSet result = sqlStatement.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> account = new HashMap<>();
                    account.put("accountId", result.getInt("accountId"));
                    account.put("email", result.getString("email"));
                    account.put("username", result.getString("username"));
                    account.put("isAssigned", result.getInt("isAssigned") == 1);
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden der Account-Zuordnung fuer Task " + taskId, e);
        }

        return accounts;
    }

    @Override
    public List<Map<String, Object>> getTasksWithAssignmentForUserStory(int userStoryId) {
        List<Map<String, Object>> tasks = new ArrayList<>();

        String sql = """
                SELECT t.taskId, t.title,
                       CASE WHEN ust.taskId IS NULL THEN 0 ELSE 1 END AS isAssigned
                FROM tasks t
                LEFT JOIN user_story_tasks ust
                       ON ust.taskId = t.taskId
                      AND ust.userStoryId = ?
                ORDER BY t.title
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userStoryId);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("taskId", result.getInt("taskId"));
                    task.put("title", result.getString("title"));
                    task.put("isAssigned", result.getInt("isAssigned") == 1);
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden der Task-Zuordnung fuer UserStory " + userStoryId, e);
        }

        return tasks;
    }

  @Override
public List<Task> getTasksWithoutAssignmentForUserStory(int backlogId) {
    List<Task> tasks = new ArrayList<>();

    String sql = """
            SELECT t.taskId, t.title, t.description, t.creatorId,
                   t.startDate, t.dueDate, t.createdDate,
                   t.estimatedTime, t.actualTime, t.status, t.priorities, t.lastUpdated
            FROM tasks t
            JOIN backlog_tasks bt ON t.taskId = bt.taskId
            WHERE bt.backlogId = ?
            AND NOT EXISTS (
                SELECT 1
                FROM user_story_tasks ust
                WHERE ust.taskId = t.taskId
            )
            """;


    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, backlogId);

        try (ResultSet result = stmt.executeQuery()) {
            while (result.next()) {

                int id = result.getInt("taskId");
                String title = result.getString("title");
                String desc = result.getString("description");
                int creatorId = result.getInt("creatorId");
                Date start = result.getDate("startDate");
                Date due = result.getDate("dueDate");
                Date created = result.getDate("createdDate");
                int estimatedTime = result.getInt("estimatedTime");
                int actualTime = result.getInt("actualTime");
                TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                int priorities = result.getInt("priorities");  
                long lastUpdated = result.getLong("lastUpdated");

                Account creator = getAccountById(creatorId);
                List<Account> assignees = getAssigneesForTask(id);

                Task task = new Task(
                        id, title, desc, assignees, creator,
                        start, due, created,
                        estimatedTime, actualTime, status, priorities, lastUpdated
                );

                tasks.add(task);
            }
        }

    } catch (SQLException e) {
        throw new RuntimeException(
            "Fehler beim Laden unzugeordneter Tasks für Backlog " + backlogId, e);
    }

    return tasks;
}

    private void saveAssigneesForTask(int taskId, List<Account> assignees) {
        // First, delete existing assignees
        try (PreparedStatement deleteStatement = connection.prepareStatement(
                "DELETE FROM task_assignees WHERE taskId = ?")) {
            deleteStatement.setInt(1, taskId);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Löschen der alten Assignees für Task " + taskId, e);
        }

        // Then, insert new assignees
        try (PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO task_assignees(taskId, accountId) VALUES(?, ?)")) {
            for (Account assignee : assignees) {
                insertStatement.setInt(1, taskId);
                insertStatement.setInt(2, assignee.accountId());
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Speichern der Assignees für Task " + taskId, e);
        }
    }

    @Override
    public List<Task> searchTasks(String search) {
        List<Task> tasks = new ArrayList<>();
        String sql = """
                    SELECT taskId, title, description, creatorId, startDate, dueDate,
                           createdDate, estimatedTime, actualTime, status, lastUpdated
                    FROM tasks
                    WHERE LOWER(title) LIKE ?
                """;
        try (PreparedStatement sqlStatement = connection.prepareStatement(sql)) {

            sqlStatement.setString(1, "%" + search.toLowerCase() + "%");

            try (ResultSet result = sqlStatement.executeQuery()) {
                while (result.next()) {

                    int id = result.getInt("taskId");
                    String title = result.getString("title");
                    String desc = result.getString("description");
                    int creatorId = result.getInt("creatorId");
                    Date start = result.getDate("startDate");
                    Date due = result.getDate("dueDate");
                    Date created = result.getDate("createdDate");
                    int estimatedTime = result.getInt("estimatedTime");
                    int actualTime = result.getInt("actualTime");
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                    int priorities = result.getInt("priorities");  
                    long lastUpdated = result.getLong("lastUpdated");

                    Account creator = getAccountById(creatorId);
                    List<Account> assignees = getAssigneesForTask(id);

                    Task task = new Task(
                            id, title, desc, assignees, creator,
                            start, due, created,
                            estimatedTime, actualTime, status, priorities, lastUpdated);

                    tasks.add(task);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler bei Task-Suche", e);
        }

        return tasks;
    }

    @Override
    public List<Task> getTasksByBacklogId(int backlogId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.taskId, t.title, t.description, t.creatorId, t.startDate, t.dueDate, " +
                "t.createdDate, t.estimatedTime, t.actualTime, t.status, t.lastUpdated " +
                "FROM tasks t JOIN backlog_tasks bt ON t.taskId = bt.taskId WHERE bt.backlogId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, backlogId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("taskId");
                    String title = result.getString("title");
                    String desc = result.getString("description");
                    int creatorId = result.getInt("creatorId");
                    Date start = result.getDate("startDate");
                    Date due = result.getDate("dueDate");
                    Date created = result.getDate("createdDate");
                    int estimatedTime = result.getInt("estimatedTime");
                    int actualTime = result.getInt("actualTime");
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                    Account creator = getAccountById(creatorId);
                    int priorities = result.getInt("priorities");  
                    List<Account> assignees = getAssigneesForTask(id);
                    long lastUpdated = result.getLong("lastUpdated");
                    tasks.add(new Task(id, title, desc, assignees, creator, start, due, created, estimatedTime,
                            actualTime, status, priorities, lastUpdated));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden der Tasks für Backlog " + backlogId, e);
        }
        return tasks;
    }

    @Override
    public List<Task> filterTask(List<Task> list, int filter) {
        if (filter == 0) {
            list.removeIf(task -> task.status() != TaskStatus.TODO);
        }
        if (filter == 1) {
            list.removeIf(task -> task.status() != TaskStatus.IN_PROGRESS);
        } else if (filter == 2) {
            list.removeIf(task -> task.status() != TaskStatus.COMPLETED);
        }
        return list;
    }
    @Override
    public List<Task> checkwhendue(List<Task> list){
        List<Task> tasks = new ArrayList<>();
        Date now = new Date();
        Date in7Days = new Date(now.getTime()+7L*24*60*60*1000);
        for(Task t: list){
            Date due = t.due();
            if(due != null && !due.before(now) && !due.after(in7Days)){
                tasks.add(t);
            }
            
        }
        tasks.sort(Comparator.comparing(Task::due));
        return tasks;
    }


}
