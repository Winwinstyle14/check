CREATE EXTENSION IF NOT EXISTS ltree;

CREATE TABLE organizations
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(600)                           NOT NULL,
    short_name VARCHAR(63)                            NOT NULL,
    code       VARCHAR(63) UNIQUE                     NOT NULL,
    email      VARCHAR(255)                           NOT NULL,
    phone      VARCHAR(15)                            NOT NULL,
    fax        VARCHAR(15),
    status     int4         DEFAULT 1                 NOT NULL,
    parent_id  INTEGER,
    path       LTREE,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by INTEGER,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by INTEGER,

    CONSTRAINT fk_organizations_organizations
        FOREIGN KEY (parent_id)
            REFERENCES organizations (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL
);

CREATE INDEX ix_organizations_path_gist ON organizations USING GIST (path);

CREATE TABLE types
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(255)                           NOT NULL,
    code            VARCHAR(63) UNIQUE                     NOT NULL,
    status          int4         DEFAULT 1                 NOT NULL,
    ordering        INTEGER      DEFAULT 1                 NOT NULL,
    organization_id INTEGER                                NOT NULL,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by      INTEGER,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by      INTEGER,

    CONSTRAINT fk_types_organizations
        FOREIGN KEY (organization_id)
            REFERENCES organizations (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
);

create table customers
(
    id              serial primary key,
    name            varchar(600)                           not null,
    email           varchar(191) unique                    not null,
    password        varchar(60)                            not null,
    phone           varchar(15)                            not null,
    phone_sign      varchar(15),
    phone_tel       smallint     default 1,
    birthday        date,
    sign_image      json,
    hsm_name        varchar(255),
    status          int4         default 1                 not null,
    type_id         integer                                not null,
    organization_id integer                                not null,
    created_at      timestamp(6) default current_timestamp not null,
    created_by      integer,
    updated_at      timestamp(6) default current_timestamp not null,
    updated_by      integer,

    constraint fk_customers_types foreign key (type_id)
        references types (id)
        on update cascade
        on delete restrict,

    constraint fk_customers_organizations foreign key (organization_id)
        references organizations (id)
        on update cascade
        on delete restrict
);