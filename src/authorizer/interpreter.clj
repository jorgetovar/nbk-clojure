(ns authorizer.interpreter
    (:require [authorizer.account :as a]))

(defn- account-movement? [line] (not (nil? (:transaction line))))

(defn- user-fmt [account] {:account (:account account)
                           :violations (:violations account)})

(defn execute-transactions! [transactions-coll]
      (doseq [tx transactions-coll]
             (if (account-movement? tx)
                 (do (println "Transaction->" tx)
                     (a/execute-tx! tx)
                     (println (user-fmt (a/account-query)))
                     )
                 (do (println "Create account->" tx)
                     (a/create-account! tx)
                     (println (user-fmt (a/account-query)))
                     )
                 )
             )
      (user-fmt (a/account-query)))
