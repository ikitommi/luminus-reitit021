(ns myapp.middleware
  (:require [myapp.env :refer [defaults]]
            [cognitect.transit :as transit]
            [clojure.tools.logging :as log]
            [myapp.layout :refer [error-page]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [muuntaja.core :as muuntaja]
            [muuntaja.middleware :refer [wrap-format wrap-params]]
            [myapp.config :refer [env]]
            [ring.middleware.flash :refer [wrap-flash]]
            [immutant.web.middleware :refer [wrap-session]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]])
  (:import
    [com.fasterxml.jackson.datatype.joda JodaModule]
    [org.joda.time ReadableInstant]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))

(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(def muuntaja
  (muuntaja/create
    (-> muuntaja/default-options
        (assoc-in
          [:formats "application/json" :opts :modules]
          [(JodaModule.)])
        (assoc-in
          [:formats "application/transit+json" :encode-opts]
          {:handlers {org.joda.time.DateTime joda-time-writer}}))))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format muuntaja))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))
      wrap-internal-error))
