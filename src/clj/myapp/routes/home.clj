(ns myapp.routes.home
  (:require [myapp.layout :as layout]
            [clojure.java.io :as io]
            [myapp.middleware :as middleware]
            [ring.util.http-response :as response]))

(defn home-page [_]
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [_]
  (layout/render "about.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])

