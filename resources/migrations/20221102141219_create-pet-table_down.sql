-- we need to use CASCADE because the 'pet' table depends on the 'animal_t' type
DROP TYPE IF EXISTS animal_t CASCADE;

DROP TABLE IF EXISTS pet;
