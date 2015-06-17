(ns lillith
  (:require [durable-queue :as q :refer [take! put! complete!]]))

(defprotocol EventSourcingSystem
  (process-command [this command])
  (get-aggregate [this aggregate-id]))

(defrecord lillith [command-queue event-store]
  EventSourcingSystem

  (process-command [this command]
    (put! command-queue :command command))

  (get-aggregate [this aggregate-id]
    nil)
