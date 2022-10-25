(ns reitit-demo-api.app
  (:require [expound.alpha :as expound]
            [muuntaja.core :as m]
            [reitit-demo-api.handlers :refer [not-found]]
            [reitit-demo-api.routes :refer [raw-routes]]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.spec]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.spec :as rrs]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.spec :as rs]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit-demo-api.middleware.store :refer [create-store-middleware]]
            [reitit-demo-api.store :refer [def-atom-store def-postgres-store]]))

(defn def-reitit-ring-router
  "Reitit Ring router that uses the given store as the persistence layer.
   
   See also:

   - [Ring Router](https://cljdoc.org/d/metosin/reitit/0.5.18/doc/ring/ring-router)
   - [Configuring Routers](https://cljdoc.org/d/metosin/reitit/0.5.18/doc/advanced/configuring-routers)"
  [store-middleware]
  (ring/router
   raw-routes
   {:data {:muuntaja m/instance
           :coercion reitit.coercion.spec/coercion
           :middleware [swagger/swagger-feature

                        ;; query-params & form-params
                        parameters/parameters-middleware

                        ;; content-negotiation
                        muuntaja/format-negotiate-middleware

                        ;; encoding response body
                        muuntaja/format-response-middleware

                        ;; inject a store in the request
                        store-middleware

                        ;; exception handling
                        exception/exception-middleware

                        ;; decoding request body
                        muuntaja/format-request-middleware

                        ;; coercing response bodys
                        coercion/coerce-response-middleware

                        ;; coercing request parameters
                        coercion/coerce-request-middleware

                        ;; multipart
                        multipart/multipart-middleware]}
    :exception pretty/exception
    :validate rrs/validate
    ::rs/explain expound/expound-str}))

(defn def-app
  "Defines the ring app (i.e. the main handler which routes the requests to the
   endpoints)."
  [datasource]
  (let [store (def-postgres-store "My PostgreSQL Store" datasource)
        store-middleware (create-store-middleware store)
        router (def-reitit-ring-router store-middleware)]
    (ring/ring-handler
     router
     (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/"
                                             :config {:validatorUrl nil
                                                      :operationsSorter "alpha"}})
      (ring/create-default-handler {:not-found not-found})))))

(comment
  (def store (def-atom-store "My Atom Store"))

  (def app
    (ring/ring-handler
     (def-reitit-ring-router (create-store-middleware store))
     (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/"
                                             :config {:validatorUrl nil
                                                      :operationsSorter "alpha"}})
      (ring/create-default-handler {:not-found not-found}))))
  
  (app {:request-method :get :uri "/"})
  (app {:request-method :get :uri "/pets"})
  (app {:request-method :get :uri "/this-route-does-not-exists"})
  )