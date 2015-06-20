(ns user
  (:require [clojure.java.io :as io]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [reloaded.repl :refer [system init start stop go reset]]
            [durable-queue :as q :refer [take! put! complete! delete!]]
            [event]
            [event-tests :as evtests]
            [event-store :refer [commit get-events]]
            [psql.event-store :as psql]
            [psql.schema :as schema]
            [psql.db :as db]
            [system :refer [event-system]]
            [lillith :refer [get-aggregate dispatch-command]]))

(reloaded.repl/set-init! #(event-system {}))

;; Example data

(def aid (java.util.UUID/fromString "d7cf3e35-05b1-42a6-8016-d5a82cb7b394"))

(def example-commands [{:aggregate-type :quiz :aggregate-id aid :action :init     :data {:answers [0 0 0 0 0]}}
                       {:aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 0 :value 1}}
                       {:aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 1 :value 1}}
                       {:aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 2 :value 1}}
                       {:aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 3 :value 1}}
                       {:aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 4 :value 1}}
                       {:aggregate-type :quiz :aggregate-id aid :action :complete :data nil}])

;; Helpers

(defn dispatch-commands [commands]
  (let [lilith (-> system :lilith :lilith)]
    (loop [commands commands]
      (when-let [command (first commands)]
        (dispatch-command lilith (first commands))
        (recur (rest commands))))))

(defn commit-course-event [data]
  (let [command {:aggregate-type "course" :aggregate-id aid :action :init :data data}
        lilith (-> system :lilith :lilith)]
    (dispatch-command lilith command)))

;;(commit-course-event {:title "Ancient Philosophy"})

(defn show-events [aggregate-id]
  (get-aggregate (-> system :lilith :lilith) aggregate-id))
