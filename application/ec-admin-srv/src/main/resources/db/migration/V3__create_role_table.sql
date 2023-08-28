create table roles
(
    id              serial primary key,
    name            varchar(255)                           not null,
    code            varchar(255) unique                    not null,
    description     varchar(255),
    status          int4         default 1                 not null,
    organization_id integer                                not null,
    created_at      timestamp(6) default current_timestamp not null,
    created_by      integer,
    updated_at      timestamp(6) default current_timestamp not null,
    updated_by      integer,

    constraint fk_roles_organizations
        foreign key (organization_id)
            references organizations (id)
            on update cascade
            on delete cascade
);

create table permissions
(
    id         serial primary key,
    code       varchar(255)                           not null,
    status     int4         default 1                 not null,
    role_id    integer                                not null,
    created_at timestamp(6) default current_timestamp not null,
    created_by integer,
    updated_at timestamp(6) default current_timestamp not null,
    updated_by integer,

    constraint fk_permissions_roles
        foreign key (role_id)
            references roles (id)
            on update cascade
            on delete cascade
);