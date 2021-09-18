(ns finance_server.data
  (:require
   [honeysql.core :as sql]
   [next.jdbc :as jdbc]
   )
  )

(def db-config
  {:dbtype "postgresql"
   :dbname "finance"
   :host "localhost"
   :user "postgres"
   :password "postgres"})

(def db (jdbc/get-datasource db-config))

(defn execute [querry] (jdbc/execute! db (sql/format querry)))

(defn page-offset [size page]
  (* size page))

(defn select-with-paging [entity size page]
  (let [res {:select [:*]
             :from [entity]
             :limit size
             :offset (page-offset size page)
             }]
    (execute res)))

(defn data-page[entity size page]
  {:data (select-with-paging entity size page)
   :size size
   :page page})


(defn get-transactions-data [request]
  (let [ {:keys [params]} request
       {:keys [size page]} params]
  (data-page :transaction (Integer/parseInt size) (Integer/parseInt page))))

;(data-page :transaction 10 0)


;;(reduce + (map :transaction/sum (:data (data-page :transaction 10 0))))






