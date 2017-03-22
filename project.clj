(defproject affiliate_mgr "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.8.10"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [ring "1.5.0"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]]
  ;; You need a :ring with lein ring or lein ring server-headless
  ;; However, you must send the request through the function that wraps the handler with
  ;; with wrap-params, and any other wrap-* decorators.
  :ring {:handler affiliate-mgr.core/app}

  ;; Note hyphen, affiliate-mgr even though our path is affiliate_mgr
  :main ^:skip-aot affiliate-mgr.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
