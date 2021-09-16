(defproject health_crud_server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "1.0.0"]
                 [korma "0.5.0-RC1"]
                 [com.h2database/h2 "1.3.170"]
                 [org.postgresql/postgresql "42.2.18"]
                 [ring "1.8.2"]
                 [ring/ring-json "0.3.1"]
                 [ring-cors "0.1.13"]
                 [compojure "1.1.6"]
                 [ring/ring-jetty-adapter "1.8.2"]]
  :ring {:handler health-crud-server.core/app
         :auto-refresh? true}
  :uberjar-name "app-standalone.jar"
  :profiles {:uberjar {:aot [finance-app.core]}})
