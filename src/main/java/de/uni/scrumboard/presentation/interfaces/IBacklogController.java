package de.uni.scrumboard.presentation.interfaces;

import io.javalin.http.Context;

public interface IBacklogController {

    void getCreateBacklog(Context context);

    void postCreateBacklog(Context context);

    void getBacklogs(Context context);

    void getBacklog(Context context);

    void getLastUpdate(Context context);

}
