package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.uni.scrumboard.data.interfaces.IBacklogRepository;

public class BacklogRepository implements IBacklogRepository {

    private Connection connection;

    public BacklogRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createBacklog(Backlog backlog) {
        String insertBacklogStatement = "INSERT INTO backlogs(`bezeichnung`) VALUES (?);";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertBacklogStatement);
            preparedStatement.setString(1, backlog.bezeichnung());

            preparedStatement.execute();
        } catch (SQLException e) {
            // siehe https://sqlite.org/rescode.html#constraint
            if (e.getErrorCode() == 19) {
                throw new IllegalArgumentException(
                        "Das Backlog mit der Bezeichnung " + backlog.bezeichnung() + " existiert bereits");
            }
            e.printStackTrace();
        }
    }

    @Override
    public List<Backlog> getBacklogs() {
        String selectAllBacklogsStatement = "SELECT * FROM backlogs;";
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(selectAllBacklogsStatement)) {

            List<Backlog> backlogs = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String bezeichnung = resultSet.getString("bezeichnung");
                Backlog backlog = new Backlog(id, bezeichnung);
                backlogs.add(backlog);
            }

            return backlogs;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Backlog> getBacklogById(int id) {
        String sql = "SELECT id, bezeichnung FROM backlogs WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Backlog(rs.getInt("id"), rs.getString("bezeichnung")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void addTaskToBacklog(int backlogId, int taskId) {
        String sql = "INSERT OR IGNORE INTO backlog_tasks(backlogId, taskId) VALUES(?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, backlogId);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Zuordnen der Task zum Backlog", e);
        }
    }
    @Override
    public Optional<Integer> getBacklogIdByTaskId(int taskId) {
        String sql = "SELECT backlogId FROM backlog_tasks WHERE taskId = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("backlogId"));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Lesen der Backlog-Zuordnung", e);
        }
    }
    @Override
    public void removeTaskFromBacklog(int backlogId, int taskId) {
        String sql = "DELETE FROM backlog_tasks WHERE backlogId = ? AND taskId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, backlogId);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Entfernen der Task aus dem Backlog", e);
        }
    }

    @Override
    public Timestamp getLastUpdate(int backlogId) {
        String sql = "SELECT lastUpdated FROM user_stories WHERE backlogId = ? UNION ALL SELECT lastUpdated FROM backlog_tasks JOIN tasks ON backlog_tasks.taskId = tasks.taskId WHERE backlog_tasks.backlogId = ? ORDER BY lastUpdated DESC LIMIT 1;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, backlogId);
            statement.setInt(2, backlogId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return new Timestamp(0);
            }
            return new Timestamp(resultSet.getInt("lastUpdated"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler beim Abfragen der letzten Aktualisierung aufgetreten");
        }
    }
    @Override
    public List<BacklogAssignee> getAssigneesByBacklogId(int backlogId) {
        List<BacklogAssignee> assignees = new ArrayList<>();
        String sql = """
            SELECT 
                ba.backlogId,
                ba.assigneeid,
                a.username,
                a.email,
                ba.roles as role
            FROM backlog_assignees ba
            JOIN accounts a ON ba.assigneeid = a.accountId
            WHERE ba.backlogId = ?
            ORDER BY ba.roles, a.username
        """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, backlogId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignees.add(new BacklogAssignee(
                        rs.getInt("backlogId"),
                        rs.getInt("assigneeid"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignees;
    }


    @Override
    public boolean addAssigneeToBacklog(int backlogId, int assigneeId, String role) {
        String sql = "INSERT INTO backlog_assignees (backlogId, assigneeid, roles) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, backlogId);
            ps.setInt(2, assigneeId);
            ps.setString(3, role);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean removeAssigneeFromBacklog(int backlogId, int assigneeId) {
        String sql = "DELETE FROM backlog_assignees WHERE backlogId = ? AND assigneeid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, backlogId);
            ps.setInt(2, assigneeId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
     public boolean updateAssigneeRole(int backlogId, int assigneeId, String newRole) {
        String sql = "UPDATE backlog_assignees SET roles = ? WHERE backlogId = ? AND assigneeid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, backlogId);
            ps.setInt(3, assigneeId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean isUserAssignedToBacklog(int backlogId, int assigneeId) {
        String sql = "SELECT 1 FROM backlog_assignees WHERE backlogId = ? AND assigneeid = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, backlogId);
            ps.setInt(2, assigneeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public int getProductOwnerIdByBacklogId(int backlogId) {
        String sql = "SELECT assigneeid FROM backlog_assignees WHERE backlogId = ? AND roles = 'Product Owner' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, backlogId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("assigneeid");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Account> getAvailableUsersForBacklog(int backlogId) {
    List<Account> availableUsers = new ArrayList<>();
    String sql = """
        SELECT accountId, email, passwordHash, username 
        FROM accounts 
        WHERE accountId NOT IN (
            SELECT assigneeid 
            FROM backlog_assignees 
            WHERE backlogId = ?
        )
        ORDER BY username
    """;
    
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setInt(1, backlogId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                availableUsers.add(new Account(
                    rs.getInt("accountId"),
                    rs.getString("email"),
                    rs.getString("passwordHash"),
                    rs.getString("username")
                ));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return availableUsers;
}

}
