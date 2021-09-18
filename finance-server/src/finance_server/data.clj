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

(defn insert-transaction-querry [request]
  (let [{:keys [body]} request
        {:keys [title sum]} body]
    {:insert-into :transaction
     :columns [:title :sum]
     :values [[title sum]]}))

(defn update-transacrion-querry [request]
  (let [
        {:keys [body]} request
        {:keys [id title sum]} body]
    {:update :transaction
             :set {:title title :sum sum}
             :where [:= :id id]}))

(defn delete-transacrion-querry [id]
  {:delete-from :transaction
   :where [:= :id (Integer/parseInt id)]})

(defn insert-transaction-data [request]
  (execute (insert-transaction-querry request)))

(defn update-transaction-data [request]
  (execute (update-transacrion-querry request)))

(defn delete-transaction-data [request]
  (execute (delete-transacrion-querry request)))

(defn data-page[entity size page]
  {:data (select-with-paging entity size page)
   :size size
   :page page})

(defn get-transactions-data [request]
  (let [ {:keys [params]} request
       {:keys [size page]} params]
  (data-page :transaction (Integer/parseInt size) (Integer/parseInt page))))


