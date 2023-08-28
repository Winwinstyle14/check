create table messages
(
    id serial
        constraint messages_pk
            primary key,
    name varchar(50) not null,
    type int not null,
    code varchar(50) not null,
    mail_template text,
    sms_template varchar(600),
    status int default 1 not null,
    created_at timestamp,
    created_by varchar(50),
    updated_at timestamp,
    updated_by varchar(50)
);

comment on table messages is 'Template notification';

comment on column messages.type is 'Loai: 1-reset password';

comment on column messages.code is 'Ma code';

comment on column messages.status is '1-active';

INSERT INTO public.messages (id, name, type, code, mail_template, sms_template, status, created_at, created_by, updated_at, updated_by) VALUES (DEFAULT, 'Reset password', 1, 'reset_password', 'Vui lòng truy cập <a href="http://localhost:4200/reset-password?token=#TOKEN#>tại đây</a> để khôi phục mật khẩu.', null, 1, null, null, null, null);

create table email
(
    id bigserial
        constraint email_pk
            primary key,
    message_id int,
    subject varchar(255) not null,
    recipient varchar(600) not null,
    cc varchar(600),
    content text not null,
    status int default 0,
    retry int default 0,
    created_at timestamp,
    updated_at timestamp
);

create index email_status_index
    on email (status);

comment on column email.status is '0-init, 1-success, 2-sending, 3-fail';

create table sms
(
    id bigserial
        constraint sms_pk
            primary key,
    message_id int,
    phone varchar(15) not null,
    content text not null,
    status int default 0,
    retry int default 0,
    created_at timestamp,
    updated_at timestamp
);

create index sms_status_index
    on sms (status);

comment on column sms.status is '0-init, 1-success, 2-sending, 3-fail';