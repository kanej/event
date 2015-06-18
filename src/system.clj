(ns system
  (:require [com.stuartsierra.component :as component]
            [psql.db :as db]
            [psql.schema :as schema]
            [psql.event-store :as psql]
            [durable-queue :as q :refer [take! put! complete!]]
            [event-store :refer [commit get-events]]
            [lillith :as l]

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
      (assoc component :event-store es)))

  (stop [component]
    (assoc component :event-store nil)))


(defrecord lilith-component [command-queue event-store]
  component/Lifecycle

  (start [component]
    (let [lilith (l/init-lilith (:queue command-queue) (:event-store event-store))]
      (assoc component :lilith lilith)))

  (stop [component]
    (l/stop-lilith (:lilith component))
    (assoc component :lilith nil)))


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
         :event-store (map->event-store {})
         :lilith (map->lilith-component {})
         )
        (component/system-using
         {:event-store [:ds]
          :lilith [:command-queue :event-store]})
        )))
