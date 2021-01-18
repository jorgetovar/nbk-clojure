(ns authorizer.db)

(def account-db (atom
                  {:account {:active-card false :available-limit 0}
                   :violations []
                   :active? false
                   :transactions []
                   }))

(defn query [] @account-db)

(defn create-account [account]
      (reset! account-db (merge (query) account {:active? true}))
      )

(defn clean-db []
      (reset! account-db {:account {:active-card false :available-limit 0}
                          :violations []
                          :active? false
                          :transactions []
                          }))

; validations
(defn add-violation [violation]
      (swap! account-db update-in [:violations] conj violation))

;transactions

(defn- tx-amount [transaction] (get-in transaction [:transaction :amount]))

(defn withdrawals [transaction]
      (swap! account-db update-in [:account :available-limit] - (tx-amount transaction))
      (swap! account-db update-in [:transactions] conj transaction)
      )
