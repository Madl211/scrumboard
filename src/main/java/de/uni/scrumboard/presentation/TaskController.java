package de.uni.scrumboard.presentation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.Task;
import de.uni.scrumboard.data.enums.TaskStatus;
import de.uni.scrumboard.presentation.interfaces.ITaskController;
import de.uni.scrumboard.service.interfaces.IAccountService;
import de.uni.scrumboard.service.interfaces.IBacklogService;
import de.uni.scrumboard.service.interfaces.ITaskService;
import io.javalin.http.Context;

public record TaskController(ITaskService taskService, IAccountService accountService, IBacklogService backlogService)
        implements ITaskController {

    private static final String createTaskTemplate = "templates/create-task.mustache";
    //für route zum korrekten
    private static final String taskReturnToSessionKey = "taskReturnTo"; 

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
                && !candidatePath.startsWith("/create-task")
                && !candidatePath.startsWith("/edit-task")) {
            context.sessionAttribute(taskReturnToSessionKey, candidatePath);
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
        String returnPath = context.sessionAttribute(taskReturnToSessionKey);
        context.sessionAttribute(taskReturnToSessionKey, null);

        if (isSafeInternalPath(returnPath)) {
            return backlogOrHome(returnPath, fallbackPath);
        }
        return backlogOrHome(null, fallbackPath);
    }

    private String currentReturnPathOrFallback(Context context, String fallbackPath) {
        String returnPath = context.sessionAttribute(taskReturnToSessionKey);
        if (isSafeInternalPath(returnPath)) {
            return backlogOrHome(returnPath, fallbackPath);
        }
        return backlogOrHome(null, fallbackPath);
    }

    @Override
    public void getCreateTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        rememberReturnPath(context);

        List<Account> accounts = accountService.getAllAccounts();
        List<Backlog> backlogs = backlogService.getBacklogs();
        Map<String, Object> model = new HashMap<>();
        model.put("accounts", accounts);
        model.put("backlogs", backlogs);
        model.put("cancelPath", currentReturnPathOrFallback(context, "/home"));
        context.render(createTaskTemplate, model);
    }

    @Override
    public void postCreateTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        String title = context.formParam("title");
        String description = context.formParam("description");
        String startDateStr = context.formParam("startDate");
        String dueDateStr = context.formParam("dueDate");
        String estimatedTimeStr = context.formParam("estimatedTime");
        String[] assigneeIdsStr = context.formParamMap().getOrDefault("assignees", List.of()).toArray(new String[0]);

        List<Integer> assigneeIds = Arrays.stream(assigneeIdsStr)
                .map(Integer::parseInt)
                .toList();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date startDate = null;
        java.util.Date dueDate = null;
        int estimatedTime = 0;

        int statusInt = Integer.valueOf(context.formParam("status"));
        if (statusInt < 0 || statusInt > 4)
            statusInt = 0;

        TaskStatus status = TaskStatus.itoStatus(statusInt);

        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = dateFormat.parse(startDateStr);
            }
            if (dueDateStr != null && !dueDateStr.isEmpty()) {
                dueDate = dateFormat.parse(dueDateStr);
            }
            if (estimatedTimeStr != null && !estimatedTimeStr.isEmpty()) {
                estimatedTime = Integer.parseInt(estimatedTimeStr);
            }
        } catch (ParseException | NumberFormatException e) {
            context.status(400).result("Invalid date or number format");
            return;
        }

        String backlogIdStr = context.formParam("backlog");
        Integer selectedBacklogId = null;
        Task created = taskService.createTask(title, description, assigneeIds, accountId, startDate, dueDate,
                estimatedTime, status);
        if (backlogIdStr != null && !backlogIdStr.isEmpty()) {
            try {
                selectedBacklogId = Integer.parseInt(backlogIdStr);
                backlogService.addTaskToBacklog(selectedBacklogId, created.taskId());
            } catch (NumberFormatException ignored) {
            }
        }

        String fallbackPath = selectedBacklogId != null ? "/backlog/" + selectedBacklogId : "/home";
        context.redirect(resolveReturnPathAndClear(context, fallbackPath));
    }

    @Override
    public void getEditTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        rememberReturnPath(context);

        int taskId = Integer.parseInt(context.pathParam("taskId"));
        Optional<Task> task = taskService.getTaskById(taskId);
        if (task.isEmpty()) {
            context.status(404).result("Task not found");
            return;
        }
        List<Backlog> backlogs = backlogService.getBacklogs();

        Task t = task.get();
        Map<String, Object> model = new HashMap<>();
        model.put("task", t);
        model.put("accounts", taskService.getAccountsWithAssignmentForTask(taskId));
        model.put("statusIsTodo", t.status() == TaskStatus.TODO);
        model.put("statusIsInProgress", t.status() == TaskStatus.IN_PROGRESS);
        model.put("statusIsCompleted", t.status() == TaskStatus.COMPLETED);
        model.put("statusIsReview", t.status() == TaskStatus.REVIEW);
        model.put("statusIsTest", t.status() == TaskStatus.TEST);
        model.put("backlogs", backlogs);
        String editFallbackPath = backlogService.getBacklogIdByTaskId(taskId)
            .map(id -> "/backlog/" + id)
            .orElse("/home");
        model.put("cancelPath", currentReturnPathOrFallback(context, editFallbackPath));

        context.render("templates/edit-task.mustache", model);
        // button zum kriegen der Daten über die Task
        // Status updaten
        // wenn status completed == 2
        // dann finishtime auf current gesetzt, und dann muss finishtime-Starttime mit
        // estimatedtime verglichen werden
        // Vergleiche der einzelnen estimatedtime mit Actualtime mit einzelne Tasks per
        // ID
    }

    @Override
    public void postEditTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/home");
            return;
        }

        int taskId = Integer.parseInt(context.pathParam("taskId"));
        Optional<Task> task = taskService.getTaskById(taskId);
        if (task.isEmpty()) {
            context.status(404).result("Task not found");
            return;
        }

        String title = context.formParam("title");
        String description = context.formParam("description");
        String startDateStr = context.formParam("startDate");
        String dueDateStr = context.formParam("dueDate");
        String estimatedTimeStr = context.formParam("estimatedTime");
        String[] assigneeIdsStr = context.formParamMap().getOrDefault("assignees", List.of()).toArray(new String[0]);

        List<Integer> assigneeIds = Arrays.stream(assigneeIdsStr)
                .map(Integer::parseInt)
                .toList();
        List<Account> assignees = accountService.getAllAccounts().stream()
                .filter(a -> assigneeIds.contains(a.accountId()))
                .toList();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date startDate = null;
        java.util.Date dueDate = null;
        int estimatedTime = 0;

        int statusInt = Integer.valueOf(context.formParam("status"));
        if (statusInt < 0 || statusInt > 4)
            statusInt = 0;

        TaskStatus status = TaskStatus.itoStatus(statusInt);
        try {
            if (startDateStr != null && !startDateStr.isEmpty())
                startDate = dateFormat.parse(startDateStr);
            if (dueDateStr != null && !dueDateStr.isEmpty())
                dueDate = dateFormat.parse(dueDateStr);
            if (estimatedTimeStr != null && !estimatedTimeStr.isEmpty())
                estimatedTime = Integer.parseInt(estimatedTimeStr);
        } catch (ParseException | NumberFormatException e) {
            context.status(400).result("Invalid date or number format");
            return;
        }

        Task existing = task.get();
        Task updated = new Task(
            existing.taskId(),
            title,
            description,
            assignees,
            existing.creator(),
            startDate,
            dueDate,
            existing.created(),
            estimatedTime,
            existing.actualTime(),
            status,
            existing.lastUpdated()
        );

        String backlogIdStr = context.formParam("backlog");
        Integer newBacklogId = null;

        try {
            if (backlogIdStr != null && !backlogIdStr.isBlank()) {
                newBacklogId = Integer.parseInt(backlogIdStr);
            }
        } catch (NumberFormatException e) {
            context.status(400).result("Invalid backlog id");
            return;
        }

        Optional<Integer> oldBacklogId = backlogService.getBacklogIdByTaskId(updated.taskId());

        if (oldBacklogId.isPresent() && (newBacklogId == null || !oldBacklogId.get().equals(newBacklogId))) {
            backlogService.removeTaskFromBacklog(oldBacklogId.get(), updated.taskId());
        }
        if (newBacklogId != null && (oldBacklogId.isEmpty() || !oldBacklogId.get().equals(newBacklogId))) {
            backlogService.addTaskToBacklog(newBacklogId, updated.taskId());
        }
        

        taskService.updateTask(updated);
        String fallbackPath = oldBacklogId
            .map(id -> "/backlog/" + id)
            .orElse("/home");
        if (newBacklogId != null) {
            fallbackPath = "/backlog/" + newBacklogId;
        }
        context.redirect(resolveReturnPathAndClear(context, fallbackPath));

        // button zum kriegen der Daten über die Task
        // Status updaten
        // wenn status completed == 2
        // dann finishtime auf current gesetzt, und dann muss finishtime-Starttime mit
        // estimatedtime verglichen werden
        // Vergleiche der einzelnen estimatedtime mit Actualtime mit einzelne Tasks per
        // ID

    }
    @Override
    public void SearchTask(Context context){
        String filterParam = context.queryParam("filter");
        int filter = (filterParam == null || filterParam.isEmpty())
        ? -1
        : Integer.parseInt(filterParam);
        String search = context.queryParam("search");
        List<Task> tasks = taskService.searchTasks(search, filter);
        Map<String, Object> model = new HashMap<>();
        model.put("tasks", tasks);
        context.render("templates/search.mustache", model);
    }
    @Override
    public void showMessage(Context context){
        List<Task> tasks = taskService.checkwhendue();
        Map<String, Object> model = new HashMap<>();
        model.put("tasks", tasks);
        context.render("templates/messages.mustache", model);
    }
}