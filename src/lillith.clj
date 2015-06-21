(ns lillith
  (:require [durable-queue :as q :refer [take! put! complete!]]
            [event-store :refer [commit get-events]]
            [clojure.core.async :as a :refer [>! <! >!! <!! go go-loop chan close! alts!!]]
            [event :refer [apply-command map->event-machine map->state-machine process-event]]
            ))

(defprotocol EventSourcingSystem
  (dispatch-command [this command])
  (get-aggregate [this aggregate-id]))

(defrecord lilith [command-queue event-store command-channel l1 l2 state-machine]
  EventSourcingSystem

  (dispatch-command [this command]
    (put! command-queue :command command))

  (get-aggregate [this aggregate-id]
    (let [events (get-events event-store aggregate-id)
          aggregate (reduce #(process-event state-machine %1 %2) {:id aggregate-id} events)]
      aggregate)))

(defn first-loop [command-queue command-channel]
  (go-loop []
    (when-let [message (take! command-queue :command)]
      (>! command-channel message)
      (recur))))

(defn second-loop [command-channel event-store]
  (go-loop []
    (when-let [message (<! command-channel)]
      (commit event-store @message)
      (complete! message)
      (recur))))

(defn init-lilith [command-queue event-store state-machine]
  (let [command-channel (chan)
        l1 (first-loop command-queue command-channel)
        l2 (second-loop command-channel event-store)]
    (map->lilith {:command-queue command-queue
                  :event-store event-store
                  :command-channel command-channel
                  :l1 l1
                  :l2 l2
                  :state-machine state-machine})))

(defn stop-lilith [{:keys [command-queue event-store command-channel l1 l2 state-machine] :as lilith}]
  (close! command-channel)
  (close! l1)
  (close! l2)
  (merge lilith {:command-channel nil :l1 nil :l2 nil}))
