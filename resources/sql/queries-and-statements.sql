-- :name get-persons
-- :command :query
-- :result :raw
-- :doc Get all records from the `person` table
SELECT * FROM person
ORDER BY fullname DESC;

-- :name get-pets
-- :command :query
-- :result :raw
-- :doc Get all records from the `pet` table
SELECT * FROM pet
ORDER BY animal DESC;

-- Note:
-- For INSERT statements that do not return anything, use the :execute command
-- and the :raw result.
-- For INSERT statements that return something, use the :returning-execute command.
-- https://www.hugsql.org/using-hugsql/insert/#option-1-insert--returning
-- https://github.com/layerware/hugsql/issues/15#issuecomment-184221850

-- :name put-person!
-- :command :returning-execute
-- :result :raw
-- :doc Insert a single record in the `person` table.
INSERT INTO person (id, fullname, age)
VALUES (gen_random_uuid(), :fullname, :age)
RETURNING id, fullname, age;

-- Note: the double colon :: casts the incoming type (a character varying in
-- this case) to the actual type defined in the table schema. It can also be
-- written as:
-- CAST(expression AS target_type);
-- https://github.com/metabase/metabase/issues/1623#issuecomment-345887183

-- :name put-pet!
-- :command :returning-execute
-- :result :raw
-- :doc Insert a single record in the `pet` table.
INSERT INTO pet (id, name, animal) 
VALUES (gen_random_uuid(), :name, :animal::animal_t) 
RETURNING id, name, animal;

-- :name delete-person-by-id!
-- :command :execute
-- :result :affected
-- :doc Delete record by id from the `person` table.
DELETE FROM person WHERE id = :id::UUID;

-- :name delete-pet-by-id!
-- :command :execute
-- :result :affected
-- :doc Delete record by id from the `pet` table.
DELETE FROM pet WHERE id = CAST(:id AS UUID);
