(ns psql.db
  (:require [clojure.java.jdbc :as jdbc]
            [yesql.core :refer [defquery]]
            [psql.schema :as schema]
            [clojure.edn :as edn]))

(defquery commit-event<! "queries/commit_event.sql")

(defn commit-event [{:keys [aggregate-type aggregate-id data] :as event}]
  (let [datasource (schema/make-postgres-datasource)]
    (jdbc/with-db-transaction [connection {:datasource datasource}]
       (commit-event<! connection aggregate-type aggregate-id (prn-str data)))))
