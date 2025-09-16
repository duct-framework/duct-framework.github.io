(ns build
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [integrant.core :as ig]))

(defn- ->asciidoc [s]
  (-> s
      (str/replace #"(?m)^  " "")
      (str/replace #"(\n[^-][^\n]*)\n-" "$1\n\n-")))

(def keywords
  [:duct.logger/simple
   :duct.module/logging])

(ig/load-annotations)

(with-open [writer (io/writer "keywords.adoc")]
  (binding [*out* writer]
    (doseq [kw (sort keywords)]
      (println (str "=== " kw))
      (newline)
      (println (->asciidoc (:doc (ig/describe kw))))
      (newline))))
