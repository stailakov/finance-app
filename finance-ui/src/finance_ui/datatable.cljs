(ns finance-ui.datatable
  (:require
            [reagent.core :as r]
            [reagent-material-ui.core.table :refer [table]]
            [reagent-material-ui.core.table-head :refer [table-head]]
            [reagent-material-ui.core.table-body :refer [table-body]]
            [reagent-material-ui.core.table-footer :refer [table-footer]]
            [reagent-material-ui.core.table-row :refer [table-row]]
            [reagent-material-ui.core.table-cell :refer [table-cell]]
            [reagent-material-ui.core.table-pagination :refer [table-pagination]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.core.input :refer [input]]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.select :refer [select]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.icons.edit :refer [edit]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [finance-ui.config :as config]
            ))

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

