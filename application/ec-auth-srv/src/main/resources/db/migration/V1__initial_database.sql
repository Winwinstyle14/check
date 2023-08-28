CREATE TABLE groups
(
    id         serial primary key,
    name       varchar(63)                            not null,
    status     int4         default 1                 not null,
    created_at timestamp(6) default current_timestamp not null,
    created_by integer,
    updated_at timestamp(6) default current_timestamp not null,
    updated_by integer
);

CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    username   VARCHAR(63)                            NOT NULL,
    email      VARCHAR(255) UNIQUE                    NOT NULL,
    password   VARCHAR(60)                            NOT NULL,
    phone      VARCHAR(15),
    group_id   INTEGER,
    status     int4         DEFAULT 1                 NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by INTEGER,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by INTEGER,

    CONSTRAINT fk_users_groups FOREIGN KEY (group_id)
        REFERENCES groups (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

