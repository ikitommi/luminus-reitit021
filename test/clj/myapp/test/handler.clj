(ns myapp.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [myapp.handler :refer :all]
            [myapp.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [mount.core :as mount]))

(defn parse-json [body]
  (m/decode muuntaja/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'myapp.config/env
                 #'myapp.handler/app)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "api-route"
    (testing "success"                 
      (let [response (app (request :get "/api/math/plus" {:x 1, :y 2}))]
        (is (= 200 (:status response)))  
        (is (= {:total 3} (-> response :body parse-json)))))
    (testing "failure"
      (let [response (app (request :get "/api/math/plus" {:x 1, :y "kikka"}))]
        (is (= 400 (:status response)))))))
