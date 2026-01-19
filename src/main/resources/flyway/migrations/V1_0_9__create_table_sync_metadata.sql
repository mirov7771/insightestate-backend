create table sync_metadata
(
    id                      uuid            not null primary key,
    created_at              timestamp       without time zone not null,
    updated_at              timestamp       without time zone not null,
    sync_metadata_detail    json            not null

);

comment on table sync_metadata is 'Служебная таблица синхронизации';
comment on column sync_metadata.id is 'PK таблицы';
comment on column sync_metadata.created_at is 'Дата создания записи';
comment on column sync_metadata.updated_at is 'Дата обновления записи';
comment on column sync_metadata.sync_metadata_detail is 'JSON данные синхронизации';
