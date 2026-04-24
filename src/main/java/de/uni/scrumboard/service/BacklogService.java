package de.uni.scrumboard.service;

import de.uni.scrumboard.data.Backlog;
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

}
