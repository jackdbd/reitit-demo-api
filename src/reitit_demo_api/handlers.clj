(ns reitit-demo-api.handlers
  "ring handlers."
  (:require
   [clojure.java.io :as io]
   [clojure.string :refer [upper-case]]
   [reitit-demo-api.store :as store]))

(defn method-uri [{:keys [request-method uri]}]
  (str (upper-case (name request-method)) " " uri))

(defn get-persons
  [req]
  (tap> (method-uri req))
  (tap> req)
  (let [m (-> req :store store/get-persons)]
    (if (:value m)
      {:status 200
       :body {:persons (:value m)}}
      {:status 503 ;; TODO: decide between 500 or 503
       :body {:error (ex-message (:ex-info m))
              :details (ex-data (:ex-info m))}})))

(defn get-pets
  [req]
  (tap> (method-uri req))
  (tap> req)
  (let [xs (-> req :store store/get-pets)]
    {:status 200
     :body {:pets xs}}))

(defn create-person
  [req]
  (tap> (method-uri req))
  (tap> req)
  (let [params (:body-params req)
        m (store/put-person! (:store req) params)]
    (if (:value m)
      {:status 201
       :body {:person (:value m)}}
      {:status 400
       :body {:error (ex-message (:ex-info m))
              :details (ex-data (:ex-info m))}})))

(defn create-pet
  [req]
  (tap> (method-uri req))
  (tap> req)
  (let [params (:body-params req)
        m (store/put-pet! (:store req) params)]
    (if (:value m)
      {:status 201
       :body {:pet (:value m)}}
      {:status 400
       :body {:error (ex-message (:ex-info m))
              :details (ex-data (:ex-info m))}})))

(defn get-ping
  [req]
  (tap> (method-uri req))
  (tap> req)
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (str "<h1>Pong</h1>")})

(defn hello-world
  [req]
  (let [method (:request-method req)
        headers (:headers req)
        ua (get headers "user-agent")
        cookie (get headers "cookie")
        li-method (str "<li>HTTP request method: " method "</li>")
        li-ua (str "<li>User-Agent: " ua "</li>")
        li-cookie (str "<li>session cookie: " cookie "</li>")
        ul (str "<ul>" li-method li-ua li-cookie "</ul>")]

    (tap> {:user-agent ua :method method :cookie cookie})

    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str "<h1>Hello world!</h1>" ul)}))

(defn handle-plus-query
  [{{{:keys [x y]} :query} :parameters :as req}]
  (tap> (method-uri req))
  (tap> req)
  {:status 200
   :body {:total (+ x y)}})

(defn handle-plus-body
  [{{{:keys [x y]} :body} :parameters :as req}]
  (tap> (method-uri req))
  (tap> req)
  {:status 200
   :body {:total (+ x y)}})

(defn handle-product-query
  [{:keys [parameters] :as req}]
  (tap> (method-uri req))
  (tap> req)
  (let [q (:query parameters)
        x (:x q)
        y (:y q)]
    {:status 200
     :body {:total (* x y)}}))

(defn handle-download
  [_req]
  {:status 200
   :headers {"Content-Type" "image/png"}
   :body (-> "img/reitit.png"
             (io/resource)
             (io/input-stream))})

(defn handle-upload
  [{{{:keys [file]} :multipart} :parameters :as req}]
  (tap> (method-uri req))
  (tap> req)
  (tap> (with-meta
          [:portal.viewer/image file]
          {:portal.viewer/default :portal.viewer/hiccup}))
  {:status 200
   :body {:name (:filename file)
          :size (:size file)}})

(defn not-found
  [req]
  (tap> (method-uri req))
  (tap> req)
  {:status 404
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (str "<h1>Not Found!</h1>")})
