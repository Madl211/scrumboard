package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.uni.scrumboard.presentation.interfaces.IAuthController;
import de.uni.scrumboard.service.interfaces.IAccountService;
import de.uni.scrumboard.service.interfaces.IProfileService;
import io.javalin.http.Context;

public record AuthController(IAccountService accountService, IProfileService profileService)
        implements IAuthController {

    private static final String loginTemplate = "templates/login.mustache";
    private static final String registerTemplate = "templates/register.mustache";

    @Override
    public void getLogin(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId != null) {
            context.redirect("/home");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("email", "");
        context.render(loginTemplate, model);
    }

    @Override
    public void postLogin(Context context) {
        String email = context.formParam("email").trim();
        String password = context.formParam("password");

        if (email.isEmpty() || password.isEmpty()) {
            renderLogin(context, email, "Bitte füllen Sie E-Mail und Passwort aus.");
            return;
        }

        Optional<Integer> accountId = accountService.authenticate(email, password);
        if (accountId.isEmpty()) {
            renderLogin(context, email, "Ungültige E-Mail oder falsches Passwort.");
            return;
        }

        context.sessionAttribute("accountId", accountId.get());
        context.redirect("/home");
    }

    @Override
    public void getRegister(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId != null) {
            context.redirect("/profile");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("email", "");
        model.put("username", "");
        context.render(registerTemplate, model);
    }

    @Override
    public void postRegister(Context context) {
        String email = context.formParam("email").trim();
        String username = context.formParam("username").trim();
        String password = context.formParam("password");
        String confirmPassword = context.formParam("confirmPassword");

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            renderRegister(context, email, username, "Bitte füllen Sie alle Felder aus.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            renderRegister(context, email, username, "Bitte geben Sie eine gültige E-Mail-Adresse an.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            renderRegister(context, email, username, "Die Passwörter stimmen nicht überein.");
            return;
        }

        Optional<Integer> accountId = accountService.register(email, password, username);
        if (accountId.isEmpty()) {
            renderRegister(context, email, username, "Diese E-Mail ist bereits registriert.");
            return;
        }

        context.sessionAttribute("accountId", accountId.get());
        context.redirect("/profile");
    }

    @Override
    public void logout(Context context) {
        var session = context.req().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        context.redirect("/login");
    }

    private void renderLogin(Context context, String email, String errorMessage) {
        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("errorMessage", errorMessage);
        context.render(loginTemplate, model);
    }

    private void renderRegister(Context context, String email, String username, String errorMessage) {
        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("username", username);
        model.put("errorMessage", errorMessage);
        context.render(registerTemplate, model);
    }
}
