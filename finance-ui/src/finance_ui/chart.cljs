(ns finance-ui.chart
  (:require
            [reagent.core :as r]
            ["react-vis" :as rvis]
            [finance-ui.config :as config]
            ))


(defn get-year [row] (js/parseInt ( first (clojure.string/split (:date row) "-"))))

(defn get-month [row] (js/parseInt ( second (clojure.string/split (:date row) "-"))))

(defn get-month-string [arr]
  (js/Date. (str (first arr) "-" (second arr))))


(defn sum-of-array [arr]
  (let [key (first arr)
        sum (reduce + (map js/parseInt (map :sum (second arr))))
        ]
    {:x (get-month-string key) :y sum}
  ))

(defn collect-data [data] (map sum-of-array
                               (sort (group-by (juxt get-year get-month) data))))

(defn get-sum [res]
  (reduce + (map js/parseInt (map :sum res))))

(defn current-year-month []
  [(.getFullYear ( js/Date.))
   (+ (.getMonth ( js/Date.)) 1)])

(defn current-month-sum [data]
  (get-sum
   (get
    (group-by
     (juxt get-year get-month) data)
    (current-year-month))))


(def axis-style {:line {:stroke "#333"
                        :strokeLinecap "square"}
                 :ticks {:stroke "#999"}
                 :text {:stroke "none"
                        :fill "#333"}})

(defn line-chart [data]
  [:> rvis/XYPlot
   {:xType "time"
    :width 800
    :height 225
    :margin {:left 50 :right 50}}
   [:> rvis/XAxis
    {:title "Date"
     :tickTotal (count data)
     :tickSizeInner 0
     :tickSizeOuter 3
     :style axis-style}]
   [:> rvis/YAxis
    {:tickSizeInner 0
     :tickSizeOuter 3
     :style axis-style}]
   [:> rvis/LineSeries
    {:data data
     :color "#e47320"
     :strokeWidth 5
     :style {:fill "none"
             :strokeLinejoin "round"
             :strokeLinecap "round"}}]])

(defn transaction-chart [data]
  [:div
   [line-chart (collect-data data)]])

  
