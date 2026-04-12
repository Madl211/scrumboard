CREATE TABLE accounts (
    accountId int AUTO_INCREMENT primary key,
    email VARCHAR(255) not null unique,
    passwordHash VARCHAR(255) not null,
    username VARCHAR(255)
)