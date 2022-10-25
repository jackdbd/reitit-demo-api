(ns reitit-demo-api.middleware.store
  "Middleware that adds an instance of a store to the request map."
  (:require [next.jdbc :as jdbc]
            [reitit-demo-api.store :refer [def-atom-store
                                                      def-postgres-store]]
            [reitit-demo-api.db :as db]))

(defn wrap-store
  "Ring middleware that adds an instance of a store to the request map.
   
   With a store, the persistance layer is injected into the request."
  [handler store]
  (fn [req]
    (handler (assoc req :store store))))

;; (def store-middleware
;;   "Reitit middleware that adds an instance of a store to the request map."
;;   {:name ::store
;;    :wrap wrap-store})

(defn create-store-middleware
  "Reitit middleware that adds an instance of a store to the request map."
  [store]
  {:name ::store
   :wrap (fn [handler]
           (wrap-store handler store))}
  )

(comment
  (defn my-handler
    [req]
    (println req)
    {:body "<h1>hello</h1>" :status 200 :headers {"Content-Type" "text/html"}})

  (def my-handler-with-atom-store
    (wrap-store my-handler (def-atom-store "My Atom Store")))

  (def ds (jdbc/get-datasource db/spec))
  (def my-handler-with-postgres-store
    (wrap-store my-handler (def-postgres-store "My PostgreSQL Store" ds)))

  (def req {:method :get})

  (my-handler req)
  (my-handler-with-atom-store req)
  (my-handler-with-postgres-store req))
