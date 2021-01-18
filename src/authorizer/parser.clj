(ns authorizer.parser
    (:require
      [clojure.data.json :as json]
      [clojure.string :as str]
      ))

(defn stdin-transactions [file-name] (str/split (slurp file-name) #"\n"))

(defn read-transactions [transactions]
      (map #(json/read-str % :key-fn keyword) transactions))
