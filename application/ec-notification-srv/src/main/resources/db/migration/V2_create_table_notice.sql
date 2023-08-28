create table notice
(
    id serial
        constraint notice_pk
            primary key,
    contract_id int,
    message_id int,
    message_code varchar(50),
    notice_name varchar(600),
    notice_content varchar(600),
    notice_url varchar(255),
    email varchar(255),
    notice_date timestamp,
    status int default 0,
    created_at timestamp,
    created_by int,
    updated_at timestamp,
    updated_by int
);

comment on column notice.status is '0 - default, 1 - viewed';

create index notice_email_index
    on notice (email);

alter table messages
    add url varchar(255);

alter table messages
    add notice_template varchar(600);

