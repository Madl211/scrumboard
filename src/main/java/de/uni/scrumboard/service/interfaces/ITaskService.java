package de.uni.scrumboard.service.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import de.uni.scrumboard.data.Task;
import de.uni.scrumboard.data.enums.TaskStatus;

public interface ITaskService {
    Optional<Task> getTaskById(int taskId);
    List<Task> searchTasks(String search);
    Task createTask(String title, String description, List<Integer> assigneeIds, int creatorId, Date startDate, Date dueDate, int estimatedTime, TaskStatus status);
    boolean updateTask(Task task);
}