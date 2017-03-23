(ns affiliate-mgr.core
  (:require [clojure.java.jdbc :refer :all]
            [clojure.tools.namespace.repl :as tns]
            [clojure.string :as str]
            [clojure.pprint :refer :all]
            [ring.adapter.jetty :as ringa]
            [ring.util.response :as ringu]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]])
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

(defn show [params]
    (let [output (query db ["select * from entry where id=?" 1])]
      output))

(defn choose [params]
  (let [title (params "title")]
    (cond (not (nil? title))
          (query db ["select * from entry where title like ? limit 1" (format "%%%s%%" title)]))))

(defn pq [xx] (java.util.regex.Pattern/quote xx))

(defn cstr [str] (str/replace (with-out-str (pprint str)) #"\n" "\n"))

(def myrec '({:id 1, :title "Hitachi Compact Impact Driver", :desc "The best tool I own", :stars nil, :isbn nil}))

(defn edit [record]
  (let [template (slurp "edit.html")
        body (map (fn [[kk vv]] (str/replace template (re-pattern (pq (str "{{" kk "}}"))) (str vv))) (first record))]
  (prn body)
  body))

(defn handler 
  "Affiliate link manager."
  [request]
  (let [params (:params request)
        ras  request
        rmap (cond (= "show" (params "action"))
                   (show params)
                   (= "choose" (params "action"))
                   (choose params))]
    (if (some? rmap)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (edit (first rmap))}
      (ringu/content-type 
       (ringu/response 
        (str "<html><body><pre>" (cstr ras) "</pre></body></html>")) "text/html"))))

(def app
  (wrap-multipart-params (wrap-params handler)))

;; Need -main for 'lien run', but it is ignored by 'lein ring'.
(defn -main []
  (ringa/run-jetty app {:port 3000}))

