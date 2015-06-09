(ns event)

(defrecord command-process [aggregate command events])

(defprotocol StateMachine
  (process-command [this aggregate command]))

(defrecord state-machine [dispatchers]
  StateMachine
  (process-command [this aggregate command]
    (when-let [dispatch-fn (get-in this [:dispatchers (:action command)])]
        (dispatch-fn aggregate (:data command)))))

(defprotocol EventProcessor
  (apply-command [this command]))

(defrecord event-machine [state-machine aggregate-store event-store]
  EventProcessor
  (apply-command [this {:keys [aggregate-type aggregate-id] :as command}]
    (if-let [aggregate (get-in this [:aggregate-store aggregate-type aggregate-id])]
      (let [updated-aggregate (process-command (:state-machine this) aggregate command)]
        {:aggregate updated-aggregate :events []})
      {:aggregate nil})))
