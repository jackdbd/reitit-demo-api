(ns integrant-utils 
  (:require [reitit-demo-api.config :refer [aero-prep]]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [clojure.pprint :as pprint]))

(defn integrant-prep!
  "Parses system configuration with aero-reader and applies the given profile
   values.

   Return: Integrant configuration to be used to start the system."
  [profile]
  (ig-repl/set-prep!
   #(aero-prep profile)))

(defn go
  "Prepare configuration and start the system services with Integrant-repl"
  ([]
   (go :dev))
  ([profile]
   (integrant-prep! profile)
   (ig-repl/go)))

(defn reset
  "Read updates from the configuration and restart the system services with Integrant-repl"
  ([]
   (reset :dev))
  ([profile]
   (integrant-prep! profile)
   (ig-repl/reset)))

(defn reset-all
  "Read updates from the configuration and restart the system services with Integrant-repl"
  ([]
   (reset-all :dev))
  ([profile]
   (integrant-prep! profile)
   (ig-repl/reset-all)))

(defn stop
  "Shutdown all services"
  []
  (ig-repl/halt))

(defn system
  "The running system configuration"
  []
  (pprint/pprint ig-state/system)
  ig-state/system)

(defn config
  "The current system configuration used by Integrant"
  []
  ig-state/config)

(defn dependency-graph
  "The dependency graph of the current system configuration used by Integrant"
  []
  (ig/dependency-graph (config)))