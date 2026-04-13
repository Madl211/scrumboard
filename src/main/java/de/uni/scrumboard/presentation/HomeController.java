package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.Task;
import de.uni.scrumboard.presentation.interfaces.IHomeController;
import de.uni.scrumboard.service.interfaces.IAccountService;
import de.uni.scrumboard.service.interfaces.ITaskService;
import io.javalin.http.Context;

public record HomeController(IAccountService accountService, ITaskService taskService) implements IHomeController {
    private static final String homeTemplate = "templates/home.mustache";

    @Override
    public void showHome(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        List<Account> accounts = accountService.getAllAccounts();
        List<Task> tasks = taskService.getAllTasks();

        Map<String, Object> model = new HashMap<>();
        model.put("accounts", accounts);
        model.put("tasks", tasks);
        context.render(homeTemplate, model);
    }
}
