(ns finance-server.core
      (:require [clojure.set :refer [rename-keys]]
            [korma.db :refer [defdb]]
            [korma.core :refer :all])
)

(defdb korma-db
  {:classname "org.postgresql.Driver"
                 :subprotocol "postgresql"
                 :subname "//localhost:5432/finance"
                 :user "postgres"
   :password "postgres"})

(defentity transaction)

(select transaction)


(defn page-offset [size page]
  (* size page))


(defn select-with-paging [entity size page field dir]
  (let [res (select
                   entity
                   (limit size)
                   (offset (page-offset size page))
                   (order field dir))]
    res))

(select-with-paging transaction 10 0 :title :desc)







