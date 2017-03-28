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
  (let [id (params "id")
        output (query db ["select * from entry where id=?" id])]
    (spit "show_debug.txt" (with-out-str (prn "id: " id " out: " output)))
    output))

(defn choose [params]
  (let [title (params "title")]
    (cond (not (nil? title))
          (query db ["select * from entry where title like ? limit 1" (format "%%%s%%" title)]))))

(defn update-db [params]
  (let [id (params "id")
        title (params "title")
        stars (params "stars")
        desc (params "desc")]
    (cond (not (nil? (params "id")))
          (do
            (execute! db ["update entry set title=?, desc=?, stars=? where id=?" title desc stars id])))))

(defn list-all [params]
  (query db ["select * from entry order by id"]))

;; (let [[_ pre body post] (re-matches #"(.*?)\{\{for\}\}(.*?)\{\{end\}\}(.*)$" "pre{{for}}middle{{end}}post")] {:pre pre :body body :post post})
;; {:pre "pre", :body "middle", :post "post"}

(defn fill-list-all [rseq]
  (let [template (slurp "list-all.html")
        [_ pre body post] (re-matches #"(.*?)\{\{for\}\}(.*?)\{\{end\}\}(.*)$" template)]
    ;; Need to loop the maps in rseq over body with map-re, then (str pre (apply str body-seq) post)

    ;;looping over a block of html means it is time to
    ;;add clostash templates or deft-templates.

    template))

(defn pq [xx] (java.util.regex.Pattern/quote xx))

(defn cstr [str] (str/replace (with-out-str (pprint str)) #"\n" "\n"))

(defn dev-init []
  (def myrec '({:id 1, :title "Hitachi Compact Impact Driver", :desc "The best tool I own", :stars nil, :isbn nil}))
  (def mytpl (slurp "edit.html")))

;; string map
;; (seq) the map into a sequence of k v
(defn map-re [orig remap]
  (loop [ostr orig
         [[label value] & remainder] (seq remap)]
    (if (some? label)
      (do
      (recur (str/replace ostr (re-pattern (pq (str "{{" label "}}"))) (str value)) remainder)) ostr)))

;; (map (fn [[kk vv]] (str/replace template (re-pattern (pq (str "{{" kk "}}"))) (str vv))) record)
(defn edit [record]
  (let [template (slurp "edit.html")
        body (map-re template record)]
  (spit "body_debug.html" body)
  body))

(defn handler 
  "Affiliate link manager."
  [request]
  (let [params (:params request)
        action (params "action")
        ras  request
        rmap (cond (= "show" action)
                   (map #(assoc % :_msg "read from db") (show params))
                   (= "choose" action)
                   (choose params)
                   (= "update-db" action)
                   (do 
                     (update-db params)
                     (map #(assoc % :_msg "updated") (show params)))
                   (= "list-all" action)
                   (list-all params))]
    #_(spit "rmap_debug.txt" (with-out-str (prn "rmap: " rmap)))
    (cond (some? rmap)
          (cond (or (= "show" action)
                    (= "update-db" action))
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (edit (first rmap))}
                    (= "list-all" action)
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (fill-list-all rmap)})
          :else
          (ringu/content-type 
           (ringu/response 
            (str "<html><body><pre>" (cstr ras) "</pre></body></html>")) "text/html"))))

(def app
  (wrap-multipart-params (wrap-params handler)))

;; Need -main for 'lien run', but it is ignored by 'lein ring'.
(defn -main []
  (ringa/run-jetty app {:port 3000}))

