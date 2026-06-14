CREATE TABLE user_stories (
    id INTEGER PRIMARY KEY,
    titel VARCHAR(255) NOT NULL,
    beschreibung TEXT,
    creatorId INTEGER NOT NULL,
    backlogId INTEGER NOT NULL,
    FOREIGN KEY creatorId REFERENCES accounts(accountId),
    FOREIGN KEY backlogId REFERENCES backlogs(id)
);

CREATE TABLE user_story_tasks (
    userStoryId INTEGER NOT NULL,
    taskId INTEGER NOT NULL,
    PRIMARY KEY (userStoryId, taskId),
    FOREIGN KEY userStoryId REFERENCES user_stories(id),
    FOREIGN KEY taskId REFERENCES tasks(id)
);

