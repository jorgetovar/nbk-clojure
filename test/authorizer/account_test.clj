(ns authorizer.account-test
    (:require [clojure.test :refer :all]
              [authorizer.account :as a]
              [authorizer.db :as db]
              )
    (:import (java.time LocalDateTime)))


(defn clean-database [f]
      (db/clean-db)
      (f))

(use-fixtures :each clean-database)

(def account-with-110 {:account
                       {:active-card true
                        :available-limit 110}})

(deftest account-creation-test
         (let [my-account (a/create-account! account-with-110)]
              (is (= [] (:violations my-account)))
              (is (= 110 (a/available-limit (db/query))))
              (is (true? (:active? my-account)))
              )
         )

(deftest account-already-initialized-test
         (let [my-account (a/create-account! account-with-110)]
              (is (= [] (:violations my-account)))
              (is (= ["account-already-initialized"] (:violations (a/create-account! my-account))))
              )
         )

(def corral-20 (a/create-tx "Corral" 20))

(deftest authorize-transaction-test
         (a/create-account! account-with-110)
         (a/execute-tx! corral-20)
         (is (= 90 (a/available-limit (db/query))))
         (a/execute-tx! (a/create-tx "Burger" 20))
         (is (= 70 (a/available-limit (db/query))))
         )


(deftest account-not-initialized-test
         (is (= ["account-not-initialized"] (:violations (a/execute-tx! corral-20))))
         )

(deftest active-card-test
         (a/create-account! account-with-110)
         (is (true? (a/active-card? (db/query))))
         )


(deftest card-not-active-test
         (a/create-account! {:account
                             {:active-card false
                              :available-limit 110}})
         (is (false? (a/active-card? (db/query))))
         (is (= ["card-not-active"] (:violations (a/execute-tx! corral-20))))
         )

(deftest insufficient-limit-test
         (a/create-account! account-with-110)
         (a/execute-tx! (a/create-tx "Corral" 70))
         (a/execute-tx! (a/create-tx "Burger" 70))
         (is (= 40 (a/available-limit (db/query))))
         (is (= ["insufficient-limit"] (:violations (a/account-query))))
         (is (= 1 (count (:transactions (a/account-query)))))
         )

(def ld-1012 (LocalDateTime/of 2020 01 20 10 12))
(def ld-1013 (LocalDateTime/of 2020 01 20 10 13))
(def ld-1021 (LocalDateTime/of 2020 01 20 10 21))

(def pizza-txs [(a/create-tx "Pizza1" 10 ld-1012)
                (a/create-tx "Pizza2" 10 ld-1012)])

(def test-time-interval 2)

(deftest count-interval-tx-time-2min-test
         (is (= 3 (a/count-interval-tx-time pizza-txs
                                            (a/create-tx "Pizza3" 10 ld-1013)
                                            test-time-interval)))
         )

(deftest count-interval-tx-time-10min-test
         (is (= 1 (a/count-interval-tx-time pizza-txs
                                            (a/create-tx "Pizza3" 10 ld-1021)
                                            test-time-interval)))
         )


(def merchant-txs [(a/create-tx "Corral" 10 ld-1012)
                   (a/create-tx "Corral" 11 ld-1012)])



(deftest count-interval-tx-merchant-2min-test
         (is (= 2 (a/count-interval-tx-merchant
                    merchant-txs (a/create-tx "Corral" 10 ld-1013)
                    test-time-interval
                    )))
         )

(deftest count-interval-tx-merchant-10min-test
         (is (= 1 (a/count-interval-tx-merchant
                    merchant-txs (a/create-tx "Corral" 10 ld-1021)
                    test-time-interval
                    )))
         )

(deftest count-interval-tx-merchant-10interval-test
         (is (= 2 (a/count-interval-tx-merchant
                    merchant-txs (a/create-tx "Corral" 10 ld-1021)
                    10
                    )))
         )

(def corral-10 (a/create-tx "Corral" 10))

(deftest double-transaction-test
         (let [my-account (a/create-account! account-with-110)]
              (is (= [] (:transactions my-account)))
              (a/execute-tx! corral-10)
              (is (true? (a/double-transaction? corral-10 (db/query))))
              )
         )

(deftest double-transaction-tx-test
         (a/create-account! account-with-110)
         (a/execute-tx! corral-10)
         (a/execute-tx! corral-10)
         (is (= 100 (a/available-limit (db/query))))
         (is (= ["doubled-transaction"] (:violations (a/account-query))))
         (is (= 1 (count (:transactions (a/account-query)))))
         )

(deftest high-frequency-interval-test
         (let [my-account (a/create-account! account-with-110)]
              (is (= [] (:transactions my-account)))
              (a/execute-tx! corral-10)
              (a/execute-tx! (a/create-tx "Burger" 10))
              (is (true? (a/high-frequency-interval? corral-10 (db/query))))
              )
         )

(deftest high-frequency-small-interval-test
         (a/create-account! account-with-110)
         (a/execute-tx! corral-10)
         (a/execute-tx! (a/create-tx "Burger" 10))
         (a/execute-tx! (a/create-tx "King" 10))
         (is (= 90 (a/available-limit (db/query))))
         (is (= ["high-frequency-small-interval"] (:violations (a/account-query))))
         (is (= 2 (count (:transactions (a/account-query)))))
         )