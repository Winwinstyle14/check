alter table customers drop column type_id;
drop table types;
alter table customers add column role_id integer;