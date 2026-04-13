package de.uni.scrumboard.presentation.interfaces;

import io.javalin.http.Context;

public interface IProfileController {

    public void getProfile(Context context);

    public void saveProfile(Context context);

}
