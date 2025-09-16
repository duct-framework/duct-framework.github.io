(ns build
  (:require [clojure.java.shell :as sh]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [integrant.core :as ig]))

(defn- ->asciidoc [s]
  (:out (sh/sh "pandoc" "-t" "asciidoc" :in s)))

(defn- space-out-lists [s]
  (str/replace s #"(\n[^-][^\n]*)\n-" "$1\n\n-"))

(defn- trim-indent [s]
  (str/replace s #"(?m)^  " ""))

(def keywords
  [:duct.database/sql
   :duct.database.sql/hikaricp
   :duct.logger/simple
   :duct.module/cljs
   :duct.module/logging
   :duct.module/sql
   :duct.module/web
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
