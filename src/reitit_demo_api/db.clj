(ns reitit-demo-api.db
  "This namespace represents the bridge between the database world and the
   clojure world."
  (:require [reitit-demo-api.config :as conf]
            [reitit-demo-api.db-fns :as db-fns]))

(comment
  (def spec (conf/db-spec :dev))
  
  (db-fns/get-persons spec)
  (db-fns/get-pets spec)

  (db-fns/put-person! spec {:fullname "John Doe" :age 25})
  (db-fns/put-pet! spec {:name "Fluffy" :animal "rabbit"})

  (db-fns/delete-person-by-id! spec {:id "cc81f587-7bf3-454c-8137-ddab38b008d4"})
  (db-fns/delete-pet-by-id! spec {:id "17ec5545-80e0-455d-bfbf-d412eaa65bed"})
  )