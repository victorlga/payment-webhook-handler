(ns payment-webhook-handler.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response bad-request]]
            [ring.middleware.json :refer [wrap-json-body]]
            [compojure.core :refer [defroutes POST]]
            [compojure.route :refer [not-found]]
            [clj-http.client :refer [post]]
            [cheshire.core :refer [generate-string]])
  (:gen-class))


(defn cancel-transaction
  [transaction-id]
  (try
    (let [response (post "http://127.0.0.1:5001/cancelar"
                         {:body (generate-string {:transaction-id transaction-id})
                          :headers {"Content-Type" "application/json"}
                          :throw-exceptions false})]
      (println "Cancelation response status:" (:status response)))
    (catch Exception e
      (println "Exception occurred while canceling:" (.getMessage e)))))



;; Teste


(defn confirm-transaction
  [transaction-id]
  (try
    (let [response (post "http://127.0.0.1:5001/confirmar"
                         {:body (generate-string {:transaction_id transaction-id})
                          :headers {"Content-Type" "application/json"}
                          :throw-exceptions false})]
      (println "Confirmation response status:" (:status response)))
    (catch Exception e
      (println "Exception occurred while confirming:" (.getMessage e)))))

(defn webhook-handler
  [request]
  (let [token (get-in request [:headers "x-webhook-token"])
        expected-token "meu-token-secreto"]
    (if (= token expected-token)
      (do
        (println "Received webhook:" (:body request))
        ;; Se ID duplicado, bad request. Se não, response sucess and confirmation
        
        ;;
        (confirm-transaction (get-in request [:body :transaction_id]))
        (response "OK")
        )
      (do
        (println "Invalid token:" token)
        (bad-request "Invalid or missing token")))))





(defroutes app-routes
  (POST "/webhook" request (webhook-handler request))
  (not-found "Route not found"))

(def app
  (wrap-json-body app-routes {:keywords? true}))

(defn -main
  [& args]
  (println "\n\nStarting payment webhook handler on port 5000...\n\n")
  (run-jetty app {:port 5000 :host "127.0.0.1" :join? false}))