package de.uni.scrumboard.service.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.data.UserStory;

public interface IUserStoryService {

    int createUserStory(UserStory userStory);

    List<UserStory> getUserStoriesByBacklogId(int backlogId);

    Optional<UserStory> getUserStoryById(int id);

    void updateUserStory(UserStory userStory);

    void setTasksForUserStory(int userStoryId, List<Integer> taskIds);

    List<Map<String, Object>> getTasksByUserStoryId(int userStoryId);

    int getProductOwnerByUserStoryId (int userStoryId);

    int getUserStoryPriority(int userStoryId); 


}
