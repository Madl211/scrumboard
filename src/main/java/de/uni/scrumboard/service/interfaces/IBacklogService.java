package de.uni.scrumboard.service.interfaces;

import java.util.List;
import java.util.Optional;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.BacklogAssignee;
import de.uni.scrumboard.data.Timestamp;

public interface IBacklogService {

    void createBacklog(Backlog backlog);

    List<Backlog> getBacklogs();

    Optional<Backlog> getBacklogById(int id);

    Optional<Integer> getBacklogIdByTaskId(int taskId);

    void addTaskToBacklog(int backlogId, int taskId);

    void removeTaskFromBacklog(int backlogId, int taskId);

    Timestamp getLastUpdate(int backlogId);

    List<BacklogAssignee> getBacklogAssignees(int backlogId);

    List<Account> getAvailableUsersForBacklog(int backlogId);

    boolean addAssigneeToBacklog(int backlogId, int assigneeId, String role);

    boolean removeAssigneeFromBacklog(int backlogId, int assigneeId);

    boolean updateAssigneeRole(int backlogId, int assigneeId, String newRole);
    
    boolean isUserAssignedToBacklog(int backlogId, int assigneeId);
    
    int getProductOwnerIdByBacklogId(int backlogId);
}
