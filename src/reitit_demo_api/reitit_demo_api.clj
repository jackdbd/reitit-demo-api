(ns reitit-demo-api.reitit-demo-api
  "Entry point of the app."
  (:gen-class)
  (:require [next.jdbc.connection :as connection]
            [reitit-demo-api.app :refer [def-app]]
            [reitit-demo-api.config :as conf]
            [ring.adapter.jetty :as jetty])
  (:import [com.zaxxer.hikari HikariDataSource]))

(defn -main
  "Entry point. Invoke me with clojure -M -m reitit-demo-api.reitit-demo-api"
  [& _args]
  (let [profile (or (keyword (System/getenv "APP_PROFILE")) :prod)
        cfg (conf/aero-config profile)
        opts (:reitit-demo-api/http-server cfg)
        port (:port opts)
        jdbc-url (conf/jdbc-url profile)
        ds (connection/->pool HikariDataSource {:jdbcUrl jdbc-url})
        app (def-app ds)]
    (println "=== profile ===" profile)
    (println "server listening on port" port)
    (jetty/run-jetty app {:join (:join opts) :port port})))
