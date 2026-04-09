package de.uni.scrumboard.presentation;

import io.javalin.http.Context;

public interface IProfileController {

    public void getProfile(Context context);

    public void saveProfile(Context context);

}
