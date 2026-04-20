CREATE TABLE tasks (
    taskId int AUTO_INCREMENT primary key,
    title VARCHAR(255) not null,
    description TEXT,
    creatorId int not null,
    startDate DATE,
    dueDate DATE,
    createdDate DATE not null,
    estimatedTime int,
    actualTime int,
    status int,
    foreign key (creatorId) references accounts(accountId)
);

CREATE TABLE task_assignees (
    taskId int not null,
    accountId int not null,
    primary key (taskId, accountId),
    foreign key (taskId) references tasks(taskId) on delete cascade,
    foreign key (accountId) references accounts(accountId) on delete cascade
);