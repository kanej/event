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
            [durable-queue :as q :refer [take! put! complete!]]
            [event]
            [event-tests :as evtests]
            [event-store :refer [commit get-events]]
            [psql.event-store :as psql]
            [psql.schema :as schema]
            [psql.db :as db]
            [system :refer [event-system]]))

(reloaded.repl/set-init! #(event-system {}))

;; Helpers

(def aid (java.util.UUID/randomUUID))

(defn commit-course-event [data]
  (let [command {:aggregate-type "course" :aggregate-id aid :data data}
        command-queue (-> system :command-queue :queue) ]
    (put! command-queue :command command)))

;;(commit-course-event {:title "Ancient Philosophy"})

(defn show-events []
  (get-events (:es (:es system)) "c90a4775-3e2b-4a8d-bf98-255ace3534b6"))
