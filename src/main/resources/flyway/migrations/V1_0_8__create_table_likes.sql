create table likes
(
    id                      uuid        not null primary key,
    created_at              timestamp   without time zone not null,
    updated_at              timestamp   without time zone not null,
    like_count              bigint default 0,
    collection_id           uuid,
    estate_id               uuid
);
