package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.UserStory;
import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.presentation.interfaces.IUserStoryController;
import de.uni.scrumboard.service.interfaces.IBacklogService;
import de.uni.scrumboard.service.interfaces.ITaskService;
import de.uni.scrumboard.service.interfaces.IUserStoryService;
import io.javalin.http.Context;

public class UserStoryController implements IUserStoryController {

    private static final String templatePath = "templates/create-user-story.mustache";
    private static final String userStoryReturnToSessionKey = "userStoryReturnTo";

    private IUserStoryService userStoryService;
    private IBacklogService backlogService;
    private ITaskService taskService;

    public UserStoryController(IUserStoryService userStoryService, IBacklogService backlogService, ITaskService taskService) {
        this.userStoryService = userStoryService;
        this.backlogService = backlogService;
        this.taskService = taskService;
    }

    private void rememberReturnPath(Context context) {
        String referer = context.header("Referer");
        if (referer == null || referer.isBlank()) {
            return;
        }

        String currentOrigin = context.scheme() + "://" + context.host();
        String candidatePath = null;

        if (referer.startsWith(currentOrigin)) {
            candidatePath = referer.substring(currentOrigin.length());
        } else if (referer.startsWith("/")) {
            candidatePath = referer;
        }

        if (isSafeInternalPath(candidatePath)
                && !candidatePath.startsWith("/create-user-story")
                && !candidatePath.startsWith("/edit-user-story")) {
            context.sessionAttribute(userStoryReturnToSessionKey, candidatePath);
        }
    }

    private boolean isSafeInternalPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (!path.startsWith("/")) {
            return false;
        }
        if (path.startsWith("//")) {
            return false;
        }
        return !path.contains("\r") && !path.contains("\n");
    }

    private boolean isBacklogPath(String path) {
        return path != null && path.matches("^/backlog/\\d+([?#].*)?$");
    }

    private boolean isHomePath(String path) {
        return "/home".equals(path) || (path != null && path.startsWith("/home?"));
    }

    private String backlogOrHome(String candidate, String fallbackPath) {
        if (isBacklogPath(candidate)) {
            return candidate;
        }
        if (isHomePath(candidate)) {
            return "/home";
        }
        if (isBacklogPath(fallbackPath)) {
            return fallbackPath;
        }
        return "/home";
    }

    private String resolveReturnPathAndClear(Context context, String fallbackPath) {
        String returnPath = context.sessionAttribute(userStoryReturnToSessionKey);
        context.sessionAttribute(userStoryReturnToSessionKey, null);

        if (isSafeInternalPath(returnPath)) {
            return backlogOrHome(returnPath, fallbackPath);
        }
        return backlogOrHome(null, fallbackPath);
    }

    private String currentReturnPathOrFallback(Context context, String fallbackPath) {
        String returnPath = context.sessionAttribute(userStoryReturnToSessionKey);
        if (isSafeInternalPath(returnPath)) {
            return backlogOrHome(returnPath, fallbackPath);
        }
        return backlogOrHome(null, fallbackPath);
    }

    @Override
    public void getCreateUserStory(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        rememberReturnPath(context);

        List<Backlog> backlogs = backlogService.getBacklogs();
        Map<String, Object> model = new HashMap<>();
        model.put("backlogs", backlogs);
        model.put("tasks", taskService.getTasksWithAssignmentForUserStory(0));
        model.put("cancelPath", currentReturnPathOrFallback(context, "/home"));

        context.render(templatePath, model);
    }

    @Override
    public void postCreateUserStory(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        try {
            UserStory userStory = createUserStoryFromForm(context, accountId);
            int newId = userStoryService.createUserStory(userStory);
            List<Integer> taskIds = parseTaskIds(context);
            if (newId > 0 && !taskIds.isEmpty()) {
                userStoryService.setTasksForUserStory(newId, taskIds);
            }
            String fallbackPath = "/backlog/" + userStory.backlogId();
            context.redirect(resolveReturnPathAndClear(context, fallbackPath));
        } catch (IllegalArgumentException e) {
            List<Backlog> backlogs = backlogService.getBacklogs();
            Map<String, Object> model = new HashMap<>();
            model.put("backlogs", backlogs);
            model.put("tasks", taskService.getTasksWithAssignmentForUserStory(0));
            model.put("cancelPath", currentReturnPathOrFallback(context, "/home"));
            model.put("showErrorMessage", true);
            context.render(templatePath, model);
        }
    }

    private List<Integer> parseTaskIds(Context context) {
        return context.formParamMap().getOrDefault("tasks", List.of()).stream()
                .map(s -> {
                    try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
                })
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    private UserStory createUserStoryFromForm(Context context, Integer accountId) {
        String titel = context.formParam("titel");
        String beschreibung = context.formParam("beschreibung");
        String backlogIdString = context.formParam("backlog");
        String statusString = context.formParam("status");
        String priorityString = context.formParam("priorities");

        int priorities;
        int backlogId;
        TaskStatus status;
        try {
            backlogId = Integer.parseInt(backlogIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
        try {
            status = TaskStatus.itoStatus(Integer.parseInt(statusString));
        } catch (Exception e) {
            status = TaskStatus.TODO;
        }

        try {
        priorities = Integer.parseInt(priorityString);
        if (priorities < 0 || priorities > 2) {
            priorities = 0;  
        }
        } catch (NumberFormatException e) {
        priorities = 0;  
        }


        UserStory userStory = new UserStory(-1, titel, beschreibung, accountId, backlogId, status, priorities, 0);
        validateUserStory(userStory);
        return userStory;
    }

    private void validateUserStory(UserStory userStory) {
        if (userStory.titel() == null || userStory.titel().isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void getEditUserStory(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        rememberReturnPath(context);

        int id;
        try {
            id = Integer.parseInt(context.pathParam("id"));
        } catch (NumberFormatException e) {
            context.status(400).result("Ungültige User-Story-ID");
            return;
        }

        Optional<UserStory> userStory = userStoryService.getUserStoryById(id);
        if (userStory.isEmpty()) {
            context.status(404).result("User Story nicht gefunden");
            return;
        }

        int productOwnerId = userStoryService.getProductOwnerByUserStoryId(id);
        boolean isProductOwner = (accountId == productOwnerId);
        

        UserStory us = userStory.get();
        List<Backlog> backlogs = backlogService.getBacklogs();
        Map<String, Object> model = new HashMap<>();

        int priorities = us.priorities();

        model.put("userStory", us);
        model.put("backlogs", backlogs);
        model.put("tasks", taskService.getTasksWithAssignmentForUserStory(id));
        model.put("statusIsTodo", us.status() == TaskStatus.TODO);
        model.put("statusIsInProgress", us.status() == TaskStatus.IN_PROGRESS);
        model.put("statusIsCompleted", us.status() == TaskStatus.COMPLETED);
        model.put("priorities", priorities);
        model.put("isProductOwner", isProductOwner);         
        model.put("priorityLow", priorities == 0);
        model.put("priorityMedium", priorities == 1);
        model.put("priorityHigh", priorities == 2);
        model.put("cancelPath", currentReturnPathOrFallback(context, "/backlog/" + us.backlogId()));

        context.render("templates/edit-user-story.mustache", model);
    }

    @Override
public void postEditUserStory(Context context) {
    Integer accountId = context.sessionAttribute("accountId");
    if (accountId == null) {
        context.redirect("/home");
        return;
    }

    int id;
    try {
        id = Integer.parseInt(context.pathParam("id"));
    } catch (NumberFormatException e) {
        context.status(400).result("Ungültige User-Story-ID");
        return;
    }

    Optional<UserStory> existing = userStoryService.getUserStoryById(id);
    if (existing.isEmpty()) {
        context.status(404).result("User Story nicht gefunden");
        return;
    }

    String titel = context.formParam("titel");
    String beschreibung = context.formParam("beschreibung");
    String backlogIdString = context.formParam("backlog");
    String statusString = context.formParam("status");
    String priorityString = context.formParam("priorities");
    
    int productOwnerId = userStoryService.getProductOwnerByUserStoryId(id);
    boolean isProductOwner = (accountId == productOwnerId);
    
    int backlogId;
    TaskStatus status;
    int priorities = existing.get().priorities();

    try {
        backlogId = Integer.parseInt(backlogIdString);
    } catch (NumberFormatException e) {
        backlogId = existing.get().backlogId();
    }
    
    try {
        status = TaskStatus.itoStatus(Integer.parseInt(statusString));
    } catch (Exception e) {
        status = existing.get().status();
    }

    if (isProductOwner && priorityString != null && !priorityString.isBlank()) {
        try {
            int newPriority = Integer.parseInt(priorityString);
            if (newPriority >= 0 && newPriority <= 2) {
                priorities = newPriority;
            }
        } catch (NumberFormatException e) {
        }
    }
    if (titel == null || titel.isBlank()) {
        List<Backlog> backlogs = backlogService.getBacklogs();
        UserStory us = existing.get();
        Map<String, Object> model = new HashMap<>();
        model.put("userStory", us);
        model.put("backlogs", backlogs);
        model.put("tasks", taskService.getTasksWithAssignmentForUserStory(id));
        model.put("statusIsTodo", us.status() == TaskStatus.TODO);
        model.put("statusIsInProgress", us.status() == TaskStatus.IN_PROGRESS);
        model.put("statusIsCompleted", us.status() == TaskStatus.COMPLETED);
        model.put("cancelPath", currentReturnPathOrFallback(context, "/backlog/" + us.backlogId()));
        model.put("showErrorMessage", true);
        model.put("priorities", priorities);
        model.put("isProductOwner", isProductOwner);
        model.put("priorityLow", priorities == 0);
        model.put("priorityMedium", priorities == 1);
        model.put("priorityHigh", priorities == 2);
        context.render("templates/edit-user-story.mustache", model);
        return;
    }
    UserStory updated = new UserStory(id, titel, beschreibung, existing.get().creatorId(), backlogId, status, priorities, 0);
    userStoryService.updateUserStory(updated);
    userStoryService.setTasksForUserStory(id, parseTaskIds(context));
    context.redirect(resolveReturnPathAndClear(context, "/backlog/" + backlogId));
}

}