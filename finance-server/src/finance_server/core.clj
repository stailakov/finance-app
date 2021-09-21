(ns finance_server.core
  (:require [compojure.route :as route])
  (:use [ring.adapter.jetty :as jetty]
        ring.middleware.content-type
        ring.middleware.session
        [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.middleware.keyword-params :refer [wrap-keyword-params]]
        [ring.middleware.resource :refer [wrap-resource]]
        [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
        ring.util.response
        compojure.core
        compojure.handler
        [finance_server.data :as data])
  (:gen-class))


(defn get-transactions [request]
  {:status 200
   :body (data/get-transactions-data request)})

(defn insert-transaction [request]
  {:status 200
   :body (data/insert-transaction-data request)})

(defn update-transaction [request]
  {:status 200
   :body (data/update-all request)})

(defn delete-transaction [id]
  {:status 200
   :body (data/delete-transaction-data id)})

(defroutes compojure-handler
  (GET "/transaction" request (get-transactions request))
  (POST "/transaction" request (insert-transaction request))
  (PUT "/transaction" request (update-transaction request))
  (DELETE "/transaction/:id" [id] (delete-transaction id))
  (route/not-found "<h1>Not found!</h1>"))

(def app (-> compojure-handler
             wrap-json-response
             (wrap-defaults
              (assoc-in site-defaults [:security :anti-forgery] false))
             (wrap-json-body {:keywords? true})
             (wrap-cors :access-control-allow-origin [#".*"] :access-control-allow-methods [:get :post :delete :put])))


(defn -main []
  (jetty/run-jetty
   app
   {:port 3000
    :join? false}))

(def server (-main))
(.stop server)

