package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.Map;
import de.uni.scrumboard.data.Profile;
import de.uni.scrumboard.service.ProfileService;
import io.javalin.http.Context;

public record ProfileController(ProfileService profileService) implements IProfileController {

    private final static String templatePath = "templates/profile.mustache";

    @Override
    public void getProfile(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        Profile result = profileService.getProfile(accountId);
        Map<String, Object> resultMap = convertProfileToMap(result);

        context.render(templatePath, resultMap);
    }

    @Override
    public void saveProfile(Context context) {
        Integer accountId = context.sessionAttribute("accountId");
        if (accountId == null) {
            context.redirect("/login");
            return;
        }

        Profile profile = new Profile(
                context.formParam("name"),
                context.formParam("eMail"),
                context.formParam("telefonnummer"),
                context.formParam("postleitzahl"),
                context.formParam("ort"),
                context.formParam("strasse"),
                context.formParam("hausnummer"));

        profileService.saveProfile(profile, accountId);
        context.redirect("/profile");
    }

    private Map<String, Object> convertProfileToMap(Profile profile) {
        Map<String, Object> map = new HashMap<>();

        map.put("name", profile.name());
        map.put("eMail", profile.eMailAddr());
        map.put("telefonnummer", profile.telNr());
        map.put("postleitzahl", profile.plz());
        map.put("ort", profile.ort());
        map.put("strasse", profile.strasse());
        map.put("hausnummer", profile.hausNr());

        return map;
    }

}
