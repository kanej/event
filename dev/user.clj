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
            [durable-queue :as q :refer [take! put! complete!]]
            [event]
            [event-tests :as evtests]
            [event-store :refer [commit get-events]]
            [psql.event-store :as psql]
            [psql.schema :as schema]
            [psql.db :as db]
            [clojure.core.async :as a :refer [>! <! >!! <!! go go-loop chan]]))

(def ds (schema/make-postgres-datasource))

(def command-queue (q/queues "/tmp" {}))

(def command-channel (chan))

(def event-queue (q/queues "/tmp" {}))

(def event-store (psql/psql-event-store ds))

(go-loop []
  (let [message (take! command-queue :command)]
    (>! command-channel message)
    (recur)))

(go-loop []
  (let [message (<! command-channel)]
    (commit event-store @message)
    (complete! message)
    (recur)))

(defn command-stats []
  (q/stats command-queue))

(defn event-stats []
  (q/stats event-queue))

;; Helpers

(def aid (java.util.UUID/randomUUID))

(defn commit-course-event [data]
  (let [command {:aggregate-type "course" :aggregate-id aid :data data}]
    (put! command-queue :command command)))

;;(commit-course-event {:title "Ancient Philosophy"})

(defn show-events []
  (get-events event-store "a95e33c5-2d96-4132-b92b-7488f235fb5d"))
