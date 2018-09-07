(ns user
  (:require [myapp.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [myapp.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'myapp.core/repl-server))

(defn stop []
  (mount/stop-except #'myapp.core/repl-server))

(defn restart []
  (stop)
  (start))


