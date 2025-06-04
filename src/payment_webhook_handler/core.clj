(ns payment-webhook-handler.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response]])
  (:gen-class))

(defn handler [request]
  (println "Received webhook:" (:body request))
  (response "Webhook received!"))

(defn -main
  "Start the webhook server"
  [& args]
  (println "Starting payment webhook handler on port 3000...")
  (run-jetty handler {:port 3000 :join? false}))