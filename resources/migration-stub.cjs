const { readFile } = require("node:fs/promises");
const path = require("node:path");

const MIGRATIONS_DIR = path.resolve("resources", "migrations");

const [timestamp, filename] = path.basename(__filename).split("_");
const [basename] = filename.split(".");

const migrate = async (knex, fname) => {
  const buf = await readFile(path.join(MIGRATIONS_DIR, fname));
  const sql = buf.toString();
  const results = await knex.raw(sql);

  if (results !== undefined) {
    if (results.length !== undefined) {
      console.log(`Executed ${results.length} SQL statements in ${fname}`);
      // results.forEach((r) => {
      //   console.log(`Executed ${r.command}`);
      // });
    } else {
      console.log(`Executed 1 SQL statement in ${fname}`);
      // console.log(`Executed ${results.command}`);
    }
  }
};

exports.up = async function migrateUp(knex) {
  await migrate(knex, `${timestamp}_${basename}_up.sql`);
};

exports.down = async function migrateDown(knex) {
  await migrate(knex, `${timestamp}_${basename}_down.sql`);
};
