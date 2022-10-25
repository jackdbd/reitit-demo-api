-- https://www.postgresql.org/docs/current/functions-enum.html
-- https://stackoverflow.com/questions/10923213/postgres-enum-data-type-or-check-constraint
-- https://tapoueh.org/blog/2018/05/postgresql-data-types-enum/
-- PostgreSQL does not support the syntax 'CREATE TYPE IF NOT EXISTS', so we use
-- a try/catch workaround.
-- https://stackoverflow.com/questions/7624919/check-if-a-user-defined-type-already-exists-in-postgresql
DO $$ BEGIN
    CREATE TYPE animal_t AS ENUM ('cat', 'dog', 'rabbit', 'snake', 'iguana');
EXCEPTION
  -- https://www.postgresqltutorial.com/postgresql-plpgsql/postgresql-exception/
  WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS pet
(
  id UUID NOT NULL,
  name text NOT NULL,
  animal animal_t,
  CONSTRAINT pet_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE pet IS 'Pets that belong to a person';

COMMENT on COLUMN pet.id IS 'The pet ID';
COMMENT on COLUMN pet.animal IS 'The type of animal';
