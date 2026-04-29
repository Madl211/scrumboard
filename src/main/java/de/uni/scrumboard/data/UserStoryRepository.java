package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.uni.scrumboard.data.interfaces.IUserStoryRepository;

public class UserStoryRepository implements IUserStoryRepository {

    private final Connection connection;

    public UserStoryRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createUserStory(UserStory userStory) {
        String insertStatement = "INSERT INTO user_stories(titel, bezeichnung, creatorId, backlogId) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, userStory.titel());
            preparedStatement.setString(2, userStory.beschreibung());
            preparedStatement.setInt(3, userStory.creatorId());
            preparedStatement.setInt(4, userStory.backlogId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
