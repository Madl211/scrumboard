package de.uni.scrumboard.presentation;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.BacklogAssignee;
import de.uni.scrumboard.data.Task;
import de.uni.scrumboard.data.Timestamp;
import de.uni.scrumboard.data.UserStory;
import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.presentation.interfaces.IBacklogController;
import de.uni.scrumboard.service.interfaces.IBacklogService;
import de.uni.scrumboard.service.interfaces.ITaskService;
import de.uni.scrumboard.service.interfaces.IUserStoryService;
import io.javalin.http.Context;

public class BacklogController implements IBacklogController {

    private final static String templatePath = "templates/create-backlog.mustache";

    private IBacklogService backlogService;
    private IUserStoryService userStoryService;
    private ITaskService taskService;

    public BacklogController(IBacklogService backlogService, IUserStoryService userStoryService, ITaskService taskService) {
        this.backlogService = backlogService;
        this.userStoryService = userStoryService;
        this.taskService = taskService;
    }

    @Override
    public void getCreateBacklog(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        context.render(templatePath);
    }

    @Override
    public void postCreateBacklog(Context context) {
        String bezeichnung = context.formParam("bezeichnung");

        Backlog backlog = new Backlog(-1, bezeichnung);

        try {
            backlogService.createBacklog(backlog);
        } catch (IllegalArgumentException e) {
            Map<String, Object> model = new HashMap<>();
            model.put("showErrorMessage", true);
            context.render(templatePath, model);
            return;
        }
        context.redirect("/home");
    }

    @Override
    public void getBacklogs(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        List<Backlog> backlogs = backlogService.getBacklogs();
        Map<String, Object> model = new HashMap<>();
        model.put("backlogs", backlogs);
        context.render("templates/backlogs.mustache", model);
    }

    @Override
    public void getBacklog(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        int backlogId;
        try {
            backlogId = Integer.parseInt(context.pathParam("id"));
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige Backlog-ID");
            return;
        }

        Optional<Backlog> backlog = backlogService.getBacklogById(backlogId);
        if (backlog.isEmpty()) {
            context.status(404).result("Backlog nicht gefunden");
            return;
        }

        List<UserStory> userStories = userStoryService.getUserStoriesByBacklogId(backlogId);

        List<Map<String, Object>> todoUserStories = userStories.stream()
                .filter(s -> s.status() == TaskStatus.TODO)
                .map(s -> enrichUserStory(s))
                .collect(Collectors.toList());
        List<Map<String, Object>> inProgressUserStories = userStories.stream()
                .filter(s -> s.status() == TaskStatus.IN_PROGRESS)
                .map(s -> enrichUserStory(s))
                .collect(Collectors.toList());
        List<Map<String, Object>> doneUserStories = userStories.stream()
                .filter(s -> s.status() == TaskStatus.COMPLETED)
                .map(s -> enrichUserStory(s))
                .collect(Collectors.toList());
        List<Map<String, Object>> reviewUserStories = userStories.stream()
                .filter(s -> s.status() == TaskStatus.REVIEW)
                .map(s -> enrichUserStory(s))
                .collect(Collectors.toList());
        List<Map<String, Object>> testUserStories = userStories.stream()
                .filter(s -> s.status() == TaskStatus.TEST)
                .map(s -> enrichUserStory(s))
                .collect(Collectors.toList());


        List<Task> tasks = taskService.getTasksWithoutAssignmentForUserStory(backlogId);

        List<Task> todoTasks = tasks.stream()
                .filter(t -> t.status() == TaskStatus.TODO)
                .collect(Collectors.toList());
        List<Task> inProgressTasks = tasks.stream()
                .filter(t -> t.status() == TaskStatus.IN_PROGRESS)
                .collect(Collectors.toList());
        List<Task> doneTasks = tasks.stream()
                .filter(t -> t.status() == TaskStatus.COMPLETED)
                .collect(Collectors.toList());
        List<Task> reviewTasks = tasks.stream()
                .filter(t -> t.status() == TaskStatus.REVIEW)
                .collect(Collectors.toList());
        List<Task> testTasks = tasks.stream()
                .filter(t -> t.status() == TaskStatus.TEST)
                .collect(Collectors.toList());

        Timestamp timestampOfLastCheckedUpdate = backlogService.getLastUpdate(backlogId);

        Map<String, Object> model = new HashMap<>();
        model.put("backlog", backlog.get());
        model.put("todoUserStories", todoUserStories);
        model.put("inProgressUserStories", inProgressUserStories);
        model.put("doneUserStories", doneUserStories);

        model.put("reviewUserStories", reviewUserStories);
        model.put("testUserStories", testUserStories);
        model.put("reviewTasks", reviewTasks);
        model.put("testTasks", testTasks);

        model.put("todoTasks", todoTasks);
        model.put("inProgressTasks", inProgressTasks);
        model.put("doneTasks", doneTasks);
        model.put("timestampOfLastCheckedUpdate", timestampOfLastCheckedUpdate.timestamp());
        context.render("templates/backlog.mustache", model);
    }

    private Map<String, Object> enrichUserStory(UserStory us) {
        List<Map<String, Object>> tasks = userStoryService.getTasksByUserStoryId(us.id());
        Map<String, Object> map = new HashMap<>();
        map.put("id", us.id());
        map.put("titel", us.titel());
        map.put("beschreibung", us.beschreibung());
        map.put("tasks", tasks);
        return map;
    }

    @Override
    public void getLastUpdate(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        int backlogId;
        try {
            backlogId = Integer.parseInt(context.pathParam("id"));
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige Backlog-ID");
            return;
        }

        Timestamp timestampOfLastUpdate = backlogService.getLastUpdate(backlogId);
        context.json(timestampOfLastUpdate);
    }
    
     @Override
    public void getBacklogAssignees(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        int backlogId;
        try {
            backlogId = Integer.parseInt(context.pathParam("id"));
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige Backlog-ID");
            return;
        }

        Optional<Backlog> backlog = backlogService.getBacklogById(backlogId);
        if (backlog.isEmpty()) {
            context.status(404).result("Backlog nicht gefunden");
            return;
        }

        List<BacklogAssignee> backlogAssignees = backlogService.getBacklogAssignees(backlogId);

        Map<String, Object> model = new HashMap<>();
        model.put("backlog", backlog.get());
        model.put("backlogAssignees", backlogAssignees);  
        model.put("timestampOfLastCheckedUpdate", Instant.now().getEpochSecond());

        context.render("templates/contributer.mustache", model);  
    }
    

    @Override
    public void getAddBacklogAssignee(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        int backlogId;
        try {
            backlogId = Integer.parseInt(context.pathParam("id"));
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige Backlog-ID");
            return;
        }

        Optional<Backlog> backlog = backlogService.getBacklogById(backlogId);
        if (backlog.isEmpty()) {
            context.status(404).result("Backlog nicht gefunden");
            return;
        }

      
        List<Account> availableUsers = backlogService.getAvailableUsersForBacklog(backlogId);

        Map<String, Object> model = new HashMap<>();
        model.put("backlog", backlog.get());
        model.put("availableUsers", availableUsers);

        context.render("templates/add-assignees.mustache", model);
    }


    @Override
    public void postAddBacklogAssignee(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        int backlogId;
        try {
            backlogId = Integer.parseInt(context.pathParam("id"));
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige Backlog-ID");
            return;
        }

        
        String assigneeIdString = context.formParam("assigneeid");
        String role = context.formParam("role");

        if (assigneeIdString == null || assigneeIdString.isBlank()) {
            context.status(400).result("Bitte wählen Sie einen Benutzer aus");
            return;
        }

        int assigneeId;
        try {
            assigneeId = Integer.parseInt(assigneeIdString);
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige Benutzer-ID");
            return;
        }

        if (role == null || role.isBlank()) {
            role = "Developer";
                    }

        boolean success = backlogService.addAssigneeToBacklog(backlogId, assigneeId, role);
        if (!success) {
            context.status(400).result("Fehler beim Hinzufügen des Assignees");
            return;
        }

        context.redirect("/backlog/" + backlogId + "/assignees");
    }

    @Override
    public void postRemoveBacklogAssignee(Context context) {
    Integer accountId = context.sessionAttribute("accountId");
    if (accountId == null) {
        context.redirect("/login");
        return;
    }

    int backlogId;
    int assigneeId;
    try {
        backlogId = Integer.parseInt(context.pathParam("id"));
        assigneeId = Integer.parseInt(context.pathParam("assigneeId"));
    } catch (NumberFormatException e) {
        context.status(400).result("Ungültige ID");
        return;
    }

    boolean success = backlogService.removeAssigneeFromBacklog(backlogId, assigneeId);
    if (!success) {
        context.status(400).result("Fehler beim Entfernen des Assignees");
        return;
    }

    context.redirect("/backlog/" + backlogId + "/assignees");
}

}







