(ns event-tests
  (:require [clojure.test :refer :all]
            [event :refer [apply-command map->event-machine map->state-machine process-command]])
  (:import [event event-machine]))

(defn create-quiz [entity data]
  (merge entity {:answers [0 0 0 0 0 ]}))

(defn update-answer [quiz {:keys [answer value]}]
  (assoc-in quiz [:answers answer] value))

(defn complete-quiz [quiz data]
  (assoc quiz :status :complete))

(def quiz {:id 1 :answers [0 0 0 0 0]})

(def quiz-state-machine (map->state-machine {:dispatchers {:init create-quiz
                                                           :answer update-answer
                                                           :complete complete-quiz}}))

(def quiz-machine (map->event-machine {:state-machine quiz-state-machine
                                       :aggregate-store {:quiz {1 quiz}}}))

;; (defn apply-command [event-system command]
;;   (-> command
;;       validate-command
;;       get-aggregate
;;       dispatch-command
;;       commit-events))

(deftest process-a-command
  (let [command {:id 1 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 0 :value 2}}
        {:keys [aggregate]} (apply-command quiz-machine command)]
    (is (= [2 0 0 0 0] (:answers aggregate)))))

(deftest process-multiple-commands
  (let [commands [{:id 0 :aggregate-type :quiz :aggregate-id 1 :action :init :data {:answers [0 0 0 0 0]}}
                  {:id 1 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 0 :value 1}}
                  {:id 2 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 1 :value 1}}
                  {:id 3 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 2 :value 1}}
                  {:id 4 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 3 :value 1}}
                  {:id 5 :aggregate-type :quiz :aggregate-id 1 :action :answer :data {:answer 4 :value 1}}
                  {:id 6 :aggregate-type :quiz :aggregate-id 1 :action :complete :data nil}]
        aggregate (reduce #(process-command quiz-state-machine %1 %2) {:id 1} commands)]
    (is (= [1 1 1 1 1] (:answers aggregate)))))
