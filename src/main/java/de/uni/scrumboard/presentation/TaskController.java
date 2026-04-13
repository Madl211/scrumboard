package de.uni.scrumboard.presentation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.presentation.interfaces.ITaskController;
import de.uni.scrumboard.service.interfaces.IAccountService;
import de.uni.scrumboard.service.interfaces.ITaskService;
import io.javalin.http.Context;

public record TaskController(ITaskService taskService, IAccountService accountService) implements ITaskController {

    private static final String createTaskTemplate = "templates/create-task.mustache";

    @Override
    public void getCreateTask(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        List<Account> accounts = accountService.getAllAccounts();
        Map<String, Object> model = new HashMap<>();
        model.put("accounts", accounts);
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

        taskService.createTask(title, description, assigneeIds, accountId, startDate, dueDate, estimatedTime);
        context.redirect("/home");
    }
}