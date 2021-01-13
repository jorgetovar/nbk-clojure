(ns authorizer.core
    (:gen-class)
    (:require
      [authorizer.parser :as p]
      [authorizer.interpreter :as i]
      )
    )

(defn -main
      "I love simplicity"
      [& args]
      (let [operations args]
           (i/execute-transactions! (p/read-transactions operations))
           )
      )
