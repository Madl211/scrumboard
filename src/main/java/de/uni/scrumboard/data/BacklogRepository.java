package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
                throw new IllegalArgumentException("Das Backlog mit der Bezeichnung " + backlog.bezeichnung() + " existiert bereits");
            }
            e.printStackTrace();
        }
    }

}
