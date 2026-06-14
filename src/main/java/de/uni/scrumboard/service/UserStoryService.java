package de.uni.scrumboard.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.data.interfaces.IUserStoryRepository;
import de.uni.scrumboard.data.UserStory;
import de.uni.scrumboard.service.interfaces.IUserStoryService;

public class UserStoryService implements IUserStoryService {

    private IUserStoryRepository userStoryRepository;

    public UserStoryService(IUserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }

    @Override
    public int createUserStory(UserStory userStory) {
        return userStoryRepository.createUserStory(userStory);
    }

    @Override
    public List<UserStory> getUserStoriesByBacklogId(int backlogId) {
        return userStoryRepository.getUserStoriesByBacklogId(backlogId);
    }

    @Override
    public Optional<UserStory> getUserStoryById(int id) {
        return userStoryRepository.getUserStoryById(id);
    }

    @Override
    public void updateUserStory(UserStory userStory) {
        userStoryRepository.updateUserStory(userStory);
    }

    @Override
    public void setTasksForUserStory(int userStoryId, List<Integer> taskIds) {
        userStoryRepository.setTasksForUserStory(userStoryId, taskIds);
    }

    @Override
    public List<Map<String, Object>> getTasksByUserStoryId(int userStoryId) {
        return userStoryRepository.getTasksByUserStoryId(userStoryId);
    }

}
