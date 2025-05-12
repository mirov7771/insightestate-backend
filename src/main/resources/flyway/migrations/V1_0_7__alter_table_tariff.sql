alter table tariff add column stripe_id varchar;
alter table tariff add column stripe_extra_id varchar;

alter table tariff add column stripe_product_id varchar;
alter table tariff add column stripe_product_extra_id varchar;

update tariff
set stripe_id = 'price_1RNrf9C7cCHxCxhsl37lmvLi'
  , stripe_extra_id = 'price_1RNrfzC7cCHxCxhs4bG88QBt'
  , stripe_product_id = 'prod_SISaO6AWbWO9ao'
  , stripe_product_extra_id = 'prod_SISbTo33heYOOl'
where id = '8acf9e68-c4d0-43b1-9c22-b7f712f101a4';

update tariff
set stripe_id = 'price_1RNrimC7cCHxCxhsURj4QxWt'
  , stripe_extra_id = 'price_1RNrjWC7cCHxCxhsiDhqDqPY'
  , stripe_product_id = 'prod_SISelcVEo8CHb5'
  , stripe_product_extra_id = 'prod_SISeFxpNsQCYKK'
where id = 'b749d197-846e-49d4-aedc-abf7b3784b11';

