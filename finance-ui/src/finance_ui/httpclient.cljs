(ns finance-ui.httpclient
  (:require
   [ajax.core :refer [GET POST PUT DELETE]]
   [finance-ui.config :as config]
   [finance-ui.statemanager :as state]
   ))

(defn get-data [handler]
  (GET (:server-url config/properties)
       {:params {:size (state/page-state-value :size)
                 :page (state/page-state-value :page)}
        :handler #(handler %)
        :response-format :json
        :keywords? true
        :error-handler (fn [{:keys [status status-text]}]
                              (js/console.log status status-text))})
  )

(defn add-transaction [user handler]
  (POST (:server-url config/properties)
        {:params user
         :format :json
         :handler #(handler %)
         :error-handler (fn [{:keys [status status-text]}]
                              (js/console.log status status-text))
             })
  )

(defn update-transactions [user handler]
  (PUT (:server-url config/properties)
        {:params user
         :format :json
         :handler #(handler)
         :error-handler (fn [{:keys [status status-text]}]
                              (js/console.log status status-text))
             })
  )

(defn delete-transaction [id]
  (DELETE (str (:server-url config/properties) id)
        {:format :json
         :error-handler (fn [{:keys [status status-text]}]
                              (js/console.log status status-text))
             })
  )
