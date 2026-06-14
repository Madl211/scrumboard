CREATE TABLE accounts (
    accountId int AUTO_INCREMENT primary key,
    email VARCHAR(255) not null unique,
    passwordHash VARCHAR(255) not null,
    username VARCHAR(255)
)


CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(account_id) REFERENCES accounts(accountId)
);

-- Index für schnellere Suche
CREATE INDEX IF NOT EXISTS idx_reset_token ON password_reset_tokens(token);