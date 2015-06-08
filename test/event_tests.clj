(ns event-tests
  (:require [clojure.test :refer :all]
            [event :refer [apply-command map->event-machine map->state-machine]])
  (:import [event event-machine]))

(defn update-answer [quiz {:keys [answer value]}]
  (assoc-in quiz [:answers answer] value))

(def quiz {:id 1 :answers [0 0 0 0 0]})

(def quiz-machine (map->event-machine {:state-machine (map->state-machine {:dispatchers {:answer update-answer}})
                                       :aggregate-store {:quiz {1 quiz}}}))

;; (defn apply-command [event-system command]
;;   (-> command
;;       validate-command
;;       get-aggregate
;;       dispatch-command
;;       commit-events))

(deftest a-test
  (let [command {:id 1 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 0 :value 2}}
        {:keys [aggregate]} (apply-command quiz-machine command)]
    (is (= [2 0 0 0 0] (:answers aggregate)))))
