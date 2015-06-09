(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [durable-queue :as q :refer [take! put! complete!]]
   [event]
   [event-tests :as evtests]
   [clojure.core.async :as a :refer [>! <! >!! <!! go go-loop chan]]))

(def command-queue (q/queues "/tmp" {}))

(def command-channel (chan))

(def event-queue (q/queues "/tmp" {}))

(go-loop []
  (let [message (take! command-queue :command)]
    (>! command-channel message)
    (recur)))

(go-loop []
  (let [message (<! command-channel)]
    (complete! message)
    (recur)))

(defn command-stats []
  (q/stats command-queue))

(defn event-stats []
  (q/stats event-queue))
