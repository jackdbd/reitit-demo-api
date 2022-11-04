const migrations = {
  directory: "resources/migrations",
  stub: "resources/migration-stub.cjs",
  tableName: "knex_migrations",
};

// See APP_PROFILE (dev, prod, prod-proxied, test)

const config = {
  dev: {
    client: "postgresql",
    connection: {
      port: 5432,
      database: "root",
      user: "root",
      password: "root",
    },
    migrations,
    pool: {
      min: 2,
      max: 10,
    },
  },
  "prod-proxied": {
    client: "postgresql",
    connection: {
      port: process.env.DB_PROD_PROXY_PORT,
      user: process.env.DB_PROD_USER,
      password: process.env.DB_PROD_PASSWORD,
    },
    migrations,
    pool: {
      min: 2,
      max: 10,
    },
  },
};

// console.log("=== Knex config ===", config);
module.exports = config;
