CREATE TABLE user_stories (
    id INTEGER PRIMARY KEY,
    titel VARCHAR(255) NOT NULL,
    beschreibung TEXT,
    creatorId INTEGER NOT NULL,
    backlogId INTEGER NOT NULL,
    FOREIGN KEY creatorId REFERENCES accounts(accountId),
    FOREIGN KEY backlogId REFERENCES backlogs(id)
);

