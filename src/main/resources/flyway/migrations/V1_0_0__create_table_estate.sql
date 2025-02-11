create table estate
(
    id              uuid        not null primary key,
    created_at      timestamp   without time zone not null,
    updated_at      timestamp   without time zone not null,
    estate_detail   json        not null
);
