(set-env!
 :source-paths #{ "src/html" "src/cljs"}
 :resource-paths #{"html"}
 :dependencies '[
                 [org.clojure/clojure "1.8.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.9.473"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.228-2"]
                 [pandeiro/boot-http "0.7.6"]
                 [adzerk/boot-reload "0.5.1"]
                 [adzerk/boot-cljs-repl "0.3.0"]       ;; add bREPL
                 [quil "2.6.0"]
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 [org.clojars.magomimmo/domina "2.0.0-SNAPSHOT"]
                 [hiccups "0.3.0"]
                 [compojure "1.5.2"]                   ;; for routing
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.3"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1"]
                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [org.clojars.magomimmo/valip "0.4.0-SNAPSHOT"]
                 [hoplon/hoplon             "6.0.0-alpha17"]
                 [enlive "1.1.6"]
                 [adzerk/boot-test "1.2.0"]
                 [crisptrutski/boot-cljs-test "0.2.1-SNAPSHOT"]
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[boot.core          :as    core]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[clojure.java.io    :as    io]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-test :refer [test]]
         '[boot.util          :as    util]
         '[hoplon.boot-hoplon       :refer [hoplon prerender]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(def defaults {:test-dirs #{"test/cljc"}
               :output-to "main.js"
               :testbed :phantom
               :namespaces '#{solar_concentrator.core}} )

(defn- copy [tf dir]
  (let [f (core/tmp-file tf)]
    (util/with-let [to (doto (io/file dir (:path tf)) io/make-parents)]
      (io/copy f to))))

(deftask add-source-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (merge-env! :source-paths dirs)
  identity)

(deftask tdd
  "Launch a customizable TDD Environment"
  [e testbed        ENGINE kw     "the JS testbed engine (default phantom)"
   k httpkit               bool   "Use http-kit web server (default jetty)"
   n namespaces     NS     #{sym} "the set of namespace symbols to run tests in"
   o output-to      NAME   str    "the JS output file name for test (default main.js)"
   O optimizations  LEVEL  kw     "the optimization level (default none)"
   p port           PORT   int    "the web server port to listen on (default 3000)"
   t dirs           PATH   #{str} "test paths (default test/clj test/cljs test/cljc)"
   v verbose               bool   "Print which files have changed (default false)"]
  (let [dirs        (or dirs (:test-dirs defaults))
        output-to   (or output-to (:output-to defaults))
        testbed     (or testbed (:testbed defaults))
        namespaces  (or namespaces (:namespaces defaults))]
    (comp
     (serve
      ;; :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true
            ;; :httpkit httpkit
            ;; :port port
            )
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload :ws-host "localhost")
     (cljs-repl)
     (test-cljs :out-file output-to
                :js-env testbed
                :namespaces namespaces
                :update-fs? true
                :optimizations optimizations)
     (test :namespaces namespaces)
     (target :dir #{"target"}))))

(deftask insert
  "Inject content in new file"
  [f file FILE str "FileSet root-relative path of the HTML file to tinker with."
   c chunk JAVASCRIPT str "JavaScript files to inject as <script> tags in <head>."]
  (assert (and file (seq chunk)) "inject: file and scripts are required arguments")
  (let [tgt (core/tmp-dir!)]
    (core/with-pre-wrap [fs]
      (core/empty-dir! tgt)
      (util/info "Injecting %s into %s...\n" chunk file)
      (if-let [html-file (first (by-path [file] (core/input-files fs)))]
        (let [f   (copy html-file tgt)
              txt (slurp f)
              chunk-file (first (by-path [chunk] (core/input-files fs)))
              f2   (copy chunk-file tgt)
              chunk-content (slurp f2)
              ]
          (spit f (.replaceFirst txt "container" chunk-content))
          (-> fs
              (core/rm [html-file])
              (core/add-resource tgt)
              core/commit!))
        fs))))

(deftask dev
  "Launch immediate feedback dev environment"
  []
  (comp
   (watch)
   (hoplon)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs :optimizations :none)
   (insert :file "index.html" :chunk "chunk.html")
   (target :dir #{"target"})
   (serve :dir "target")))

(deftask django
  "Launch immediate feedback dev environment"
  []
  (comp
   (watch)
   (hoplon)
   (reload)
   (cljs :optimizations :advanced)
   (insert :file "solar.html" :chunk "chunk.html")
   (target :dir #{"target"})))
