CREATE TABLE IF NOT EXISTS "tab"
(
    id       uuid    NOT NULL,
    name     varchar NOT NULL,
    password varchar     DEFAULT NULL,
    created  timestamptz DEFAULT now(),
    updated  timestamptz DEFAULT now(),
    user_id  uuid    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT owner_id FOREIGN KEY (user_id) REFERENCES "user" (id)
)