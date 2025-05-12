create table stripe_user
(
    id                      uuid        not null primary key,
    created_at              timestamp   without time zone not null,
    updated_at              timestamp   without time zone not null,
    user_id                 uuid,
    customer_id             varchar null
);
