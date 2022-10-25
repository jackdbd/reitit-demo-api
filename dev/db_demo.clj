#!/usr/bin/env bb
(ns db-demo
  (:require [babashka.fs :as fs]
            [babashka.pods :as pods])
  (:import [java.util Date]))

;; https://github.com/babashka/babashka-sql-pods
;; https://github.com/babashka/pod-registry/tree/master/examples
(pods/load-pod 'org.babashka/postgresql "0.1.1")

;; https://github.com/babashka/babashka-sql-pods/blob/master/test/pod/babashka/postgresql_test.clj
(require '[pod.babashka.postgresql :as pg])

(def db {:dbtype   "postgresql"
         :host     "postgres"
         :dbname   "root"
         :user     "root"
         :password "root"
         :port     5432})

(def up-migrations
  ["./resources/migrations/20221102141219_create-pet-table_up.sql"
   "./resources/migrations/20221102143235_create-api-user-table_up.sql"])

(def down-migrations
  ["./resources/migrations/20221102143235_create-api-user-table_down.sql"
   "./resources/migrations/20221102141219_create-pet-table_down.sql"])

(def pg-version (-> (pg/execute! db ["select version()"])
                    first
                    :version))

;; this is just to make clj-kondo stop complaining
;; https://github.com/clj-kondo/clj-kondo/blob/master/doc/linters.md#unresolved-symbol
(declare tx)

(defn migrate! [{:keys [direction verbose]}]
  (let [_start-date (Date.)
        xs (if (= :up direction) up-migrations down-migrations)
        conn (pg/get-connection db)]
    (println (str "Run " (count xs) " migration scripts on " pg-version))
    (doseq [file-path xs
            :let [sql (slurp file-path)]
            :when (fs/exists? file-path)]
      (println (str "Run " file-path))
      (pg/with-transaction [tx conn]
        (when verbose
          (println (str "=== SQL BEGIN ===\n" sql)))
        (pg/execute! tx [sql])
        (when verbose
          (println (str "=== SQL END ===")))))))

(comment
  (migrate! {:direction :up
             :verbose true})
  )

(println pg-version)

(def seed-script-filepath "./resources/seed.sql")

(when (not (fs/exists? seed-script-filepath))
  (println (str "filepath " seed-script-filepath " does not exist"))
  (System/exit 1))
(let [sql (slurp seed-script-filepath)
      conn (pg/get-connection db)]
  (println (str "Run " seed-script-filepath))
  (pg/execute! conn [sql]))