(ns authorizer.account)

(def account-db (atom
                  {:account
                   {:active-card false :available-limit 0}
                   :violations []
                   }
                  ))

(defn active-account? []
      (get-in @account-db [:account :active-card]))



(defn exec-tx [transaction]
      (swap! account-db update-in
             [:account :available-limit]
             - (get-in transaction [:transaction :amount]))
      @account-db
      )

(defn get-amount [account]
      (get-in @account-db [:account :available-limit])
      )

(defn create [account]
      (if (active-account?)
          (swap! account-db update-in [:violations] conj "account-already-initialized")
          (reset! account-db (merge @account-db account))
          )
      @account-db
      )

