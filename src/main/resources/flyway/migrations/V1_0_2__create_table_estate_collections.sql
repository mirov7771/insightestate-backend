create table estate_collections
(
    id                      uuid        not null primary key,
    created_at              timestamp   without time zone not null,
    updated_at              timestamp   without time zone not null,
    collection_detail       json        not null
);

comment on table estate_collections is 'Пользователи';
comment on column estate_collections.id is 'PK таблицы';
comment on column estate_collections.created_at is 'Дата создания записи';
comment on column estate_collections.updated_at is 'Дата обновления записи';
comment on column estate_collections.collection_detail is 'JSON данные коллекции estate';

create unique index estate_collections__user_id
    on estate_collections ((collection_detail ->> 'userId'));
comment on index estate_collections__user_id is 'Уникальный индекс по id пользователя';
