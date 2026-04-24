CREATE TABLE backlogs (
    id INTEGER PRIMARY KEY,
    bezeichnung VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE backlog_tasks (
    backlogId INTEGER NOT NULL,
    taskId INTEGER NOT NULL,
    PRIMARY KEY(backlogId, taskId),
    FOREIGN KEY(backlogId) REFERENCES backlogs(id),
    FOREIGN KEY(taskId) REFERENCES tasks(taskId)
);
