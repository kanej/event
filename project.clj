(defproject event "0.1.0-SNAPSHOT"
  :description "TODO"
  :url "TODO"
  :license {:name "TODO: Choose a license"
            :url "http://choosealicense.com/"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [factual/durable-queue "0.1.5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [yesql "0.4.0"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.7"]]
                   :source-paths ["dev" "test"]}})
