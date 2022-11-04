#!/usr/bin/env bb
(ns db-seed
  (:require [babashka.fs :as fs]
            [babashka.pods :as pods]
            [clojure.edn :as edn]
            [clojure.string :as str]))

;; https://github.com/babashka/babashka-sql-pods
;; https://github.com/babashka/pod-registry/tree/master/examples
(pods/load-pod 'org.babashka/postgresql "0.1.1")

;; https://github.com/babashka/babashka-sql-pods/blob/master/test/pod/babashka/postgresql_test.clj
(require '[pod.babashka.postgresql :as pg])

(defn db-url->jdbc-url
  [db-url]
  (let [s (last (str/split (str db-url) (re-pattern "//")))
        [user-password host-port] (str/split s (re-pattern "@"))
        [user password] (str/split user-password (re-pattern ":"))
        [host port] (str/split host-port (re-pattern ":"))]
    (format "jdbc:postgresql://%s:%s/?user=%s&password=%s" host port user password)))

(def jdbc-url
  (case (keyword (System/getenv "APP_PROFILE"))
    :dev (format "jdbc:postgresql://%s:%s/%s?user=%s&password=%s" "postgres" 5432 "root" "root" "root")
    :prod-proxied (let [m (edn/read-string (slurp "./secrets/postgres-prod-proxied.edn"))]
                    (format "jdbc:postgresql://%s:%s/?user=%s&password=%s" (:host m) (:port m) (:user m) (:password m)))
    :prod (db-url->jdbc-url (System/getenv "DATABASE_URL"))
    nil))

(when (not jdbc-url)
  (println "Cannot resolve JDBC connection string. Check APP_PROFILE")
  (System/exit 1))

(def seed-script-path "./resources/seed.sql")

(when (not (fs/exists? seed-script-path))
  (println (format "%s does not exist" seed-script-path))
  (System/exit 1))

(def sql (slurp seed-script-path))

(def pg-version (-> (pg/execute! jdbc-url ["select version()"])
                    first
                    :version))

(println pg-version)

(pg/execute! jdbc-url [sql])
