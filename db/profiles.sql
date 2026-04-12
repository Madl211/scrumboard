CREATE TABLE profiles (
    accountId int primary key,
    name text,
    eMail text,
    telefonnummer text,
    postleitzahl text,
    ort text,
    strasse text,
    hausnummer text,
    foreign key (accountId) references accounts(accountId) on delete cascade
)