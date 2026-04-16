package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.data.interfaces.ITaskRepository;

public record TaskRepository(Connection connection) implements ITaskRepository {

    @Override
    public Optional<Task> getTaskById(int taskId) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "SELECT taskId, title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status FROM tasks WHERE taskId = ?")) {
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
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));

                    Account creator = getAccountById(creatorId);
                    List<Account> assignees = getAssigneesForTask(taskId);

                    Task task = new Task(id, title, desc, assignees, creator, start, due, created, estimatedTime,
                            actualTime, status);
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
                "SELECT taskId, title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status FROM tasks")) {

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
                    TaskStatus status = TaskStatus.itoStatus(result.getInt(("status")));

                    Account creator = getAccountById(creatorId);
                    List<Account> assignees = getAssigneesForTask(id);

                    Task task = new Task(id, title, desc, assignees, creator, start, due, created, estimatedTime,
                            actualTime, status);
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
                "INSERT INTO tasks(taskId, title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON CONFLICT(taskId) DO UPDATE SET "
                        + "title = excluded.title, "
                        + "description = excluded.description, "
                        + "startDate = excluded.startDate, "
                        + "dueDate = excluded.dueDate, "
                        + "estimatedTime = excluded.estimatedTime, "
                        + "actualTime = excluded.actualTime")) {

            sqlStatement.setInt(1, taskId);
            sqlStatement.setString(2, task.title());
            sqlStatement.setString(3, task.desc());
            sqlStatement.setInt(4, task.creator().accountId());
            sqlStatement.setDate(5, task.start() != null ? new java.sql.Date(task.start().getTime()) : null);
            sqlStatement.setDate(6, task.due() != null ? new java.sql.Date(task.due().getTime()) : null);
            sqlStatement.setDate(7, task.created() != null ? new java.sql.Date(task.created().getTime()) : null);
            sqlStatement.setInt(8, task.estimatedTime());
            sqlStatement.setInt(9, task.actualTime());

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
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "INSERT INTO tasks(title, description, creatorId, startDate, dueDate, createdDate, estimatedTime, actualTime, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
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

            int affectedRows = sqlStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Konnte Task nicht anlegen");
            }

            try (ResultSet generatedKeys = sqlStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedTaskId = generatedKeys.getInt(1);
                    Task task = new Task(generatedTaskId, title, desc, assignees, creator, start, due, created,
                            estimatedTime, actualTime, status);
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
}
