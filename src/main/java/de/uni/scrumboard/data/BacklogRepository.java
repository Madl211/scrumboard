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

}
