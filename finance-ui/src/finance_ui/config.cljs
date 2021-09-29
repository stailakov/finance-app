(ns finance-ui.config)

(def properties {
           :columns ["ID" "Title" "Sum" "Date"]
                 :fields [:id :title :sum :date]
                 :server-url "http://localhost:3000/transaction/"
           }
  )


