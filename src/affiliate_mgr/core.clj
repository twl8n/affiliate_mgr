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

(defn mya
  "Loop only for side effect. The str/replace is done, but the value is only printed, not saved. stracc and
  rex are always passed back in the recur, and only the index is modified."
  []
  (loop [stracc "xxxa yyya yb" 
         rex [#"x" #"y"]
         index 0]
    (if (> index 100)
      (println "hit: " index)
      (recur 
       (let [cs (str/replace stracc (first rex) "zz")]
         (println index "is string: " cs)
         stracc)
       rex
       (inc index)))))

(defn myb
  "Same speed as (myz). Call the body in a let binding so we can return before the index is incremented an extra time. Return with
  index 1 unlike (mya) and (myz) which return with index 2. Loop over regex seq rex (consuming rex), modifying string
  accumulator stracc. Use an index."
  []
  (loop [stracc "xxxa yyya yb" 
         rex [#"x" #"y"]
         index 0]
    (let [cs (str/replace stracc (first rex) "zz")
          rexr (rest rex)]
      (println index "is string: " cs)
      (if (empty? rexr)
        (do
          ;; This print raises the timing on 10 invocations from 33 to 47 ms.
          (println "Final index: " index)
          cs)
        (recur cs 
               rexr
               (inc index))))))
  
  (defn myz
  "Loop over regex seq rex (consuming rex), modifying string accumulator stracc. Also inc an index so we can print debug info."
  []
  (loop [stracc "xxxa yyya yb" 
         rex [#"x" #"y"]
         index 0]
    (if (empty? rex)
        (do
          ;; This print raises the timing on 10 invocations from 33 to 47 ms.
          (println "Final index: " index)
          stracc)
      (recur (let [cs (str/replace stracc (first rex) "zz")]
               (println index "is string: " cs)
               cs)
             (rest rex) 
             (inc index)))))

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

(defn pq [xx] (java.util.regex.Pattern/quote xx))

(defn cstr [str] (str/replace (with-out-str (pprint str)) #"\n" "\n"))

(defn dev-init []
  (def myrec '({:id 1, :title "Hitachi Compact Impact Driver", :desc "The best tool I own", :stars nil, :isbn nil}))
  (def mytpl (slurp "edit.html")))

;; [string map] returning modified string
;; (seq) the map into a sequence of k v
(defn map-re
  "Replaced placeholders in the orig template with keys and values from the map remap. This is the functional 
equivalent of using regexes to change a string in place."
  [orig remap]
  (loop [ostr orig
         [[label value] & remainder] (seq remap)]
    (if (nil? label)
      ostr
      (recur (str/replace ostr (re-pattern (pq (str "{{" label "}}"))) (str value)) remainder))))

(defn list-all [params]
  (query db ["select * from entry order by id"]))

;; (let [[_ pre body post] (re-matches #"(.*?)\{\{for\}\}(.*?)\{\{end\}\}(.*)$" "pre{{for}}middle{{end}}post")] {:pre pre :body body :post post})
;; {:pre "pre", :body "middle", :post "post"}

(defn fill-list-all
  "Fill in a list of all records. The regex must use (?s) so that newline matches .
Initialize with empty string, map-re on the body, and accumulate all the body strings."
  [rseq]
  (let [template (slurp "list-all.html")
        [all pre body post] (re-matches #"(?s)^(.*?)\{\{for\}\}(.*?)\{\{end\}\}(.*)$" template)]
    (str (map-re pre {:_msg "List all from db"})
         (loop [full ""
                remap rseq]
           (prn full)
           (if (empty? remap)
               full
             (recur (str full (map-re body (first remap))) (rest remap))))
         post)))

(defn edit
  "Map each key value in the record against placeholders in the template to create a web page."
  [record]
  (let [template (slurp "edit.html")
        body (map-re template record)]
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

