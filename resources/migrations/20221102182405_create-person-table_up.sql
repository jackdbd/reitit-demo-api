CREATE TABLE IF NOT EXISTS person
(
  id UUID NOT NULL,
  fullname text NOT NULL,
  age smallint NOT NULL CHECK(age > 0 AND age < 120),
  CONSTRAINT user_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE person IS 'Persons that may or may not have pets';

-- https://www.citusdata.com/blog/2018/10/17/commenting-your-postgresql-database/
COMMENT on COLUMN person.id IS 'The person ID';
COMMENT on COLUMN person.fullname IS 'The person fullname (first name and last name)';
