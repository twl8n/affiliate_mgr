(ns affiliate-mgr.core
  (:require [clojure.java.jdbc :refer :all]
            [clojure.tools.namespace.repl :as tns]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.params :as rmp])
  (:gen-class))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "affmgr.db"
   })

(defn create-db []
  (try (db-do-commands db
                       (create-table-ddl :news
                                         [:date :text]
                                         [:url :text]
                                         [:title :text]
                                         [:body :text]))
       (catch Exception e (println e))))


(defn xhandler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello Ring! Am I reloadable?"})

(defn handler 
  "Affiliate link manager."
  [request]
  (let [
        res (execute! db ["insert into entry (title,desc) values (?,?)" "Hitachi Compact Impact Driver" "The best tool I own"])
        output (query db ["select * from entry where id=?" 1])]
    (response/response (str "Mod web output:" :request request
                                      :result res
                                      :output output))))
;; (response 
;;     {:status 200
;;      :headers {"Content-Type" "text/plain"}
;;      :body (str "Mod web output:" {:request request
;;                                    :result res
;;                                    :output output})})

;; (defn app
;;   [request]
;;   (handler request ))


;; def, not defn
;; (def app
;;   (-> handler (rmp/wrap-params)))

;; http://localhost:3000/demo?foo=bar&baz=pie

(def app
  (rmp/wrap-params handler))

(defn -main []
  (jetty/run-jetty app {:port 3000}))

