alter table shares
    alter column share_type set data type int4;
alter table shares
    alter column start_at drop not null;
alter table shares
    alter column expire_at drop not null;
alter table shares
    alter column token drop not null;