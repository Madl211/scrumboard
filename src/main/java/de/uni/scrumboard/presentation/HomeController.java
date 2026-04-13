package de.uni.scrumboard.presentation;

import io.javalin.http.Context;

public record HomeController() implements IHomeController {
    private static final String homeTemplate = "templates/home.mustache";

    @Override
    public void showHome(Context context) {
        context.render(homeTemplate);
    }
}
