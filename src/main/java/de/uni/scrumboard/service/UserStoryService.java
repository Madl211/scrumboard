package de.uni.scrumboard.service;

import de.uni.scrumboard.data.interfaces.IUserStoryRepository;
import de.uni.scrumboard.data.UserStory;
import de.uni.scrumboard.service.interfaces.IUserStoryService;

public class UserStoryService implements IUserStoryService {

    private IUserStoryRepository userStoryRepository;

    public UserStoryService(IUserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }

    @Override
    public void createUserStory(UserStory userStory) {
        userStoryRepository.createUserStory(userStory);
    }

}
