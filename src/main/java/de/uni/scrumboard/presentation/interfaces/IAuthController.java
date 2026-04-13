package de.uni.scrumboard.presentation.interfaces;

import io.javalin.http.Context;

public interface IAuthController {

    public void getLogin(Context context);

    public void postLogin(Context context);

    public void getRegister(Context context);

    public void postRegister(Context context);

    public void logout(Context context);
}
