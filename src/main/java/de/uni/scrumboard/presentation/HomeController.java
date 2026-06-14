package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.List;

import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.presentation.interfaces.IHomeController;
import de.uni.scrumboard.service.interfaces.IBacklogService;
import io.javalin.http.Context;

public record HomeController(IBacklogService backlogService) implements IHomeController {
    private static final String homeTemplate = "templates/home.mustache";

    @Override
    public void showHome(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        List<Backlog> backlogs = backlogService.getBacklogs();
        HashMap<String, Object> model = new HashMap<>();
        model.put("backlogs", backlogs);
        context.render(homeTemplate, model);
    }

}
