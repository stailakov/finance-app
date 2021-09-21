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

(defn execute [querry] (jdbc/execute! db (sql/format querry){:return-keys ["id" "title" "sum"]}))

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
     :values [[title (Integer/parseInt sum)]]}))


(defn update-transacrion-querry [element]
  (let [{:keys [id title sum]} element]
      {:update :transaction
             :set {:title title :sum sum}
             :where [:= :id id]}))

(defn delete-transacrion-querry [id]
  {:delete-from :transaction
   :where [:= :id (Integer/parseInt id)]})


(defn unwrap [row]
  {
   :id (:transaction/id row)
   :title (:transaction/title row)
   :sum (:transaction/sum row)
   :date (:transaction/date row)
   }
  )


(defn insert-transaction-data [request]
(unwrap(first  (execute (insert-transaction-querry request)))))

(defn update-transaction-data [element]
  (execute (update-transacrion-querry element)))

(defn delete-transaction-data [request]
  (execute (delete-transacrion-querry request)))

(defn update-all [request]
   (let [
        {:keys [body]} request]
     (map update-transaction-data body)))


(defn data-page[entity size page]
  {:data (map unwrap (select-with-paging entity size page))
   :size size
   :page page})

(def req {:body {:title "AAARBUZ" :sum "12"}})

(insert-transaction-data req)

(execute (insert-transaction-querry req))

(data-page :transaction 10 0)

(defn get-transactions-data [request]
  (let [ {:keys [params]} request
       {:keys [size page]} params]
  (data-page :transaction (Integer/parseInt size) (Integer/parseInt page))))


