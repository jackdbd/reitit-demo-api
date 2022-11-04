(ns reitit-demo-api.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [integrant.core :as ig]))

;; https://practical.li/clojure-web-services/repl-driven-development/integrant-repl/

;; extra reader tag for Integrant references
(defmethod aero/reader 'ig/ref
  [_opts _tag value]
  (ig/ref value))

(defn aero-config
  "Retrieves all the configuration for a profile (:dev, :test, :prod)."
  [profile]
  (aero/read-config (io/resource "config.edn") {:profile profile}))

(defn db-spec
  "Retrieves the configuration for the database, for the given profile (:dev, :test, :prod)."
  [profile]
  (:reitit-demo-api/relational-store (aero-config profile)))

(defn fly.io-db-url->jdbc-url
  [db-url]
  (let [s (last (str/split (str db-url) (re-pattern "//")))
        [user-password host-port] (str/split s (re-pattern "@"))
        [user password] (str/split user-password (re-pattern ":"))
        [host port] (str/split host-port (re-pattern ":"))]
    (format "jdbc:postgresql://%s:%s?user=%s&password=%s" host port user password)))

(defn jdbc-url
  [profile]
  (let [{:keys [dbtype db-url host port dbname user password]} (db-spec profile)]
    (case profile
      :dev (format "jdbc:%s://%s:%s/%s?user=%s&password=%s", dbtype host port dbname user password)
      :prod (fly.io-db-url->jdbc-url db-url))))

 (defn aero-prep
  "Parses the system config and updates values for the given profile.
   
   Top-level keys in the config.edn use a qualified name of the Clojure
   namespace the ig/init-key defmethod is defined in ig/load-namespaces will
   automatically load each namespace referenced by a top-level key in the
   Integrant configuration.

   Return: configuration hash-map for the specified profile."
  [profile]
  (let [config (aero-config profile)]
    (ig/load-namespaces config)
    config))

(comment
  (aero-config :default)
  (aero-config :dev)
  (aero-config :prod)
  (aero-config :test)

  (jdbc-url :dev)

  (:reitit-demo-api/relational-store (aero-config :dev))
  (:reitit-demo-api/router (aero-config :dev)) 
  (:reitit-demo-api/http-server (aero-config :dev))

  (db-spec :dev)
  (aero-prep :dev)
  )