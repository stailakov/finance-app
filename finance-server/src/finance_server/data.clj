(ns finance_server.data
  (:require
   [honeysql.core :as sql]
   [next.jdbc :as jdbc]))

(def db-config
  {:dbtype "postgresql"
   :dbname "finance"
   :host "localhost"
   :user "postgres"
   :password "postgres"})

(def db (jdbc/get-datasource db-config))

(defn execute [querry] (jdbc/execute! db (sql/format querry){:return-keys ["id" "title" "sum" "date"]}))

(defn page-offset [size page]
  (* size page))

(defn select-with-paging [entity size page]
  (let [res {:select [:*]
             :from [entity]
             :limit size
             :offset (page-offset size page)
             }]
    (execute res)))

(defn get-count []
  (:count (first (execute {:select [:%count.*]
             :from [:transaction]
             }))))

(defn parse-date [string]
  (java.time.LocalDate/parse string))

(defn cast [c x]
  (sql/call :cast x (sql/raw (name c))))

(defn ->date [x]
  (cast :date x))


(defn insert-transaction-querry [request]
  (let [{:keys [body]} request
        {:keys [title sum date]} body]
    {:insert-into :transaction
     :columns [:title :sum :date]
     :values [[title (Integer/parseInt sum) (->date date)]]}))

(defn update-transacrion-querry [element]
  (let [{:keys [id title sum date]} element]
      {:update :transaction
             :set {:title title :sum (Integer/parseInt sum) :date (->date date)}
             :where [:= :id id]}))

(defn delete-transacrion-querry [id]
  {:delete-from :transaction
   :where [:= :id (Integer/parseInt id)]})

(defn date-from-inst [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") date))

(defn cut-date [m]
  (assoc m :date (date-from-inst (m :date))))

(defn unwrap [row]
  {
   :id (:transaction/id row)
   :title (:transaction/title row)
   :sum (:transaction/sum row)
   :date (:transaction/date row)
   })

(defn insert-transaction-data [request]
(cut-date (unwrap(first  (execute (insert-transaction-querry request))))))

(defn update-transaction-data [element]
  (execute (update-transacrion-querry element)))

(defn delete-transaction-data [request]
  (execute (delete-transacrion-querry request)))

(defn update-all [request]
   (let [
        {:keys [body]} request]
     (map update-transaction-data body)))

(defn data-page[entity size page]
  {:data (map cut-date (map unwrap (select-with-paging entity size page)))
   :size size
   :page page
   :count (get-count)})

(defn get-transactions-data [request]
  (let [ {:keys [params]} request
       {:keys [size page]} params]
  (data-page :transaction (Integer/parseInt size) (Integer/parseInt page))))

