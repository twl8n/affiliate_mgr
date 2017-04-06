# affiliate_mgr

* need to implement the state machine

* need to implement deft templates, or start using  clostache

clj only?
https://github.com/fhd/clostache

clj and cljs?
https://github.com/fotoetienne/cljstache

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



#### regexp hints, looping, atom

;; recur inside fn
((fn [x] (if (= x 0) (print "done") (recur (do (print x) (dec x))))) 10)

(def zz (atom "foo"))
(swap! zz #(clojure.string/replace % #"f" "g")))

(def zz "foo bxx byyy")
(doseq [rex [#"x" #"y"]] (def zz (clojure.string/replace zz rex "a")))

(defn rreg [mystr rex]
  (if (empty? rex)
    mystr
    (rreg (clojure.string/replace mystr (first rex) "a") (rest rex))))
(rreg "foo bxx byy" [#"x" #"y"])

(def zz "foo bxx byy")
(doseq [rex [#"x" #"y"]] (def zz (clojure.string/replace zz rex "a")))

;; or plain (map) works in a repl, but need (dorun (map ...)) in code where execution is lazy
(dorun (map #(def zz (clojure.string/replace zz % "a")) [#"x" #"y"]))

;; run! added in clojure 1.7
(run! #(def zz (clojure.string/replace zz % "a")) [#"x" #"y"])

(defn myz []
  (loop [ii "xxxa yyya" rex [#"x" "aa"  #"y" "bb"]]
    (if (empty? rex)
      ii
      (recur (str/replace ii (first rex) (second rex)) (nthrest rex 2)))))

;; Accumulate ii, returning it when rex is consumed.
(defn myz []
  (loop [ii "xxxa yyya" rex [#"x" #"y"]]
    (if (empty? rex)
      ii
      (recur (str/replace ii (first rex) "zz") (rest rex)))))

# Verbose, and still needs an if to halt recursion.
(transduce (map identity) (fn [aa & bb] (if (some? bb) (clojure.string/replace aa (first bb) "a") aa)) "foo bxx byy" [#"x" #"y"])

(defn myz []
  (loop [ii "xxx yyy" rex [#"x" #"y"]]
    (if (some? (first rex))
      (do
      (recur (str/replace ii (first rex) "zz") (rest rex))) ii)))

(loop [ii "xxx yyy" rex [#"x" #"y"]] (when (some? rex) (recur (str/replace ii (first rex) "xx") (rest rex))))

(loop [i [#"x" #"y"]] (when (some? i) (recur ((prn i) (rest i)))))

(loop [i (range 5)] (when (not (nil? i)) (recur (prn i))))

https://clojure.org/about/functional_programming#_recursive_looping

(defn myz [keys]
  (loop [my-keys (seq keys)]
    (if (some? (first my-keys))
      (do
      (prn (first my-keys))
      (recur (rest my-keys)))
      my-keys)))


(defn my-zipmap [keys vals]
  (loop [my-map {}
         my-keys (seq keys)
         my-vals (seq vals)]
    (if (and my-keys my-vals)
      (recur (assoc my-map (first my-keys) (first my-vals))
             (next my-keys)
             (next my-vals))
      my-map)))
(my-zipmap [:a :b :c] [1 2 3])
-> {:b 2, :c 3, :a 1}

(defn pq [xx] (java.util.regex.Pattern/quote xx))

(defn rep [xx mmap]
  (map 
  (let [regexp (re-pattern (pq (str "{{" xx "}}")))]
    {:xx xx
     :string (clojure.string/replace "test {{:foo}}" regexp (mmap xx))
     :mmap mmap}))

(rep :foo {:foo "one" :bar "two"})
;; {:xx :foo, :string "test one", :mmap {:foo "one", :bar "two"}}

;; repeatedly apply regexp to a string to create a map.
;; Not what I need, but has some similar qualities
user> (let [x "layout: default\ntitle: Envy Labs"]
        (reduce (fn [h [_ k v]] (assoc h k v))
                {}
                (re-seq #"([^:]+): (.+)(\n|$)" x)))
;; {"title" "Envy Labs", "layout" "default"}


  (let [matcher (re-matcher #"\((\w+)\):(\d+)" "(fish):1 sausage (cow):3 tree (boat):4")]
    (loop [match (re-find matcher)
           lst []]
      (if match
        (recur (re-find matcher) (conj lst (str (second match) (nth match 2))))
        lst)))

;; perl, only 3 lines and easier to read
while ($text =~ /\((\w+)\):(\d+)/g) {
  push @list, "$1$2"
}
  


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


