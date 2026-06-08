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

    @Override
    public void getCreateTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        List<Account> accounts = accountService.getAllAccounts();
        List<Backlog> backlogs = backlogService.getBacklogs();
        Map<String, Object> model = new HashMap<>();
        model.put("accounts", accounts);
        model.put("backlogs", backlogs);
        context.render(createTaskTemplate, model);
    }

    @Override
    public void postCreateTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
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
        if (statusInt < 0 || statusInt > 3)
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
        Task created = taskService.createTask(title, description, assigneeIds, accountId, startDate, dueDate,
                estimatedTime, status);
        if (backlogIdStr != null && !backlogIdStr.isEmpty()) {
            try {
                int backlogId = Integer.parseInt(backlogIdStr);
                backlogService.addTaskToBacklog(backlogId, created.taskId());
            } catch (NumberFormatException ignored) {
            }
        }
        context.redirect("/home");
    }

    @Override
    public void getEditTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

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
        model.put("backlogs", backlogs);

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
            context.redirect("/login");
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
        if (statusInt < 0 || statusInt > 3)
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
                existing.actualTime(), status);

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
        context.redirect("/home");

        // button zum kriegen der Daten über die Task
        // Status updaten
        // wenn status completed == 2
        // dann finishtime auf current gesetzt, und dann muss finishtime-Starttime mit
        // estimatedtime verglichen werden
        // Vergleiche der einzelnen estimatedtime mit Actualtime mit einzelne Tasks per
        // ID

    }

}