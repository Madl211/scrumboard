package de.uni.scrumboard.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Task;
import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.data.interfaces.IAccountRepository;
import de.uni.scrumboard.data.interfaces.ITaskRepository;
import de.uni.scrumboard.service.interfaces.ITaskService;
import java.util.Map;

public record TaskService(ITaskRepository taskRepository, IAccountRepository accountRepository)
        implements ITaskService {

    @Override
    public Optional<Task> getTaskById(int taskId) {
        return taskRepository.getTaskById(taskId);
    }

    @Override
    public List<Task> searchTasks(String search, int filter) {
        if (filter != 0 && filter != 1 && filter != 2){
            if(search==null){
            return taskRepository.getAllTasks();
            }
            return taskRepository.searchTasks(search);
        }  
        else if(search==null){
            return taskRepository.filterTask(taskRepository.getAllTasks(),filter);
            }
            return taskRepository.filterTask(taskRepository.searchTasks(search),filter);

    }
    @Override
    public Task createTask(String title, String description, List<Integer> assigneeIds, int creatorId, Date startDate,
            Date dueDate, int estimatedTime, TaskStatus status) {
                
        Account creator = accountRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        List<Account> assignees = assigneeIds.stream()
                .map(id -> accountRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Assignee not found: " + id)))
                .collect(Collectors.toList());

        Date created = new Date();
        return taskRepository.createTask(0, title, description, assignees, creator, startDate, dueDate, created,
                estimatedTime, 0, status);
    }

    @Override
    public boolean updateTask(Task task) {
        return taskRepository.saveTask(task, task.taskId());
    }

    @Override
    public List<Task> getTasksByBacklogId(int backlogId) {
        return taskRepository.getTasksByBacklogId(backlogId);
    }

    @Override
    public List<Map<String, Object>> getAccountsWithAssignmentForTask(int taskId) {
    return taskRepository.getAccountsWithAssignmentForTask(taskId);
}
}