ALTER TABLE users (
    ADD COLUMN email VARCHAR(100);
    ADD CONSTRAINT unique_email UNIQUE (email);
);