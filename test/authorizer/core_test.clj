(ns authorizer.core-test
    (:require [clojure.test :refer :all]
              [authorizer.core]
              [authorizer.account :as act]
              ))


(def account-with-110 {:account
                       {:active-card true
                        :available-limit 110}})

(def transaction-20
     {:transaction {:merchant "Burger King"
                    :amount 20
                    :time "2019-02-13T10:00:00.000Z"}}
     )

(deftest account-creation-test
         (let [my-account (act/create account-with-110)]
              (is (= [] (:violations my-account)))
              (is (= 110 (act/get-amount my-account)))
              )
         )

(deftest account-creation-violation-test
         (let [my-account (act/create account-with-110)]
              (is (= [] (:violations my-account)))
              (is (= ["account-already-initialized"] (:violations (act/create my-account))))
              )
         )

(deftest authorize-transaction-test
         (let [my-account (act/create account-with-110)]
              (is (= 90 (act/get-amount
                          (act/exec-tx transaction-20))))
              (is (= 70 (act/get-amount
                          (act/exec-tx transaction-20))))
              )
         )


