package de.uni.scrumboard.service;

import java.util.List;
import java.util.Optional;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.BacklogAssignee;
import de.uni.scrumboard.data.Timestamp;
import de.uni.scrumboard.data.interfaces.IBacklogRepository;
import de.uni.scrumboard.service.interfaces.IBacklogService;

public class BacklogService implements IBacklogService {

    private IBacklogRepository backlogRepository;

    public BacklogService(IBacklogRepository backlogRepository) {
        this.backlogRepository = backlogRepository;
    }

    @Override
    public void createBacklog(Backlog backlog) {
        backlogRepository.createBacklog(backlog);
    }

    @Override
    public List<Backlog> getBacklogs() {
        return backlogRepository.getBacklogs();
    }

    @Override
    public Optional<Backlog> getBacklogById(int id) {
        return backlogRepository.getBacklogById(id);
    }

    @Override
    public Optional<Integer> getBacklogIdByTaskId(int taskId) {
        return backlogRepository.getBacklogIdByTaskId(taskId);
    }

    @Override
    public void addTaskToBacklog(int backlogId, int taskId) {
        backlogRepository.addTaskToBacklog(backlogId, taskId);
    }

    public void removeTaskFromBacklog(int backlogId, int taskId) {
        backlogRepository.removeTaskFromBacklog(backlogId, taskId);
    }

    @Override
    public Timestamp getLastUpdate(int backlogId) {
        return backlogRepository.getLastUpdate(backlogId);
    }


    @Override
    public List<BacklogAssignee> getBacklogAssignees(int backlogId) {
        return backlogRepository.getAssigneesByBacklogId(backlogId);
    }

    @Override
    public List<Account> getAvailableUsersForBacklog(int backlogId) {
        return backlogRepository.getAvailableUsersForBacklog(backlogId);
    }

    @Override
    public boolean addAssigneeToBacklog(int backlogId, int assigneeId, String role) {
        return backlogRepository.addAssigneeToBacklog(backlogId, assigneeId, role);
    }

    @Override
    public boolean removeAssigneeFromBacklog(int backlogId, int assigneeId) {
        return backlogRepository.removeAssigneeFromBacklog(backlogId, assigneeId);
    }

    @Override
    public boolean updateAssigneeRole(int backlogId, int assigneeId, String newRole) {
        return backlogRepository.updateAssigneeRole(backlogId, assigneeId, newRole);
    }

    @Override
    public boolean isUserAssignedToBacklog(int backlogId, int assigneeId) {
        return backlogRepository.isUserAssignedToBacklog(backlogId, assigneeId);
    }

    @Override
    public int getProductOwnerIdByBacklogId(int backlogId) {
        return backlogRepository.getProductOwnerIdByBacklogId(backlogId);
    }
}
