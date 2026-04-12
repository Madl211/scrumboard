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
        int accountId = context.sessionAttribute("accountId"); // -> Nullpointer!

        Profile result = profileService.getProfile(accountId);
        Map<String, Object> resultMap = convertProfileToMap(result);

        context.render(templatePath, resultMap);
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

    @Override
    public void saveProfile(Context context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveProfile'");
    }

}
