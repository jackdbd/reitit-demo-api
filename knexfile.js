const config = {
  development: {
    client: "postgresql",
    connection: {
      database: "root",
      user: "root",
      password: "root",
    },
    migrations: {
      directory: "resources/migrations",
      stub: "resources/migration-stub.cjs",
      tableName: "knex_migrations",
    },
    pool: {
      min: 2,
      max: 10,
    },
  },
};

// console.log("=== Knex config ===", config);
module.exports = config;
