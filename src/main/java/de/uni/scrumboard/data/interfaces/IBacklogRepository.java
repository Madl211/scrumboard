package de.uni.scrumboard.data.interfaces;

import java.util.List;

import de.uni.scrumboard.data.Backlog;

import java.util.Optional;

public interface IBacklogRepository {

    void createBacklog(Backlog backlog);

    List<Backlog> getBacklogs();

    Optional<Backlog> getBacklogById(int id);

    Optional<Integer> getBacklogIdByTaskId(int taskId);
    
    void addTaskToBacklog(int backlogId, int taskId); 

    void removeTaskFromBacklog(int backlogId, int taskId);
}
