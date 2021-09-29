(ns finance-ui.statemanager
  (:require [reagent.core :as r]))

(def page-state (r/atom (hash-map :size 10 :page 0)))

(defn page-state-value [key]
  (@page-state key))

(defn update-page-state [key value]
  (swap! page-state assoc key value))

(defn inc-count []
  (let [current (@page-state :count)]
  (update-page-state :count (inc current))))

(defn dec-count []
  (let [current (@page-state :count)]
  (update-page-state :count (dec current))))

