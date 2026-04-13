package de.uni.scrumboard.presentation.interfaces;

import io.javalin.http.Context;

public interface ITaskController {
    void getCreateTask(Context context);
    void postCreateTask(Context context);
}