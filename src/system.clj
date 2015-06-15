(ns system
  (:require [com.stuartsierra.component :as component]
            [psql.db :as db]
            [psql.schema :as schema]
            [psql.event-store :as psql]

            [meta-merge.core :refer [meta-merge]]
            [clojure.pprint :refer [pprint]])
  (:import [psql.event_store postgres-event-store])
  )

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


(def base-config
  {:ds {:server-name "localhost"
        :port-number 5432
        :database-name "eventstore"
        :username "eve"
        :password "eve"}})

(defn event-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :ds (->datasource (:ds config))
         :es (map->event-store {})
         )
        (component/system-using
         {:es [:ds]})
        )))
