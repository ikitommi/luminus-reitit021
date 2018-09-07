(ns myapp.routes.services
  (:require [reitit.swagger :as swagger]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.schema :as ring-schema]
            [reitit.coercion.schema :as schema-coercion]
            [ring.util.http-response :refer :all]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [myapp.middleware :as middleware]
            [ring.middleware.params :as params]
            [schema.core :as s]
            [clojure.java.io :as io]))

(defn routes []
  ["/api"
   {:middleware [;; query-params & form-params
                 params/wrap-params
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]
    ;; schema-coercion
    :coercion schema-coercion/coercion
    ;; muuntaja instance to for body encoding & decoding
    :muuntaja middleware/muuntaja}

   ["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "my-api"
                            :description "with reitit-ring"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/ping"
    {:get (constantly (ok {:message "ping"}))}]

   ["/files"
    {:swagger {:tags ["files"]}}

    ["/upload"
     {:post {:summary "upload a file"
             :parameters {:multipart {:file ring-schema/TempFilePart}}
             :responses {200 {:body {:name s/Str, :size s/Int}}}
             :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                        {:status 200
                         :body {:name (:filename file)
                                :size (:size file)}})}}]

    ["/download"
     {:get {:summary "downloads a file"
            :swagger {:produces ["image/png"]}
            :handler (fn [_]
                       {:status 200
                        :headers {"Content-Type" "image/png"}
                        :body (-> "public/img/warning_clojure.png"
                                  (io/resource)
                                  (io/input-stream))})}}]]

   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:get {:summary "plus with query parameters"
            :parameters {:query {:x s/Int, :y s/Int}}
            :responses {200 {:body {:total s/Int}}}
            :handler (fn [{{{:keys [x y]} :query} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}
      :post {:summary "plus with body parameters"
             :parameters {:body {:x s/Int, :y s/Int}}
             :responses {200 {:body {:total s/Int}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        {:status 200
                         :body {:total (+ x y)}})}}]]])
