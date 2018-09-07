(ns myapp.routes.home
  (:require [myapp.layout :as layout]
            [clojure.java.io :as io]
            [myapp.middleware :as middleware]))

(defn home-page [_]
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [_]
  (layout/render "about.html"))

(defn home-routes []
  [""
   {:no-doc true ;; don't collect to swagger-docs
    :middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])

