CREATE TABLE estate
(
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP   WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP   WITHOUT TIME ZONE NOT NULL,
    estate_detail   JSONB       NOT NULL
);
