(ns authorizer.account
    (:require
      [authorizer.db :as db])
    (:import (java.time LocalDateTime)))

; account
(defn- active-account? [account-db] (:active? account-db))

(defn account-query [] (db/query))

(defn create-account! [account]
      (if (active-account? (account-query))
          (db/add-violation "account-already-initialized")
          (db/create-account account)
          )
      (account-query)
      )

(defn available-limit [account-db] (get-in account-db [:account :available-limit]))

(defn active-card? [account-db] (get-in account-db [:account :active-card]))

; transactions
(defn- tx-amount [transaction] (get-in transaction [:transaction :amount]))

(defn- invalid-limit? [amount account-db] (neg? (- (available-limit account-db) amount)))

(defn create-tx
      ([merchant amount]
       (create-tx merchant amount (LocalDateTime/now)))

      ([merchant amount time]
       {:transaction {:merchant merchant
                      :amount amount
                      :time time}})
      )


(defn- filter-by-time [current-transaction transaction time-interval]
       (.isAfter (:time (:transaction current-transaction))
                 (.minusMinutes (:time (:transaction transaction)) time-interval))
       )

(defn- count-txs [coll] (inc (count coll)))

(defn count-interval-tx-time [transactions transaction time-interval]
      (count-txs (filter #(filter-by-time % transaction time-interval) transactions)))

(defn- same-tx [transaction1 transaction2]
       (let [tx1 (:transaction transaction1) tx2 (:transaction transaction2)]
            (and (= (:merchant tx1) (:merchant tx2))
                 (= (:amount tx1) (:amount tx2))
                 )
            ))

(defn- duplicated-merchants [transaction transactions]
       (filter #(same-tx transaction %) transactions))

(defn count-interval-tx-merchant [transactions transaction time-interval]
      (count-txs (filter #(filter-by-time % transaction time-interval)
                         (duplicated-merchants transaction transactions)))
      )

(def hf-time-interval 2)
(def hf-allowed-tx 2)

(defn high-frequency-interval? [transaction account-db]
      (let [txs (:transactions account-db)]
           (> (count-interval-tx-time txs transaction hf-time-interval)
              hf-allowed-tx))
      )


(defn double-transaction? [transaction account-db]
      (let [txs (:transactions account-db)]
           (> (count-interval-tx-merchant txs transaction hf-time-interval)
              1))
      )

(defn execute-tx! [transaction]
      (let [account-db (account-query)]
           (cond
             (not (active-account? account-db)) (db/add-violation "account-not-initialized")
             (not (active-card? account-db)) (db/add-violation
                                               "card-not-active")
             (invalid-limit? (tx-amount transaction) account-db) (db/add-violation
                                                                       "insufficient-limit")
             (high-frequency-interval? transaction account-db) (db/add-violation
                                                                 "high-frequency-small-interval")
             (double-transaction? transaction account-db) (db/add-violation
                                                            "doubled-transaction")
             :else (db/withdrawals transaction)
             )
           )
      (account-query)
      )
