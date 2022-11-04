(ns reitit-demo-api.reitit-demo-api
  "Entry point of the app."
  (:gen-class)
  (:require [next.jdbc.connection :as connection]
            [reitit-demo-api.app :refer [def-app]]
            [reitit-demo-api.config :refer [aero-config jdbc-url]]
            [ring.adapter.jetty :as jetty])
  (:import [com.zaxxer.hikari HikariDataSource]))

(defn -main
  "Entry point. Invoke me with clojure -M -m reitit-demo-api.reitit-demo-api"
  [& _args]
  (let [cfg (aero-config :prod)
        opts (:reitit-demo-api/http-server cfg)
        ds (connection/->pool HikariDataSource {:jdbcUrl (jdbc-url :prod)})
        app (def-app ds)]
    (println "cfg" cfg)
    (println "JDBC URL" (jdbc-url :prod))
    (println "server listening on port" (:port opts))
    (jetty/run-jetty app {:join (:join opts) :port (:port opts)})))
