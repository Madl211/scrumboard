package de.uni.scrumboard.service;

import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Backlog> getBacklogs() {
        return backlogRepository.getBacklogs();
    }

    @Override
    public Optional<Backlog> getBacklogById(int id) {
        return backlogRepository.getBacklogById(id);
    }

    @Override
    public void addTaskToBacklog(int backlogId, int taskId) {
        backlogRepository.addTaskToBacklog(backlogId, taskId);
    }

}
