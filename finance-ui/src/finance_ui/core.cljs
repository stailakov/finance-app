(ns finance-ui.core
  (:require [clojure.walk :as walk]
            [reagent.core :as r]
            [reagent-material-ui.colors :as colors]
            [reagent-material-ui.core.button :refer [button]]
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
            [finance-ui.datatable :as data]
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


(defn add-row-to-state [element]
  (let [{:keys [id]} element]
    (do (swap! state assoc id (r/atom element)))
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

(defn delete-user-by-id [id]
  (do (http/delete-user id)
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


(defn delete-user-handler []
  (do (doall (map delete-user-by-id @checkbox-edit-state))
      (hidden-header-buttons)
      (data/dec-count)))


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

(defn update-user-handler []
  (do (doall (@create-function))
      (hidden-header-buttons)))

(defn create-update-request []
    (for [id @checkbox-edit-state]
      (swap! update-request conj @(@state id))))

(defn update-users []
  (do (doall (create-update-request))
      (http/update-user-request @update-request update-user-handler)))

(def update-button [button {:children "UPDATE"
                            :color :primary
                            :variant :outlined
                            :on-click #(update-users)
                            }])


(def delete-button [button {:children "DELETE"
                            :color :secondary
                            :variant :contained
                            :on-click #(delete-user-handler)
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


(defn add-user-handler [response]
  (do
     (doall (create-component-row-new (walk/keywordize-keys response)))
      (reset! form-state nil)
      (reset! str-state nil)
      (data/inc-count)
      ))


(def new-user-form
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
                    :on-click #(http/add-user @str-state add-user-handler)
                    }]
   [button {:children "CANCEL"
                    :color :primary
                    :variant :outlined
                    :on-click #(reset! form-state nil)
                    }]])

(defn get-user-handler [response]
  (let [{:keys [data count]} response]
    (do
      (data/update-page-state :count count)
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
  (http/get-data get-user-handler))

(defn change-rows-per-page [new-page]
  (do (data/update-page-state :size new-page)
      (reset! component-state nil)
      (reset! state nil)
        (reset! edit-component-state nil)
        (reset! simple-component-state nil)
        (fetch-data)))


(defn change-page [new-page]
  (do (data/update-page-state :page new-page)
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
              (create-cells-header (:columns config/user))]]
            (body-t)
            [table-footer
             [table-row @edit-row-state]
             [table-row
              [table-pagination {:on-change-page (fn [event page] (change-page page))
                                 :on-change-rows-per-page #(change-rows-per-page (-> % .-target .-value))
                                 :rows-per-page (data/page-state-value :size)
                                 :count (data/page-state-value :count)
                                 :page (data/page-state-value :page)}]]]]))


(defn new-page []
  (fn [] [:span.main
          [:h1 (str "Patient list")]
          [:div [button {:children "NEW"
                         :color :primary
                         :variant :outlined
                         :on-click #(reset! form-state new-user-form)
                         }]
           (@header-buttons-state :update)
           (@header-buttons-state :delete)
           ]
          [grid
           @form-state]

          [grid {:item true}
           [(data-table)]
           ]
          
           [grid (ch/transaction-chart)]
           
           ]))



(defn start []
  (do
      (r/render-component [new-page]
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
  
