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
  (sort [:duct.compiler.cljs.shadow/release
         :duct.compiler.cljs.shadow/server
         :duct.database/sql
         :duct.database.sql/hikaricp
         :duct.handler/file
         :duct.handler/reitit
         :duct.handler/resource
         :duct.handler/static
         :duct.handler.static/ok
         :duct.handler.static/bad-request
         :duct.handler.static/not-found
         :duct.handler.static/method-not-allowed
         :duct.handler.static/internal-server-error
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
         :duct.repl/refers
         :duct.router/reitit
         :duct.scheduler/simple
         :duct.server.http/jetty]))

(defn- namespace->path [kw]
  (-> (namespace kw) (str/replace #"\." "/") (str/replace #"-" "_")))

(defn- keyword->path [kw]
  (str (namespace->path kw) "/" (-> (name kw) (str/replace #"-" "_"))))

(defn- find-resource [kw]
  (or (io/resource (str (keyword->path kw) ".clj"))
      (io/resource (str (namespace->path kw) ".clj"))))

(def ^:private re-artifact-path
  #"\/\.m2/repository/(.*)/(.*?)/(.*?)/(.*?)\.jar\!")

(defn- get-dependency [path]
  (let [[_ group artifact version _] (re-find re-artifact-path (str path))]
    (str (str/replace group #"/" ".") "/" artifact
         " {:mvn/version \"" version "\"}")))

(defn- get-fragment [kw]
  (-> kw str (str/replace #"[:.-]" "_") (str/replace #"/" "")))

(ig/load-annotations)

(with-open [writer (io/writer "docs/keywords.adoc")]
  (binding [*out* writer]
    (println "[.concise-index]")
    (println ".Index")
    (println "****")
    (doseq [kw keywords]
      (println (str "- <<" (get-fragment kw) ">>")))
    (println "****")
    (newline)
    (doseq [kw keywords]
      (let [doc (-> kw ig/describe :doc trim-indent
                    space-out-lists ->asciidoc)]
        (println "[discrete]")
        (println "###" kw)
        (newline)
        (println (str "`" (get-dependency (find-resource kw)) "`"))
        (newline)
        (println doc)))))

(shutdown-agents)
