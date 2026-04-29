package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

}
