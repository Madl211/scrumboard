package de.uni.scrumboard.data.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Task;
import de.uni.scrumboard.data.enums.TaskStatus;

public interface ITaskRepository {
    Optional<Task> getTaskById(int taskId);

    List<Task> getAllTasks();
    List<Task> searchTasks(String search);
    List<Task> filterTask(List<Task> list, int filter);
    boolean saveTask(Task task, int taskId);

    Task createTask(int taskId, String title, String desc, List<Account> assignees, Account creator, Date start,
            Date due,
            Date created, int estimatedTime, int actualTime, TaskStatus status);

    List<Task> getTasksByBacklogId(int backlogId);

    List<Map<String, Object>> getAccountsWithAssignmentForTask(int taskId);
    List<Task> checkwhendue(List<Task>list);

    List<Map<String, Object>> getTasksWithAssignmentForUserStory(int userStoryId);

    List<Task> getTasksWithoutAssignmentForUserStory(int backlogId);

}
