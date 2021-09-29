DROP DATABASE IF EXISTS finance;
CREATE USER finance;
CREATE DATABASE finance;
GRANT ALL PRIVILEGES ON DATABASE finance TO finance;
\c finance;

create table transaction
(
    id    bigserial not null
        constraint transaction_pk
            primary key,
    title text,
    sum   integer,
    date  date      not null
);

alter table transaction
    owner to finance;

create unique index transaction_id_uindex
    on transaction (id);


