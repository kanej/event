(ns psql.db
  (:require [clojure.java.jdbc :as jdbc]
            [yesql.core :refer [defquery]]
            [psql.schema :as schema]
            [clojure.edn :as edn]))

(defquery commit-event<! "queries/commit_event.sql")

(defquery get-events-for-aggregate "queries/get_events_for_aggregate.sql")

(defn commit-event [ds {:keys [aggregate-type aggregate-id action data] :as event}]
  (jdbc/with-db-transaction [connection {:datasource ds}]
    (commit-event<! connection (name aggregate-type) aggregate-id (name action) (prn-str data))))
