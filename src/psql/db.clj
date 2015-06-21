(ns psql.db
  (:require [clojure.java.jdbc :as jdbc]
            [yesql.core :refer [defquery]]
            [psql.schema :as schema]
            [clojure.edn :as edn]))

(defquery commit-event<! "queries/commit_event.sql")

(defquery get-events-for-aggregate-query "queries/get_events_for_aggregate.sql")

(defn commit-event [ds {:keys [aggregate-type aggregate-id action data] :as event}]
  (jdbc/with-db-transaction [connection {:datasource ds}]
    (commit-event<! connection (name aggregate-type) aggregate-id (name action) (prn-str data))))

(defn get-events-for-aggregate [ds aggregate-id]
  (let [db {:datasource ds}
        results (get-events-for-aggregate-query db aggregate-id)
        convert (fn [{:keys [id aggregate_type aggregate_id action data] :as result}]
                  {:id id
                   :aggregate-type (keyword aggregate_type)
                   :aggregate-id aggregate_id
                   :action (keyword action)
                   :data (edn/read-string data)})]
    (map convert results)))
