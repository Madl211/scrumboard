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
    //für route zurück zum korrekten Backlog
    private static final String userStoryReturnToSessionKey = "userStoryReturnTo";

    private IUserStoryService userStoryService;
    private IBacklogService backlogService;

    public UserStoryController(IUserStoryService userStoryService, IBacklogService backlogService) {
        this.userStoryService = userStoryService;
        this.backlogService = backlogService;
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

        if (isSafeInternalPath(candidatePath) && !candidatePath.startsWith("/create-user-story")) {
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
            userStoryService.createUserStory(userStory);
            String fallbackPath = "/backlog/" + userStory.backlogId();
            context.redirect(resolveReturnPathAndClear(context, fallbackPath));
        } catch (IllegalArgumentException e) {
            List<Backlog> backlogs = backlogService.getBacklogs();
            Map<String, Object> model = new HashMap<>();
            model.put("backlogs", backlogs);
            model.put("cancelPath", currentReturnPathOrFallback(context, "/home"));
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
