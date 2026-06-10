package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.data.interfaces.IUserStoryRepository;

public class UserStoryRepository implements IUserStoryRepository {

    private final Connection connection;

    public UserStoryRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int createUserStory(UserStory userStory) {
        String sql = "INSERT INTO user_stories(titel, beschreibung, creatorId, backlogId, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userStory.titel());
            ps.setString(2, userStory.beschreibung());
            ps.setInt(3, userStory.creatorId());
            ps.setInt(4, userStory.backlogId());
            ps.setInt(5, userStory.status().val);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<UserStory> getUserStoriesByBacklogId(int backlogId) {
        List<UserStory> userStories = new ArrayList<>();
        String query = "SELECT id, titel, beschreibung, creatorId, backlogId, status FROM user_stories WHERE backlogId = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, backlogId);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("id");
                    String titel = result.getString("titel");
                    String beschreibung = result.getString("beschreibung");
                    int creatorId = result.getInt("creatorId");
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                    userStories.add(new UserStory(id, titel, beschreibung, creatorId, backlogId, status));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userStories;
    }

    @Override
    public Optional<UserStory> getUserStoryById(int id) {
        String query = "SELECT id, titel, beschreibung, creatorId, backlogId, status FROM user_stories WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    String titel = result.getString("titel");
                    String beschreibung = result.getString("beschreibung");
                    int creatorId = result.getInt("creatorId");
                    int backlogId = result.getInt("backlogId");
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                    return Optional.of(new UserStory(id, titel, beschreibung, creatorId, backlogId, status));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void updateUserStory(UserStory userStory) {
        String sql = "UPDATE user_stories SET titel = ?, beschreibung = ?, backlogId = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userStory.titel());
            ps.setString(2, userStory.beschreibung());
            ps.setInt(3, userStory.backlogId());
            ps.setInt(4, userStory.status().val);
            ps.setInt(5, userStory.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setTasksForUserStory(int userStoryId, List<Integer> taskIds) {
        try (PreparedStatement del = connection.prepareStatement(
                "DELETE FROM user_story_tasks WHERE userStoryId = ?")) {
            del.setInt(1, userStoryId);
            del.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        try (PreparedStatement ins = connection.prepareStatement(
                "INSERT INTO user_story_tasks(userStoryId, taskId) VALUES (?, ?)")) {
            for (int taskId : taskIds) {
                ins.setInt(1, userStoryId);
                ins.setInt(2, taskId);
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, Object>> getTasksByUserStoryId(int userStoryId) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        String sql = "SELECT t.taskId, t.title, t.status FROM tasks t " +
                "JOIN user_story_tasks ust ON t.taskId = ust.taskId " +
                "WHERE ust.userStoryId = ? ORDER BY t.taskId";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userStoryId);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("taskId", result.getInt("taskId"));
                    task.put("title", result.getString("title"));
                    TaskStatus status = TaskStatus.itoStatus(result.getInt("status"));
                    String statusLabel;
                    switch (status) {
                        case IN_PROGRESS -> statusLabel = "In Progress";
                        case COMPLETED   -> statusLabel = "Done";
                        default          -> statusLabel = "Todo";
                    }
                    task.put("statusLabel", statusLabel);
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

}

