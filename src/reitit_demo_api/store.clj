(ns reitit-demo-api.store
  "This namespace defines the application store and its methods."
  (:require [next.jdbc :as jdbc]
            [reitit-demo-api.config :refer [db-spec]]
            [reitit-demo-api.db-fns :as db-fns]
            [reitit-demo-api.utils :refer [uuid]]
            [taoensso.timbre :as timbre :refer [debug error]])
  (:import [org.postgresql.util PSQLException]))

(defprotocol Store
  "An abstraction of a store that holds the application's state."
  (get-persons [this] "Retrieves all persons from the store")
  (get-pets [this] "Retrieves all pets from the store")
  (put-person! [this person] "Inserts a new person in the store.")
  (put-pet! [this pet] "Inserts a new pet in the store.")
  (reset-persons! [this] "Deletes all persons from the store.")
  (reset-pets! [this] "Deletes all pets from the store."))

(defrecord AtomStore [^String name data])

(defrecord PostgresStore [^String name datasource])

(extend-protocol Store

  AtomStore
  (get-persons
    [this]
    (debug "get-persons")
    {:value (get @(:data this) :persons)})

  (get-pets
    [this]
    (debug "get-pets")
    (get @(:data this) :pets))

  (put-person!
    [this params]
    (debug "Try storing person in Atom" params)
    (let [id (uuid)
          person (assoc params :id id)]
      (debug "Person stored in Atom with ID" id)
      (swap! (:data this) update-in [:persons] conj person)
      {:value person}))

  (put-pet!
    [this params]
   (debug "Try storing pet in Atom" params)
    (let [id (uuid)
          pet (assoc params :id id)]
      (debug "Pet stored in Atom with ID" id)
      (swap! (:data this) update-in [:pets] conj pet)
      {:value pet}))

  (reset-persons!
    [this]
    (debug "reset-persons!")
    (swap! (:data this) assoc-in [:persons] '()))

  (reset-pets!
    [this]
    (debug "reset-pets!")
    (swap! (:data this) assoc-in [:pets] '()))

  PostgresStore
  (get-persons
   [this]
   (debug "Try retrieving persons from PostgreSQL")
   (try
     (let [ds (:datasource this)
           persons (db-fns/get-persons ds)]
       (debug "Retrieved" (count persons) "persons from PostgreSQL")
       {:value persons})
     (catch PSQLException e
       (do (error "Could not retrieve persons from PostgreSQL" (ex-message e))
           {:ex-info (ex-info "Could not retrieve persons" {:cause :database-exception})}))))

  (get-pets
   [this]
   (debug "get-pets")
   (let [ds (:datasource this)]
     (db-fns/get-pets ds)))

  (put-person!
   [this params]
   (debug "Try storing person in PostgreSQL" params)
     ;; consider using this library
     ;; https://github.com/rufoa/try-let
   (try
     (let [ds (:datasource this)
           xs (db-fns/put-person! ds params)
           person (first xs)]
       (debug "Person stored in PostgreSQL with ID" (:id person))
       {:value person})
     (catch PSQLException e
       (do (error "Could not store person in PostgreSQL" (ex-message e))
             ;; don't let the caller know that we use PostgreSQL for storage
           {:ex-info (ex-info "Could not create person" {:cause :database-exception
                                                         :params params})}))))

  (put-pet!
   [this params]
   (debug "Try storing pet in PostgreSQL" params)
   (try
     (let [ds (:datasource this)
           xs (db-fns/put-pet! ds params)
           pet (first xs)]
       (debug "Pet stored in PostgreSQL with ID" (:id pet))
       {:value pet})
     (catch PSQLException e
       (do
         (error "Could not store pet in PostgreSQL" (ex-message e))
         {:ex-info (ex-info "Could not create pet" {:cause :database-exception
                                                    :params params})})))))

(defn def-atom-store
  "Instantiate a store that holds the state in an atom.

   See [Record Constructors](https://guide.clojure.style/#record-constructors)."
  [name]
  (->AtomStore name (atom {:persons '()
                           :pets '()})))

(defn def-postgres-store
  "Instantiate a store that holds the state in a PostgreSQL database.

   See [Record Constructors](https://guide.clojure.style/#record-constructors)."
  [name datasource]
  (->PostgresStore name datasource))

(comment
  (def a-store (def-atom-store "My Atom Store"))

  (put-person! a-store {:fullname "John Doe" :age 25})
  (put-pet! a-store {:name "Fluffy" :animal "cat"})

  (get-persons a-store)
  (get-pets a-store)

  (reset-persons! a-store)
  (reset-pets! a-store)
  )

(comment
  (def ds (jdbc/get-datasource (db-spec :dev)))
  (def pg-store (def-postgres-store "My PostgreSQL Store" ds))

  (put-person! pg-store {:fullname "John Doe" :age 35})
  (put-pet! pg-store {:name "Fido" :animal "dog"})

  (get-pets pg-store)
  (get-persons pg-store)
  )
