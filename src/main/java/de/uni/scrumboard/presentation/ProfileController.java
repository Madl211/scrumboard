package de.uni.scrumboard.presentation;

import java.util.HashMap;
import java.util.Map;
import de.uni.scrumboard.data.Profile;
import de.uni.scrumboard.service.ProfileService;
import io.javalin.http.Context;

public class ProfileController implements IProfileController {

    private final static String templatePath = "templates/profile.mustache";

    private ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public void getProfile(Context context) {
        Profile result = profileService.getProfile();
        Map<String, Object> resultMap = convertProfileToMap(result);

        context.render(templatePath, resultMap);
    }

    private Map<String,Object> convertProfileToMap(Profile profile) {
        Map<String, Object> map = new HashMap<>();

        map.put("name", profile.getName());
        map.put("eMail", profile.getEMail());
        map.put("telefonnummer", profile.getTelefonnummer());
        map.put("postleitzahl", profile.getPostleitzahl());
        map.put("ort", profile.getOrt());
        map.put("strasse", profile.getStrasse());
        map.put("hausnummer", profile.getHausnummer());

        return map;
    }

    @Override
    public void saveProfile(Context context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveProfile'");
    }

}
