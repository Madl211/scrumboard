package de.uni.scrumboard;

import de.uni.scrumboard.data.ProfileRepository;
import de.uni.scrumboard.presentation.ProfileController;
import de.uni.scrumboard.service.ProfileService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;

public class App {

    private static ProfileController profileController;
    private static ProfileService profileService;
    private static ProfileRepository profileRepository;

    public static void main(String[] args) {
        bootstrapApplication();

        Javalin.create(config -> config.fileRenderer(new JavalinMustache()))
            .get("/profile", profileController::getProfile)
        .start(8080);
    }

    private static void bootstrapApplication() {
        profileRepository = new ProfileRepository();
        profileService = new ProfileService(profileRepository);
        profileController = new ProfileController(profileService);
    }

}
