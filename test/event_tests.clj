(ns event-tests
  (:require [clojure.test :refer :all]
            [event :refer [apply-command map->event-machine map->state-machine process-event]])
  (:import [event event-machine]))

(defn create-quiz [entity data]
  (merge entity {:answers [0 0 0 0 0 ]}))

(defn update-answer [quiz {:keys [answer value]}]
  (assoc-in quiz [:answers answer] value))

(defn complete-quiz [quiz data]
  (assoc quiz :status :complete))

(def aid (java.util.UUID/fromString "d7cf3e35-05b1-42a6-8016-d5a82cb7b394"))

(def quiz {:id aid :answers [0 0 0 0 0]})

(def quiz-state-machine (map->state-machine {:dispatchers {:init create-quiz
                                                           :answer update-answer
                                                           :complete complete-quiz}}))

(def quiz-machine (map->event-machine {:state-machine quiz-state-machine
                                       :aggregate-store {:quiz {aid quiz}}}))

;; (defn apply-command [event-system command]
;;   (-> command
;;       validate-command
;;       get-aggregate
;;       dispatch-command
;;       commit-events))

(deftest process-a-command
  (let [command {:id 1 :aggregate-type :quiz :aggregate-id aid :action :answer :data {:answer 0 :value 2}}
        {:keys [aggregate]} (apply-command quiz-machine command)]
    (is (= [2 0 0 0 0] (:answers aggregate)))))

(deftest process-multiple-commands
  (let [commands [{:id 0 :aggregate-type :quiz :aggregate-id aid :action :init     :data {:answers [0 0 0 0 0]}}
                  {:id 1 :aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 0 :value 1}}
                  {:id 2 :aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 1 :value 1}}
                  {:id 3 :aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 2 :value 1}}
                  {:id 4 :aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 3 :value 1}}
                  {:id 5 :aggregate-type :quiz :aggregate-id aid :action :answer   :data {:answer 4 :value 1}}
                  {:id 6 :aggregate-type :quiz :aggregate-id aid :action :complete :data nil}]
        aggregate (reduce #(process-event quiz-state-machine %1 %2) {:id aid} commands)]
    (is (= [1 1 1 1 1] (:answers aggregate)))))
