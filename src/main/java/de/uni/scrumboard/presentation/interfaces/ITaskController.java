package de.uni.scrumboard.presentation.interfaces;

import io.javalin.http.Context;

public interface ITaskController {
    void getCreateTask(Context context);
    void postCreateTask(Context context);
    void getEditTask(Context context);
    void postEditTask (Context context);
    void SearchTask(Context context);
    void showMessage(Context context);
}