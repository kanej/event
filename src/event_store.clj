(ns event-store)

(defprotocol EventStore
  (commit [this event])
  (get-events [this aggregate-id]))


