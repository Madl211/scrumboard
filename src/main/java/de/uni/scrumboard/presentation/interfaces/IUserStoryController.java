package de.uni.scrumboard.presentation.interfaces;

import io.javalin.http.Context;

public interface IUserStoryController {

    void getCreateUserStory(Context context);

    void postCreateUserStory(Context context);

    void getEditUserStory(Context context);

    void postEditUserStory(Context context);

}
