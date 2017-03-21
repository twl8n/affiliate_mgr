# affiliate_mgr

;; http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html

;; https://github.com/clojure/tools.namespace
;; (tns/refresh)

;; Oddly, the last answer might be most useful.
http://stackoverflow.com/questions/1665760/compojure-development-without-web-server-restarts

;; Example of empty params (not quite the same as no :params at all)
http://stackoverflow.com/questions/12990634/compojure-route-params-empty
https://gist.github.com/jamiei/1f74b817ca5d306af9f3

;; Partial example. No mention of dependencies.
;; https://gist.github.com/weavejester/585921

#### project.clj and :ring

https://github.com/weavejester/lein-ring

If you want to 'lein ring' or 'lein ring server-headless' then project.clj must have a :ring option However,
the option must send the request through the function that wraps the handler with with wrap-params, and any other
wrap-* decorators. If you want to wrap your request, then you must not send the request directly to the handler.

```
;; project.clj
:ring {:handler affiliate-mgr.core/app}

;; core.clj
(defn handler [] ...)

;; def, not defn, interestingly.
(def app
  (rmp/wrap-params handler))
```

If you use 'lein run' then you don't need a :ring config in project.clj Note the hyphen, affiliate-mgr even
though our path is affiliate_mgr (underscore). 

If you do use :ring and 'lein ring', the following :ring option is wrong, since it routes the http request to
handler directly, skipping a wrapper, even if there's a wrapper specified in the run-jetty call.

```
;; project.clj
:ring {:handler affiliate-mgr.core/handler} ; wrong for at least 2 reasons!

;; core.clj
(defn handler [] ...)

(def app
  (rmp/wrap-params handler))

;; -main is only called if you 'lein run', but ignored by 'lein ring'.
(defn -main []
  (jetty/run-jetty app {:port 3000}))
```


Test POST requests:

```
wget -S "http://localhost:3000/demox" --post-data 'cake=pie' -O - | less
```

#### Errors you hope to never see

Lein namespaces use hyphen, even though your project name has an underscore.

```
(ns affiliate-mgr.core ...

"Cannot find anything to run for: affiliate_mgr.core"
:main affiliate_mgr.core

No such namespace: affiliate_mgr.core
:ring {:handler affiliate_mgr.core/handler}
```


## Usage

FIXME: explanation

    $ java -jar affiliate_mgr-0.1.0-standalone.jar [args]

Copyright © 2017 Tom Laudeman


