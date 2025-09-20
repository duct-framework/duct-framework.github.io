(ns build
  (:require [clojure.java.shell :as sh]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [integrant.core :as ig]))

(defn- ->asciidoc [s]
  (:out (sh/sh "pandoc" "-t" "asciidoc" :in s)))

(defn- space-out-lists [s]
  (str/replace s #"(?m)^([^-][^\n]*)\n-" "$1\n\n-"))

(defn- trim-indent [s]
  (str/replace s #"(?m)^  " ""))

(def keywords
  [:duct.compiler.cljs.shadow/release
   :duct.compiler.cljs.shadow/server
   :duct.database/sql
   :duct.database.sql/hikaricp
   :duct.handler/reitit
   :duct.logger/simple
   :duct.middleware.web/log-requests
   :duct.middleware.web/log-errors
   :duct.middleware.web/hide-errors
   :duct.middleware.web/defaults
   :duct.middleware.web/webjars
   :duct.middleware.web/stacktrace
   :duct.middleware.web/hiccup
   :duct.migrator/ragtime
   :duct.module/cljs
   :duct.module/logging
   :duct.module/sql
   :duct.module/web
   :duct.router/reitit
   :duct.server.http/jetty])

(ig/load-annotations)

(with-open [writer (io/writer "keywords.adoc")]
  (doseq [kw (sort keywords)]
    (let [doc (-> kw ig/describe :doc trim-indent space-out-lists ->asciidoc)]
      (binding [*out* writer]
        (println "###" kw)
        (newline)
        (println doc)))))

(shutdown-agents)
