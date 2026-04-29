package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.scrumboard.data.Backlog;
import de.uni.scrumboard.data.UserStory;
import de.uni.scrumboard.presentation.interfaces.IUserStoryController;
import de.uni.scrumboard.service.interfaces.IBacklogService;
import de.uni.scrumboard.service.interfaces.IUserStoryService;
import io.javalin.http.Context;

public class UserStoryController implements IUserStoryController {

    private static final String templatePath = "templates/create-user-story.mustache";

    private IUserStoryService userStoryService;
    private IBacklogService backlogService;

    public UserStoryController(IUserStoryService userStoryService, IBacklogService backlogService) {
        this.userStoryService = userStoryService;
        this.backlogService = backlogService;
    }

    @Override
    public void getCreateUserStory(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        List<Backlog> backlogs = backlogService.getBacklogs();
        Map<String, Object> model = new HashMap<>();
        model.put("backlogs", backlogs);

        context.render(templatePath, model);
    }

    @Override
    public void postCreateUserStory(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        try {
            UserStory userStory = createUserStoryFromForm(context, accountId);
            userStoryService.createUserStory(userStory);
            context.redirect("/home");
        } catch (IllegalArgumentException e) {
            List<Backlog> backlogs = backlogService.getBacklogs();
            Map<String, Object> model = new HashMap<>();
            model.put("backlogs", backlogs);
            model.put("showErrorMessage", true);
            context.render(templatePath, model);
        }
    }

    private UserStory createUserStoryFromForm(Context context, Integer accountId) {
        String titel = context.formParam("titel");
        String beschreibung = context.formParam("beschreibung");
        String backlogIdString = context.formParam("backlog");
        int backlogId;
        try {
            backlogId = Integer.parseInt(backlogIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }

        UserStory userStory = new UserStory(-1, titel, beschreibung, accountId, backlogId);
        validateUserStory(userStory);
        return userStory;
    }

    private void validateUserStory(UserStory userStory) {
        if (userStory.titel() == null || userStory.titel().isBlank()) {
            throw new IllegalArgumentException();
        }
    }

}
