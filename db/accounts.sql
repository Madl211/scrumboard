CREATE TABLE accounts (
    accountId int AUTO_INCREMENT primary key,
    email VARCHAR(255) not null unique,
    password VARCHAR(255) not null
)