(ns reitit-demo-api.store-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [next.jdbc :as jdbc]
            [reitit-demo-api.config :as conf]
            [reitit-demo-api.store :refer [def-atom-store
                                           def-postgres-store
                                           get-persons
                                           get-pets]]))

(def jdbc-url (conf/jdbc-url :test))
(println "jdbc-url" jdbc-url)

(deftest atom-store-test
  (testing "TODO: fix"
    (let [a (def-atom-store "My Atom Store")]
      ;; TODO: why does dereferencing an atom raise an exception?
      ;; (is (empty (:pets @a)))
      (is (some? a)))))

(deftest get-persons-test
  (testing "can retrieve persons without errors"
    (let [ds (jdbc/get-datasource jdbc-url)
          store (def-postgres-store "My PostgreSQL Store" ds)
          m (get-persons store)]
      (is (nil? (:ex-info m)))
      (is (not (nil? (:value m)))))))

(deftest get-pets-test
  (testing "can retrieve pets without errors"
    (let [ds (jdbc/get-datasource jdbc-url)
          store (def-postgres-store "My PostgreSQL Store" ds)
          pets (get-pets store)]
      (is (not (nil? pets))))))
