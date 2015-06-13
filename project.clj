(defproject event "0.1.0-SNAPSHOT"
  :description "TODO"
  :url "TODO"
  :license {:name "TODO: Choose a license"
            :url "http://choosealicense.com/"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [factual/durable-queue "0.1.5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [org.clojure/java.jdbc "0.3.6"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [hikari-cp "0.13.0"]
                 [yesql "0.4.0"]
                 [clj-liquibase "0.5.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.7"]
                                  [lein-light-nrepl "0.1.0"]]
                   :source-paths ["dev" "test"]
                   :plugins [
                             [cider/cider-nrepl "0.9.0-SNAPSHOT"]
                             [refactor-nrepl "1.0.5"]]
                   :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}}})
