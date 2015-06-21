(ns event)

(defrecord command-process [aggregate command events])

(defprotocol StateMachine
  (process-event [this aggregate event]))

(defrecord state-machine [dispatchers]
  StateMachine
  (process-event [this aggregate event]
    (when-let [dispatch-fn (get-in this [:dispatchers (:action event)])]
        (dispatch-fn aggregate (:data event)))))

(defprotocol EventProcessor
  (apply-command [this command]))

(defrecord event-machine [state-machine aggregate-store event-store]
  EventProcessor
  (apply-command [this {:keys [aggregate-type aggregate-id] :as command}]
    (if-let [aggregate (get-in this [:aggregate-store aggregate-type aggregate-id])]
      (let [updated-aggregate (process-event (:state-machine this) aggregate command)]
        {:aggregate updated-aggregate :events []})
      {:aggregate nil})))
