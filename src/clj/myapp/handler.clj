(ns myapp.handler
  (:require [myapp.middleware :as middleware]
            [myapp.layout :refer [error-page]]
            [myapp.routes.home :as home]
            [myapp.routes.services :as services]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring :as ring]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.webjars :as webjars]
            [myapp.env :refer [defaults]]
            [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (ring/ring-handler
      (ring/router
        ;; routes
        [(home/routes)
         (services/routes)])
      ;; default routes
      (ring/routes
        (swagger-ui/create-swagger-ui-handler
          {:path "/swagger-ui"
           :url "/api/swagger.json"
           :config {:validatorUrl nil}})
        (ring/create-resource-handler
          {:path "/"})
        (content-type/wrap-content-type
          (webjars/wrap-webjars (constantly nil)))
        (ring/create-default-handler
          {:not-found
           (constantly (error-page {:status 404, :title "404 - Page not found"}))
           :method-not-allowed
           (constantly (error-page {:status 405, :title "405 - Not allowed"}))
           :not-acceptable
           (constantly (error-page {:status 406, :title "406 - Not acceptable"}))})))))
