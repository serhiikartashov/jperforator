CREATE TABLE IF NOT EXISTS users (
    id      VARCHAR PRIMARY KEY,
    name    VARCHAR         NOT NULL,
    email   VARCHAR UNIQUE  NOT Null
);