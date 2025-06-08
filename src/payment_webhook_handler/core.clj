(ns payment-webhook-handler.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response bad-request]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [compojure.core :refer [defroutes POST]]
            [compojure.route :refer [not-found]]
            [clj-http.client :refer [post]]
            [cheshire.core :refer [generate-string]]
            [clojure.java.jdbc :as jdbc])
  (:gen-class))

(def db {:dbtype "sqlite"
         :dbname "data/transactions.db"})

(def expected-token "meu-token-secreto")

(defn cancel-transaction!
  [transaction-id]
  (try
    (let [response (post "http://host.docker.internal:5001/cancelar"
                         {:body (generate-string {:transaction_id transaction-id})
                          :headers {"Content-Type" "application/json"}
                          :throw-exceptions false})]
      (println "Cancellation response status:" (:status response)))
    (catch Exception e
      (println "Exception occurred while canceling:" (.getMessage e)))))

(defn confirm-transaction!
  [transaction-id]
  (try
    (let [response (post "http://host.docker.internal:5001/confirmar"
                         {:body (generate-string {:transaction_id transaction-id})
                          :headers {"Content-Type" "application/json"}
                          :throw-exceptions false})]
      (println "Confirmation response status:" (:status response)))
    (catch Exception e
      (println "Exception occurred while confirming:" (.getMessage e)))))

(defn insert-transaction! [transaction-id]
  (try
    (jdbc/insert! db :transactions {:transaction_id transaction-id})
    true
    (catch org.sqlite.SQLiteException _
      false)))

(defn payload-incomplete?
  [body]
  (let [{:keys [event amount currency timestamp]} body]
    (some nil? [event amount currency timestamp])))

(defn webhook-handler
  [request]
  (let [token (get-in request [:headers "x-webhook-token"])
        body (:body request)
        transaction-id (:transaction_id body)]
    (println "Received webhook:" body)
    (cond
      (not= token expected-token) (bad-request "Invalid or missing token")
      (nil? transaction-id) (bad-request "Invalid body request")
      (not (insert-transaction! transaction-id)) (bad-request "Duplicate transaction")
      (payload-incomplete? body) (do
                                   (cancel-transaction! transaction-id)
                                   (bad-request "Payload is incomplete"))
      (not= "49.90" (:amount body)) (do
                                      (cancel-transaction! transaction-id)
                                      (bad-request "Wrong amount"))
      :else (do
              (confirm-transaction! transaction-id)
              (response "OK")))))

(defroutes app-routes
  (POST "/webhook" request (webhook-handler request))
  (not-found "Route not found"))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      (wrap-json-response)))

(defn -main [& _]
  (jdbc/execute! db ["DROP TABLE IF EXISTS transactions"])
  (jdbc/execute! db ["CREATE TABLE transactions (transaction_id TEXT PRIMARY KEY)"])

  (future
    (run-jetty app
               {:port 5000
                :host "0.0.0.0"
                :join? false}))

  (run-jetty app
             {:ssl? true
              :ssl-port 5443
              :keystore "keystore.p12"
              :key-password "changeit"
              :join? true}))

