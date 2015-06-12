(ns psql.schema
  (:require
    [hikari-cp.core :refer :all]
    [clojure.java.jdbc :as jdbc]

    [clj-liquibase.change :as ch]
    [clj-liquibase.cli    :as cli])
  (:use
    [clj-liquibase.core :only (defchangelog)]))

(def datasource-options {:auto-commit        true
                         :read-only          false
                         :connection-timeout 30000
                         :idle-timeout       600000
                         :max-lifetime       1800000
                         :minimum-idle       10
                         :maximum-pool-size  10
                         :adapter            "postgresql"
                         :username           "eve";;(env :username)
                         :password           "eve";;(env :password)
                         :database-name      "eventstore";;(env :database-name)
                         :server-name        "localhost";;(env :server-name)
                         :port-number        5432;;(env :port-number)
                         })

(defn make-postgres-datasource
  ([]
    (make-datasource datasource-options))
  ([server-name port-number database-name username password]
    (let [overrides {:server-name server-name
                     :port-number port-number
                     :database-name database-name
                     :username username
                     :password password}
          options (merge datasource-options overrides)]
      (make-datasource options))))


(def create-events-table
  (ch/create-table
    :events
    [[:id             :int           :null false :pk true :autoinc true]
     [:aggregate-type [:varchar 150] :null false]
     [:aggregate-id   :uuid          :null false]
     [:data           [:varchar 500] :null false]]))

(defchangelog
  app-changelog
  "eventstore"
  [["id=1"  "author=kanej" [create-events-table]]])

(defn migrate!
  ([]
    (apply cli/entry "update" {:datasource (make-postgres-datasource) :changelog  app-changelog} nil))
  ([{:keys [server-name port-number database-name username password]}]
    (apply cli/entry "update" {:datasource (make-postgres-datasource server-name port-number database-name username password) :changelog  app-changelog} nil)))

(defn rollback!
  ([]
   (apply cli/entry "rollback" {:datasource (make-postgres-datasource) :changelog  app-changelog :chs-count "1"} nil)))

(defn -main
  [& [cmd & args]]
  (apply cli/entry cmd {:datasource (make-postgres-datasource) :changelog  app-changelog} args))
