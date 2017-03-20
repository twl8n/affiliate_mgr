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


