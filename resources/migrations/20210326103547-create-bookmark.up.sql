CREATE TABLE IF NOT EXISTS "bookmark"
(
    id      uuid    NOT NULL,
    title   varchar NOT NULL,
    url     varchar NOT NULL,
    image   bytea,
    created timestamptz DEFAULT now(),
    updated timestamptz DEFAULT now(),
    user_id uuid    NOT NULL,
    tab_id  uuid    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT owner_id FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT owner_tab FOREIGN KEY (tab_id) REFERENCES "tab" (id)
)