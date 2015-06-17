(ns system
  (:require [com.stuartsierra.component :as component]
            [psql.db :as db]
            [psql.schema :as schema]
            [psql.event-store :as psql]
            [durable-queue :as q :refer [take! put! complete!]]
            [event-store :refer [commit get-events]]

            [clojure.core.async :as a :refer [>! <! >!! <!! go go-loop chan close! alts!!]]
            [meta-merge.core :refer [meta-merge]]
            [clojure.pprint :refer [pprint]])
  (:import [psql.event_store postgres-event-store])
  )

(defrecord command-queue [config]
  component/Lifecycle

  (start [component]
    (let [{:keys [dir]} config
          queue (q/queues dir {})]
      (assoc component :queue queue)))

  (stop [component]
    (.close (:queue component))
    (assoc component :queue nil)))

(defrecord datasource [config]
  component/Lifecycle

  (start [component]
    (let [{:keys [server-name port-number database-name username password]} config
          datasource (schema/make-postgres-datasource server-name port-number database-name username password)]
      (assoc component :ds datasource)))

  (stop [component]
    (.shutdown (:ds component))
    (assoc component :ds nil)))


(defrecord event-store [ds]
  component/Lifecycle

  (start [component]
    (let [es (psql/->postgres-event-store (:ds ds))]
      (assoc component :es es)))

  (stop [component]
    (assoc component :es nil)))


(defn first-loop [command-queue command-channel]
  (go-loop []
           (when-let [message (take! (:queue command-queue) :command)]
             (>! command-channel message)
             (recur))))

(defn second-loop [command-channel event-store]
  (go-loop []
           (when-let [message (<! command-channel)]
             (println message)
             (commit event-store @message)
             (complete! message)
             (recur))))

(defrecord lillith [command-queue es command-channel l1 l2]
  component/Lifecycle

  (start [component]
    (let [command-channel (chan)

          l1 (first-loop command-queue command-channel)

          l2 (second-loop command-channel (:es es))]

      (merge component {:command-channel command-channel :l1 l1 :l2 l2})))

  (stop [component]
    (close! command-channel)
    (close! l1)
    (close! l2)
    (merge component {:command-channel nil :l1 nil :l2 nil})))


(def base-config
  {:ds {:server-name "localhost"
        :port-number 5432
        :database-name "eventstore"
        :username "eve"
        :password "eve"}
   :command-queue {:dir "/tmp"}})

(defn event-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :command-queue (->command-queue (:command-queue config))
         :ds (->datasource (:ds config))
         :es (map->event-store {})
         :lillith (map->lillith {})
         )
        (component/system-using
         {:es [:ds]
          :lillith [:command-queue :es]})
        )))
