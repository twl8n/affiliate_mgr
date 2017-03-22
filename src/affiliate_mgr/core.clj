(ns affiliate-mgr.core
  (:require [clojure.java.jdbc :refer :all]
            [clojure.tools.namespace.repl :as tns]
            [clojure.string :as str]
            [ring.adapter.jetty :as ringa]
            [ring.util.response :as ringu]
            [ring.middleware.params :as ringm])
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

(defn show [params]
    (let [output (query db ["select * from entry where id=?" 1])]
      output))

(defn choose [params]
  (let [title (params "title")]
    (cond (not (nil? title))
          (query db ["select * from entry where title like ? limit 1" (format "%%%s%%" title)]))))

(defn pq [xx] (java.util.regex.Pattern/quote xx))

;; ({:id 1, :title "Hitachi Compact Impact Driver", :desc "The best tool I own", :stars nil, :isbn nil})
(defn edit [record]
  (let [template (slurp "edit.html")]
    (map #(str/replace template (re-pattern (str "{{" (pq %) "}}")) record))

(defn handler 
  "Affiliate link manager."
  [request]
  (let [params (:params request)
        rmap (cond (= "show" (params "action"))
                   (show params)
                   (= "choose" (params "action"))
                   (choose params)
                   :else
                   {:error "unknown action"})]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (with-out-str (prn rmap))}))


    ;; (response/response (str "Mod web output:"
    ;;                         :request request
    ;;                         :result res
    ;;                         :output output))))

(def app
  (ringm/wrap-params handler))

;; Need -main for 'lien run', but it is ignored by 'lein ring'.
(defn -main []
  (ringa/run-jetty app {:port 3000}))

