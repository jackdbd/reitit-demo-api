(ns reitit-demo-api.routes
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [expound.alpha :as expound]
            [reitit-demo-api.handlers :refer [get-persons
                                                         get-pets
                                                         create-person
                                                         create-pet
                                                         get-ping
                                                         handle-plus-body
                                                         handle-plus-query
                                                         handle-product-query
                                                         handle-download
                                                         handle-upload]]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.swagger :as swagger]))

(defn at-least-n-chars [n m] (>= (count m) n))

(s/def ::fullname (s/and string? #((partial at-least-n-chars 3) %)))
(s/def ::age (s/and int? #(> % 0) #(< % 120)))
(s/def ::animal #{"cat" "dog" "rabbit" "snake" "iguana"})

(s/def ::file multipart/temp-file-part)
(s/def ::file-params (s/keys :req-un [::file]))

(s/def ::name string?)
(s/def ::size int?)
(s/def ::file-response (s/keys :req-un [::name ::size]))

;; Use data-specs to provide extra JSON-Schema properties:
;; https://github.com/metosin/spec-tools/blob/master/docs/04_json_schema.md#annotated-specs
;; (s/def ::x (st/spec {:spec int?
;;                      :name "X parameter"
;;                      :description "Description for X parameter"
;;                      :json-schema/default 42}))
(s/def ::x int?)
(s/def ::y int?)
(s/def ::total int?)
(s/def ::math-request (s/keys :req-un [::x ::y]))
(s/def ::math-response (s/keys :req-un [::total]))

(def raw-routes
  "Routes of the application.
   
   See also:

   - [Route Syntax](https://cljdoc.org/d/metosin/reitit/0.5.18/doc/basics/route-syntax)
   - [Route Data](https://cljdoc.org/d/metosin/reitit/0.5.18/doc/basics/route-data)."
  [["/ping" {:get {:handler get-ping}}]
   ["/swagger.json" {:get {:handler (swagger/create-swagger-handler)
                           :swagger {:info {:title "reitit-swagger Example API"
                                            :description "API implemented with reitit-ring"}}
                           :no-doc true}}]

   ["/files"
    {:swagger {:tags ["files"]}}

    ["/upload"
     {:post {:summary "upload a file"
             :parameters {:multipart ::file-params}
             :responses {200 {:body ::file-response}}
             :handler handle-upload}}]

    ["/download"
     {:get {:summary "downloads a file"
            :swagger {:produces ["image/png"]}
            :handler handle-download}}]]

   ["/math" {:swagger {:tags ["math"]}}
    ["/plus" {:get {:summary "plus with spec query parameters"
                    :parameters {:query ::math-request}
                    :responses {200 {:body ::math-response}}
                    :handler handle-plus-query}
              :post {:summary "plus with spec body parameters"
                     :parameters {:body ::math-request}
                     :responses {200 {:body ::math-response}}
                     :handler handle-plus-body}}]
    ["/product" {:get {:summary "product with spec query parameters"
                       :parameters {:query ::math-request}
                       :responses {200 {:body ::math-response}}
                       :handler handle-product-query}}]]

   ["/persons" {:swagger {:tags ["persons"]}
                :get {:summary "Retrieve a list of persons"
                      :responses {200 {:body {:persons some?}}}
                      :handler get-persons}
                :post {:summary "Create a new person"
                       :parameters {:body {:fullname ::fullname
                                           :age ::age}}
                       :responses {201 {:body {:person some?}}
                                   400 {:body {:error some?
                                               :details some?}}}
                       :handler create-person}}]

   ["/pets" {:swagger {:tags ["pets"]}
             :get {:summary "Retrieve a list of pets"
                  ;;  :parameters {:query {:animal string?}}
                   :responses {200 {:body {:pets some?}}}
                   :handler get-pets}
             :post {:summary "Create a new pet"
                    :parameters {:body {:name string?
                                        :animal string?}}
                    :responses {201 {:body {:pet some?}}}
                    :handler create-pet}}]])

(comment
  raw-routes
  (tap> (with-meta
          [:portal.viewer/tree raw-routes]
          {:portal.viewer/default :portal.viewer/pprint}))
  )

(comment
  (def num-samples 3)

  (s/conform ::fullname "ciao")
  ;; (tap> (s/explain-str ::fullname "ciaomondo"))
  (s/explain ::fullname "ciaomondo")
  (expound/expound ::fullname "xy" {:print-specs? true})
  (gen/sample (s/gen ::fullname) num-samples)

  (s/conform ::age 2)
  (s/explain ::age 33)
  (gen/sample (s/gen ::age))

  (expound/expound ::age 130)

  (s/conform ::animal "stone")
  (s/explain ::animal "stone")
  (gen/sample (s/gen ::animal) num-samples))