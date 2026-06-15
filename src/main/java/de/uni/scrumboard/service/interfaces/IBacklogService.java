package de.uni.scrumboard.service.interfaces;

import java.util.List;

import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.Timestamp;

import java.util.Optional;

public interface IBacklogService {

    void createBacklog(Backlog backlog);

    List<Backlog> getBacklogs();

    Optional<Backlog> getBacklogById(int id);

    Optional<Integer> getBacklogIdByTaskId(int taskId);

    void addTaskToBacklog(int backlogId, int taskId);

    void removeTaskFromBacklog(int backlogId, int taskId);

    Timestamp getLastUpdate(int backlogId);

}
