package de.uni.scrumboard.service.interfaces;

import java.util.List;

import de.uni.scrumboard.data.Backlog;

import java.util.Optional;

public interface IBacklogService {

    void createBacklog(Backlog backlog);

    List<Backlog> getBacklogs();

    Optional<Backlog> getBacklogById(int id);

    void addTaskToBacklog(int backlogId, int taskId);

}
