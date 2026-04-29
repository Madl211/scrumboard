package de.uni.scrumboard.service.interfaces;

import java.util.List;

import de.uni.scrumboard.data.Backlog;

public interface IBacklogService {

    void createBacklog(Backlog backlog);

    List<Backlog> getBacklogs();

}
