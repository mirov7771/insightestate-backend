create table tariff
(
    id                      uuid        not null primary key,
    created_at              timestamp   without time zone not null,
    updated_at              timestamp   without time zone not null,
    type                    smallint,
    title                   varchar,
    description             varchar,
    price                   bigint
);

insert into tariff values (gen_random_uuid(), current_timestamp, current_timestamp, 0, 'Бесплатная версия', '— 3 бесплатных генераций предложений клиенту;— 2 бесплатные подборки;— 2 бесплатных запроса в AI подборщик;— ограниченную аналитику по объекту: только оценки', 0);
insert into tariff values (gen_random_uuid(), current_timestamp, current_timestamp, 0, 'PRO', '— 30 генераций предложений клиенту;— 7 подборок;— 4 бесплатных запроса в AI подборщик;— вся аналитика по объекту: оценки, расчет экономики;— возможность вступить в клуб;— 2 инвайта в клуб каждый месяц', 39);
insert into tariff values (gen_random_uuid(), current_timestamp, current_timestamp, 0, 'PREMIUM', '— Без ограничений генераций предложений клиенту;— Без ограничений по подборокам;— 8 бесплатных запросов в AI подборщик;— вся аналитика по объекту: оценки, расчет экономики;— возможность вступить в клуб;— 5 инвайтов в клуб каждый месяц', 59);
insert into tariff values (gen_random_uuid(), current_timestamp, current_timestamp, 1, 'AI пакет к любому тарифу', '— Не ограниченное количество запросов в AI подборщик', 29);
