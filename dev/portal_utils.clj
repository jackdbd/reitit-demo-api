(ns portal-utils
  (:require [muuntaja.core :as m]
            [portal.api :as p]
            [portal.console :as log]))

(defn tap-json>
  [app request-method uri params]
  (tap> request-method)
  (tap> params)
  (let [tmp {:request-method request-method :uri uri}
        req (if (= :get request-method)
              (assoc tmp :query-params params)
              (assoc tmp :body-params params))
        res (app req)
        decoded (m/decode "application/json" (:body res))]
    (tap> decoded)))

(defn tap-html>
  [app request-method uri]
  (let [req {:request-method request-method, :uri uri}
        res (app req)]
    (tap> (with-meta
            [:portal.viewer/html (:body res)]
            {:portal.viewer/default :portal.viewer/hiccup}))))

(defn tap-image>
  [app request-method uri]
  (let [req {:request-method request-method, :uri uri}
        res (app req)
        buffer-input-stream (:body res)
        bytes (.readAllBytes buffer-input-stream)]
    (tap> (with-meta
            [:portal.viewer/image bytes]
            {:portal.viewer/default :portal.viewer/hiccup}))))
  
(defn init! []
  (println "Launch Portal")
  (p/open {:window-title "Portal UI"})
  (add-tap #'p/submit)
  (tap> "Portal ready to receive taps"))

(defn close! []
  (println "Close Portal")
  ;; should I call remove-tap ?
  (p/close))

(comment
  (init!)
  (log/debug "hello world")
  (tap> ^{:portal.viewer/default :portal.viewer/hiccup} [:h1 "hello, world"])
  (close!)
  )

(comment
  ;; print classpath in Portal
  (->> (java.lang.System/getProperty "java.class.path")
       (re-seq #"[^;]+")
       (map tap>)
       dorun))