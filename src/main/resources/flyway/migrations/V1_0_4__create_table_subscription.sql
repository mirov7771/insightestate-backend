create table subscription
(
    id                      uuid        not null primary key,
    created_at              timestamp   without time zone not null,
    updated_at              timestamp   without time zone not null,
    user_id                 uuid,
    main_id                 uuid null,
    main_pay_date           timestamp null,
    extra_id                uuid null,
    extra_pay_date          timestamp null
);
