(ns authorizer.adapters-test
    (:require [clojure.test :refer :all]
              [authorizer.db :as db]
              [authorizer.interpreter :as i]
              [authorizer.parser :as p]))

(defn clean-database [f]
      (db/clean-db)
      (f))

(use-fixtures :each clean-database)

(def stdin-mock (p/stdin-transactions "test/operations-mock"))

(deftest stdin-transactions-test
         (is (= 3 (count stdin-mock)))
         )


(def string-coll
     ["{\"account\": {\"active-card\": true, \"available-limit\": 100}}\n"])

(deftest read-transactions-test
         (let [transactions (p/read-transactions string-coll)]
              (is (= 1 (count transactions)))
              (is (= {:account {:active-card true, :available-limit 100}} (first transactions)))
              )
         )

(deftest read-transactions-stdin-test
         (let [transactions (p/read-transactions stdin-mock)]
              (is (= 3 (count transactions)))
              (is (= {:account {:active-card true, :available-limit 100}} (first transactions)))
              (is (= {:transaction {:merchant "Habbib's", :amount 90, :time "2019-02-13T11:00:00.000Z"}} (last transactions)))
              )
         )

(deftest execute-transactions-test
         (let [transactions (p/read-transactions stdin-mock)
               expected {:account {:active-card true, :available-limit 80}, :violations ["insufficient-limit"]}
               ]
              (is (= expected (i/execute-transactions! transactions)))
              )
         )