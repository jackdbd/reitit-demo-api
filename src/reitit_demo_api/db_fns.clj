(ns reitit-demo-api.db-fns
  "This namespace contains the functions that HugSQL generates from SQL queries
   and statements."
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(hugsql/def-db-fns "sql/queries-and-statements.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

