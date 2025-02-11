create table users
(
    id              uuid        not null primary key,
    created_at      timestamp   without time zone not null,
    updated_at      timestamp   without time zone not null,
    user_detail     json        not null
);

comment on table users is 'Пользователи';
comment on column users.id is 'PK таблицы';
comment on column users.created_at is 'Дата создания записи';
comment on column users.updated_at is 'Дата обновления записи';
comment on column users.user_detail is 'JSON данные пользователя';

create unique index users_unique_login
    on users ((user_detail ->> 'login'));
comment on index users_unique_login is 'Уникальный индекс по логину пользователя';
