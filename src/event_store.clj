(ns event-store)

(defprotocol EventStore
  (commit [this event]))

(defrecord postgres-event-store [db]
  EventStore
  (commit [this event]
    (println event)))

(defn psql-event-store [db]
  (->postgres-event-store db))
