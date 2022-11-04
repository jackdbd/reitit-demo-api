#!/usr/bin/env bb
(ns db-seed
  (:require [babashka.fs :as fs]
            [babashka.pods :as pods]))

;; https://github.com/babashka/babashka-sql-pods
;; https://github.com/babashka/pod-registry/tree/master/examples
(pods/load-pod 'org.babashka/postgresql "0.1.1")

;; https://github.com/babashka/babashka-sql-pods/blob/master/test/pod/babashka/postgresql_test.clj
(require '[pod.babashka.postgresql :as pg])

(def db-spec
  (case (keyword (System/getenv "DB_ENV"))
    :production-proxied {:dbtype "postgresql"
                         :host  "localhost"
                         :user (System/getenv "DB_PROD_USER")
                         :password (System/getenv "DB_PROD_PASSWORD")
                         :port (System/getenv "DB_PROD_PROXY_PORT")}
    :development {:dbtype "postgresql"
                  :host  "postgres"
                  :user "root"
                  :password "root"
                  :port 5432}
    :else nil))

(when (not db-spec)
  (println "Cannot infer database connection paramaters. Check DB_ENV")
  (System/exit 1))

(def seed-script-path "./resources/seed.sql")

(when (not (fs/exists? seed-script-path))
  (println (format "%s does not exist" seed-script-path))
  (System/exit 1))

(def sql (slurp seed-script-path))

(def pg-version (-> (pg/execute! db-spec ["select version()"])
                    first
                    :version))

 (println pg-version)

(pg/execute! db-spec [sql])
