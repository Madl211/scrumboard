package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.Map;

import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.presentation.interfaces.IBacklogController;
import de.uni.scrumboard.service.interfaces.IBacklogService;
import io.javalin.http.Context;

public class BacklogController implements IBacklogController {

    private final static String templatePath = "templates/create-backlog.mustache";

    private IBacklogService backlogService;

    public BacklogController(IBacklogService backlogService) {
        this.backlogService = backlogService;
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

}
