(ns psql.event-store
  (:require [event-store :refer [EventStore commit get-events]]
            [psql.db :as db]))

(defrecord postgres-event-store [ds]
  EventStore
  (commit [this event]
    (db/commit-event ds event))
  (get-events [this aggregate-id]
    (db/get-events-for-aggregate {:datasource ds} aggregate-id)))

(defn psql-event-store [ds]
  (->postgres-event-store ds))
