(ns payment-webhook-handler.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response bad-request]]
            [ring.middleware.json :refer [wrap-json-body]]
            [compojure.core :refer [defroutes POST]]
            [compojure.route :refer [not-found]])
  (:gen-class))

(defn webhook-handler
  [request]
  (let [token (get-in request [:headers "x-webhook-token"])
        expected-token "meu-token-secreto"]
    (if (= token expected-token)
      (do
        (println "Received webhook:" (:body request))
        (confirm-webhook)
        (response "Webhook received!")
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
  "Start the webhook server"
  [& args]
  (println "Starting payment webhook handler on port 5000...")
  (run-jetty app {:port 5000 :host "127.0.0.1" :join? false}))