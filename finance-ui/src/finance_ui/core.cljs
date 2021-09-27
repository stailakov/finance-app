(ns finance-ui.core
  (:require [clojure.walk :as walk]
            [cljs.reader :as reader]
            [reagent.core :as r]
            [reagent-material-ui.colors :as colors]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.table :refer [table]]
            [reagent-material-ui.core.table-head :refer [table-head]]
            [reagent-material-ui.core.table-body :refer [table-body]]
            [reagent-material-ui.core.table-footer :refer [table-footer]]
            [reagent-material-ui.core.table-row :refer [table-row]]
            [reagent-material-ui.core.table-cell :refer [table-cell]]
            [reagent-material-ui.core.table-pagination :refer [table-pagination]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.core.grid :refer [grid]]
            [reagent-material-ui.core.input :refer [input]]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.select :refer [select]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.icons.edit :refer [edit]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [ajax.core :refer [GET POST PUT DELETE]]
            [finance-ui.config :as config]
            [finance-ui.chart :as ch]
            [finance-ui.httpclient :as http]
            [finance-ui.statemanager :as state]
            ))

(def body (r/atom nil))
(def state (r/atom (hash-map)))
(def component-state (r/atom (hash-map)))
(def edit-component-state (r/atom (hash-map)))
(def simple-component-state (r/atom (hash-map)))
(def header-buttons-state (r/atom (hash-map)))
(def edit-row-state (r/atom nil))
(def form-state (r/atom nil))
(def str-state (r/atom (hash-map)))
(def response-state (r/atom nil))
(def update-request (r/atom #{}))

(def create-function (r/atom nil))
(def checkbox-edit-state (r/atom #{}))

(defn state-array [st] (for [[k v] st] @v))


(defn add-row-to-state [element]
  (let [{:keys [id]} element]
    (swap! state assoc id (r/atom element))
    ))

(defn update-component [id component]
   (swap! component-state assoc id component))

(defn update-component-simple [id component]
   (swap! simple-component-state assoc id component))

(defn update-component-edit [id component]
   (swap! edit-component-state assoc id component))

(defn remove-row [id] (swap! state dissoc id))

(defn log [& args]
  (.apply js/console.log js/console (to-array args)))

(defn appl [f]
  (.apply js/console.log js/console (f)))


(defn create-cells-header [names]
  (let [res []]
  (for [v names]
    (conj res (fn [] [table-cell v])))))

(defn delete-transaction-by-id [id]
  (do (http/delete-transaction id)
      (swap! state dissoc id)
      (swap! component-state dissoc id)
      (swap! edit-component-state dissoc id)
      (swap! simple-component-state dissoc id)
      (swap! checkbox-edit-state disj id)
      ))

(defn hidden-header-buttons []
  (swap! header-buttons-state assoc :update nil)
  (swap! header-buttons-state assoc :delete nil))

(defn hidden-header-buttons-if-all-unchecked [state]
  (if (== 0 (count state))
    (hidden-header-buttons)))

(defn check-if-hid-needed []
  (fn [] hidden-header-buttons-if-all-unchecked))


(defn delete-transaction-handler []
  (do (mapv delete-transaction-by-id @checkbox-edit-state)
      (hidden-header-buttons)
      (state/dec-count)))


(defn create-editable-cells [row]
  (let [{:keys [id full_name sex birth_date address oms_number]} row]
    (fn [] [table-row
            [checkbox {:checked true
                       :on-change (fn [] (do (swap! component-state assoc id (@simple-component-state id))
                                        ;                                            (hidden-header-buttons)
                                             (hidden-header-buttons-if-all-unchecked (swap! checkbox-edit-state disj id))
                                             ))}
             ]         
            [table-cell id]
            [table-cell
             [text-field {:type "text"
                   :value (@(@state id) :title)
                     :on-change #(swap! (@state id) assoc :title (-> % .-target .-value))}]
             ]
            [table-cell
             [text-field {:type "text"
                   :value (@(@state id) :sum)
                     :on-change #(swap! (@state id) assoc :sum (-> % .-target .-value))}]
             ]
            [table-cell
             [text-field {:type "date"
                          :value (@(@state id) :date)
                          :on-change #(swap! (@state id) assoc :date (-> % .-target .-value))}]
             ]
            ])))

(defn update-component-row [id]
  (let [cells (create-editable-cells id)]
    (update-component id (r/atom cells))))

(defn update-transactions-handler []
  (do (doall (@create-function))
      (hidden-header-buttons)))

(defn create-update []
  (reduce conj [] ))

(defn get-state-by-id [id]
  @(@state id))

(defn update-transactions []
  (let [request (map get-state-by-id @checkbox-edit-state)] 
      (http/update-transactions request update-transactions-handler)))

(def update-button [button {:children "UPDATE"
                            :color :primary
                            :variant :outlined
                            :on-click #(update-transactions)
                            }])


(def delete-button [button {:children "DELETE"
                            :color :secondary
                            :variant :contained
                            :on-click #(delete-transaction-handler)
                            }])

(defn checkbox-handler [id]
  (do (swap! checkbox-edit-state conj id)
      (swap! component-state assoc id (@edit-component-state id))
      (swap! header-buttons-state assoc :update update-button)
      (swap! header-buttons-state assoc :delete delete-button))
  )


(defn create-cells [row]
  (let [{:keys [id title sum date]} row]
    [table-row
     [table-cell {:padding "checkbox"}
      [checkbox {:on-change (fn [] (checkbox-handler id))}]
      ]
     [table-cell id]
     [table-cell title]
     [table-cell sum]
     [table-cell date]
     ]
     ))


(defn create-component-row [row]
  (let [{:keys [id]} row
        cells (create-cells row)]
    (do
      (update-component id (r/atom cells))
      (update-component-simple id (r/atom cells))
      )))

(defn create-component-row-to-edit-state [row]
  (let [{:keys [id]} row
        cells (create-editable-cells row)]
    (update-component-edit id (r/atom cells))))

(defn create-components-rows []
    (for [[k v] @state]
      (do (create-component-row-to-edit-state @v)
          (create-component-row @v))
      ))


(defn create-component-row-new [row]
  (do
    (add-row-to-state row)
    (create-component-row-to-edit-state row)
    (create-component-row row)))


(defn add-transaction-handler [response]
  (do
     (doall (create-component-row-new (walk/keywordize-keys response)))
      (reset! form-state nil)
      (reset! str-state nil)
      (state/inc-count)
      ))

(defn parse-timestamp
  [ts]
  (if-let [[years months days hours minutes seconds ms offset]
           (reader/parse-and-validate-timestamp ts)]
    (js/Date.
      (- (.UTC js/Date years (dec months) days hours minutes seconds ms)
        (* offset 60 1000)))
    (throw (js/Error. (str "Unrecognized date/time syntax: " ts)))))


(def input-transaction-form
  [table-row
   [table-cell [text-field {:helper-text "Title"
                :on-change #(swap! str-state assoc :title (-> % .-target .-value))}]]
   [table-cell [text-field {:helper-text "Sum"
                :on-change #(swap! str-state assoc :sum (-> % .-target .-value))}]]
    
   [table-cell [text-field {:type "date"
                            :input-label-props {:format "YYYY-MM-DD"}
                            :helper-text "Date"
                :on-change #(swap! str-state assoc :date (-> % .-target .-value))}]]
           [button {:children "ADD"
                    :color :primary
                    :variant :outlined
                    :on-click #(http/add-transaction @str-state add-transaction-handler)
                    }]
   [button {:children "CANCEL"
                    :color :primary
                    :variant :outlined
                    :on-click #(reset! form-state nil)
                    }]])

(defn get-transaction-handler [response]
  (let [{:keys [data count]} response]
    (do
      (state/update-page-state :count count)
     (doall (map add-row-to-state data))
     (doall (create-components-rows))
     )
    ))


(defn show-rows []
(let [res []]
  (for [[k v] @component-state]
    (conj res (fn [] @v)
    ))))

(defn body-t []
  [table-body
   (show-rows)])


(defn fetch-data []
  (http/get-data get-transaction-handler))

(defn change-rows-per-page [new-page]
  (do (state/update-page-state :size new-page)
      (reset! component-state nil)
      (reset! state nil)
        (reset! edit-component-state nil)
        (reset! simple-component-state nil)
        (fetch-data)))


(defn change-page [new-page]
  (do (state/update-page-state :page new-page)
      (reset! component-state nil)
      (reset! state nil)
        (reset! edit-component-state nil)
        (reset! simple-component-state nil)
        (fetch-data)))


  (defn data-table []
    (fn [] [table
            [table-head
             [table-row
              [table-cell]
              (create-cells-header (:columns config/properties))]]
            (body-t)
            [table-footer
             [table-row @edit-row-state]
             [table-row
              [table-pagination {:on-change-page (fn [event page] (change-page page))
                                 :on-change-rows-per-page #(change-rows-per-page (-> % .-target .-value))
                                 :rows-per-page (state/page-state-value :size)
                                 :count (state/page-state-value :count)
                                 :page (state/page-state-value :page)}]]]]))


(defn data-page []
  (fn [] (let [arr @state
               local-arr (state-array arr)]
           [:span.main
          [:h1 (str "My Transactions")]
          
          
             [grid 
                        [table-row
                         [table-cell (ch/transaction-chart local-arr)]
                         [table-cell [:h3 (str "Current month: "
                                          (ch/current-month-sum local-arr) " EAU")]]
                         
             ]]

          [grid {:item true}
           [(data-table)]
           ]

            [:div [button {:children "NEW"
                         :color :primary
                         :variant :outlined
                         :on-click #(reset! form-state input-transaction-form)
                         }]
           (@header-buttons-state :update)
           (@header-buttons-state :delete)
           ]
          [grid
           @form-state]

            ])))

(count @state)
(defn start []
  (do
      (r/render-component [data-page]
                                (. js/document (getElementById "app")))
      (fetch-data)
      (reset! create-function create-components-rows)
      ))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
   (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
  
