CREATE TABLE persistent_logins (
    series VARCHAR(64) NOT NULL,
    username VARCHAR(50) NOT NULL,
    token VARCHAR(64) NOT NULL,
    last_used DATETIME,
    PRIMARY KEY (series)
);