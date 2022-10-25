(ns reitit-demo-api.utils
  "This namespace contains some generic utility functions."
  (:import [java.util UUID]))

(defn uuid []
  (.toString (UUID/randomUUID)))
