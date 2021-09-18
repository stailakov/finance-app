(ns finance_server.core
  (:require [compojure.route :as route])
  (:use [ring.adapter.jetty :as jetty]
        ring.middleware.content-type
        ring.middleware.session
        [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.middleware.keyword-params :refer [wrap-keyword-params]]
        [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
        ring.util.response
        compojure.core
        compojure.handler
        [finance_server.data :as data])
  (:gen-class))


(defn get-transactions [request]
  {:status 200
   :body (data/get-transactions-data request)})

(defroutes compojure-handler
  (GET "/transaction" request (get-transactions request))
  (route/not-found "<h1>Not found!</h1>"))

(defn wrap-json [handler] (-> handler
             wrap-json-response
             (wrap-json-body {:keywords? true})))

(def app
  (wrap-defaults
   (wrap-json compojure-handler) site-defaults))


(defn -main []
  (jetty/run-jetty
   app
   {:port 3000
    :join? false}))

;(def server (-main))
;(.stop server)

