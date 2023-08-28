-- Loại hợp đồng
CREATE TABLE types
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(255)                           NOT NULL,
    code            VARCHAR(63) UNIQUE                     NOT NULL,
    organization_id INTEGER                                NOT NULL,
    status          INT4         DEFAULT 1                 NOT NULL,
    ordering        INTEGER      DEFAULT 1                 NOT NULL,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by      INTEGER                                NOT NULL,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by      INTEGER                                NOT NULL
);

-- Thông tin hợp đồng
create table contracts
(
    id              serial primary key,
    name            varchar(191)                           not null,
    code            varchar(31) unique                     not null,
    contract_no     varchar(191)                           not null,
    sign_time       timestamp(6),
    sign_order      integer      default 1                 not null,
    alias_url       varchar(191),
    notes           varchar(2000),
    ref_id          integer,
    type_id         integer,
    customer_id     integer                                not null,
    organization_id integer                                not null,
    ui_config       jsonb,
    is_template     bool         default false             not null,
    status          int4         default 1                 not null,
    created_at      timestamp(6) default current_timestamp not null,
    created_by      integer                                not null,
    updated_at      timestamp(6) default current_timestamp not null,
    updated_by      integer                                not null,

    constraint fk_contracts_types
        foreign key (type_id) references types (id)
            on update cascade
            on delete cascade,

    constraint fk_contracts_ref_id_contracts
        foreign key (ref_id) references contracts (id)
            on update cascade
            on delete set null
);

-- Thành phần tham gia ký hợp đồng
CREATE TABLE participants
(
    id          serial primary key,
    name        varchar(255)                           not null,
    type        smallint                               not null,
    ordering    integer                                not null,
    contract_id integer                                not null,
    status      int4         default 1                 not null,
    created_at  timestamp(6) default current_timestamp not null,
    created_by  integer                                not null,
    updated_at  timestamp(6) default current_timestamp not null,
    updated_by  integer                                not null,

    constraint fk_participants_contracts foreign key (contract_id)
        references contracts (id)
        on update cascade
        on delete cascade
);

-- Người xử lý hợp đồng
create table recipients
(
    id             serial primary key,
    name           varchar(63)                            not null,
    email          varchar(191)                           not null,
    phone          varchar(15),
    role           integer                                not null,
    username       varchar(63),
    password       varchar(60),
    ordering       integer      default 1                 not null,
    status         int4         default 1                 not null,
    from_at        timestamp(6),
    due_at         timestamp(6),
    sign_at        timestamp(6),
    process_at     timestamp(6),
    sign_type      jsonb,
    notify_type    jsonb,
    remind         integer,
    remind_date    timestamp(6),
    remind_message varchar(600),
    reason_reject  varchar(600),
    participant_id integer                                not null,
    created_at     timestamp(6) default current_timestamp not null,
    created_by     integer                                not null,
    updated_at     timestamp(6) default current_timestamp not null,
    updated_by     integer                                not null,

    constraint fk_recipients_participants
        foreign key (participant_id) references participants (id)
            on update cascade
            on delete cascade
);

-- Tài liệu liên quan tới hợp đồng
create table documents
(
    id          serial primary key,
    name        varchar(255)                           not null,
    type        int4                                   not null,
    path        varchar(255)                           not null,
    filename    varchar(255)                           not null,
    bucket      varchar(255)                           not null,
    internal    integer,
    ordering    integer                                not null,
    status      int4         default 1                 not null,
    contract_id integer                                not null,
    created_at  timestamp(6) default current_timestamp not null,
    created_by  integer                                not null,
    updated_at  timestamp(6) default current_timestamp not null,
    updated_by  integer                                not null,

    constraint fk_documents_contracts
        foreign key (contract_id) references contracts (id)
            on update cascade
            on delete cascade
);

-- Trường dữ liệu
create table fields
(
    id           serial primary key,
    name         varchar(63)                            not null,
    type         smallint                               not null,
    value        varchar(255),
    font         varchar(63)                            not null,
    font_size    smallint                               not null,
    page         smallint                               not null,
    box_x        float4                                 not null,
    box_y        float4                                 not null,
    box_w        float4                                 not null,
    box_h        float4                                 not null,
    required     smallint                               not null,
    document_id  integer                                not null,
    contract_id  integer                                not null,
    recipient_id integer,
    status       int4         default 1                 not null,
    created_at   timestamp(6) default current_timestamp not null,
    created_by   integer                                not null,
    updated_at   timestamp(6) default current_timestamp not null,
    updated_by   integer                                not null,

    constraint fk_contract_fields_documents
        foreign key (document_id) references documents (id)
            on update cascade
            on delete restrict,

    constraint fk_contract_fields_contracts
        foreign key (contract_id) references contracts (id)
            on update cascade
            on delete restrict,

    constraint fk_contract_fields_recipient
        foreign key (recipient_id) references recipients (id)
            on update cascade
            on delete cascade
);

-- Quyền xử lý hợp đồng
CREATE TABLE permissions
(
    id           SERIAL PRIMARY KEY,
    start_at     TIMESTAMP(6)                           NOT NULL,
    expire_at    TIMESTAMP(6)                           NOT NULL,
    type         SMALLINT                               NOT NULL,
    status       INT4         DEFAULT 1                 NOT NULL,
    contract_id  INTEGER                                NOT NULL,
    recipient_id INTEGER                                NOT NULL,
    created_at   TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by   INTEGER                                NOT NULL,
    updated_at   TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by   INTEGER                                NOT NULL,

    CONSTRAINT fk_permissions_contracts
        FOREIGN KEY (contract_id) REFERENCES contracts (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    CONSTRAINT fk_permissions_recipients
        FOREIGN KEY (recipient_id) REFERENCES recipients (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

-- Chia sẻ thông tin hợp đồng
CREATE TABLE shares
(
    id          SERIAL PRIMARY KEY,
    email       VARCHAR(191)                           NOT NULL,
    start_at    TIMESTAMP(6)                           NOT NULL,
    expire_at   TIMESTAMP(6)                           NOT NULL,
    share_type  SMALLINT                               NOT NULL,
    token       VARCHAR(16)                            NOT NULL,
    status      INT4         DEFAULT 1                 NOT NULL,
    contract_id INTEGER                                NOT NULL,
    created_at  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by  INTEGER                                NOT NULL,
    updated_at  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by  INTEGER                                NOT NULL,

    CONSTRAINT fk_shares_contracts
        FOREIGN KEY (contract_id) REFERENCES contracts (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

-- Hợp đồng liên quan
create table contract_refs
(
    contract_id integer                                not null,
    ref_id      integer                                not null,
    created_at  timestamp(6) default current_timestamp not null,
    created_by  integer                                not null,
    updated_at  timestamp(6) default current_timestamp not null,
    updated_by  integer                                not null,

    constraint pk_references
        primary key (contract_id, ref_id),

    constraint fk_contract_refs_contract_id
        foreign key (contract_id) references contracts (id)
            on update cascade
            on delete cascade,

    constraint fk_contract_refs_references_id
        foreign key (ref_id) references contracts (id)
            on update cascade
            on delete cascade
);
