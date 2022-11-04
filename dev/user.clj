(ns user
  (:require [integrant.core :as ig]
            [integrant-utils :as iu]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [reitit-demo-api.app :refer [def-app]]
            [reitit-demo-api.config :refer [jdbc-url] :as conf]
            [reitit-demo-api.routes :refer [raw-routes]]
            [portal-utils :as pu]
            [reitit.core :as r]
            [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty])
  (:import [com.zaxxer.hikari HikariDataSource]))

(comment
  ;; keep these commands for Portal here and at the bottom, for convenience
  (pu/init!)
  (tap> "hello")
  (pu/close!))

(comment
  ;; Prepare and start the system using the :dev profile
  (iu/go :dev)
  (iu/stop)
  (iu/reset))

(comment
  (def db-spec (conf/db-spec :dev))
  (def handler (def-app (jdbc/get-datasource db-spec)))

  ;; Define a dynamic variable for the datasource, so we can start/stop it from the REPL.
  ;; This seems not to work
  ;; (def ^:dynamic ^HikariDataSource *ds* (connection/->pool HikariDataSource db-spec))
  ;; This works
  (def ^:dynamic ^HikariDataSource *ds* (connection/->pool HikariDataSource {:jdbcUrl (jdbc-url :dev)}))

  (def handler (def-app *ds*))
  (handler {:request-method :get :uri "/persons"})

  (.close *ds*)
  )

(defmethod ig/init-key :reitit-demo-api/relational-store [k db-spec]
  (println (str "system key " k " initialized to " db-spec))
  (println "Create database connection pool")
  (let [datasource (connection/->pool HikariDataSource {:jdbcUrl (jdbc-url :dev)})]
    datasource))

(defmethod ig/halt-key! :reitit-demo-api/relational-store [k datasource]
  (println (str "system key " k " halted"))
  (println "Close database connection pool")
  (.close datasource)
  )

(defmethod ig/init-key :reitit-demo-api/router [k m]
  (println (str "system key " k " initialized to " m))
  (let [datasource (:datasource m)
        handler (def-app datasource)]
    handler))

(defmethod ig/halt-key! :reitit-demo-api/router [k _handler]
  (println (str "system key " k " halted")))

(defmethod ig/init-key :reitit-demo-api/http-server [k m]
  (println (str "system key " k " initialized to " m))
  (let [handler (:handler m)
        jetty-options (dissoc m :handler)]
    (println (str "HTTP server will listen on port " (:port jetty-options)))
    (jetty/run-jetty handler jetty-options)))

(defmethod ig/halt-key! :reitit-demo-api/http-server [k server]
  (println (str "system key " k " halted"))
  (println "Stop HTTP server")
  (.stop server))

(comment
  ;; Prepare and start the system using the :dev profile
  (iu/go :dev)
  (iu/stop)
  (iu/reset))

(comment
  ;; info on the Integrant system
  (iu/system)
  (iu/config)
  (iu/dependency-graph))

(comment
  (pu/init!)
  (tap> "hello")
  (pu/close!))

(comment
  ;; This works only when the system is running. Call (iu/go :dev) first.
  (def app (:reitit-demo-api/router (iu/system)))

  ;; try sending some stuff to the REPL
  (app {:request-method :get :uri "/"})
  (app {:request-method :get, :uri "/favicon.ico"})

  (app {:request-method :get :uri "/ping"})
  (app {:request-method :get :uri "/files/download"})
  (app {:request-method :post :uri "/files/upload" :parameters {:multipart {:file 123}}})
  (app {:request-method :get :uri "/api/users"})
  (app {:request-method :get :uri "/swagger.json"})

  (app {:request-method :get :uri "/math/plus" :query-params {:x 2 :y 3}})
  (app {:request-method :get :uri "/math/plus" :query-params {:x 2 :y 3.2}})
  (app {:request-method :post :uri "/math/plus" :body-params {:x 2 :y 3}})
  (app {:request-method :post :uri "/math/plus" :body-params {:x 2 :y 3.2}})
  
  (app {:request-method :get :uri "/persons"})
  (app {:request-method :get :uri "/pets"})
  (app {:request-method :get :uri "/pets" :parameters {:query {:animal "cat"}}}))

(comment
  ;; try sending some stuff to Portal
  (pu/tap-html> app :get "/")
  (pu/tap-html> app :get "/ping")
  (pu/tap-html> app :get "/api/users")
  (pu/tap-image> app :get "/files/download")

  ;; http://localhost:$PORT/math/plus?x=2&y=3
  (pu/tap-json> app :get "/math/plus" {:x 2 :y 3})
  (pu/tap-json> app :get "/math/plus" {:x 2 :y 3.2})
  (pu/tap-json> app :post "/math/plus" {:x 2 :y 3})
  (pu/tap-json> app :post "/math/plus" {:x 2 :y 3.2})
  (pu/tap-json> app :get "/math/product" {:x 2 :y 3})

  (pu/tap-json> app :get "/pets" {:animal "cat"})
  (pu/tap-json> app :post "/pets" {:name "Bob" :animal "cat"}))

(comment
  ;; test reverse routing
  (def ring-router (ring/get-router app))

  (r/match-by-name ring-router :reitit-demo-api.router/ping)

  (tap> (with-meta
          [:portal.viewer/tree raw-routes]
          {:portal.viewer/default :portal.viewer/pprint})))

(comment
  ;; Prepare and start the system using the :dev profile
  (iu/go :dev)
  (iu/stop)
  (iu/reset))
