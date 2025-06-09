(ns payment-webhook-handler.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response bad-request]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [compojure.core :refer [defroutes POST]]
            [compojure.route :refer [not-found]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [cats.monad.either :as either]
            [cats.core :as m])
  (:gen-class))

(def db-spec {:dbtype "sqlite"
              :dbname "data/transactions.db"})

;; ------------------------
;; HTTP helpers
;; ------------------------

(defn post-json [url payload]
  (try
    (let [res (http/post url {:body (json/generate-string payload)
                              :headers {"Content-Type" "application/json"}
                              :throw-exceptions false})]
      (either/right res))
    (catch Exception e
      (either/left (.getMessage e)))))

(defn confirm-transaction! [tx-id]
  (post-json "http://host.docker.internal:5001/confirmar"
             {:transaction_id tx-id}))

(defn cancel-transaction! [tx-id]
  (post-json "http://host.docker.internal:5001/cancelar"
             {:transaction_id tx-id}))

;; ------------------------
;; Database helpers
;; ------------------------

(defn save-transaction! [tx-id]
  (try
    (jdbc/insert! db-spec :transactions {:transaction_id tx-id})
    (either/right :ok)
    (catch org.sqlite.SQLiteException _
      (either/left :duplicate))
    (catch Exception e
      (either/left (.getMessage e)))))

;; ------------------------
;; Validation
;; ------------------------

(defn missing-fields? [body]
  (some nil? (map body [:event :amount :currency :timestamp])))

(defn validate-request [req]
  (let [token (get-in req [:headers "x-webhook-token"])
        body  (:body req)
        tx-id (:transaction_id body)]
    (cond
      (not= token "meu-token-secreto")
      (either/left {:error "Invalid or missing token"})

      (nil? tx-id)
      (either/left {:error "Missing transaction_id"})

      (not= "49.90" (:amount body))
      (either/right {:action :cancel :tx-id tx-id :reason "Wrong amount"})

      (missing-fields? body)
      (either/right {:action :cancel :tx-id tx-id :reason "Incomplete payload"})

      :else
      (either/right {:action :confirm :tx-id tx-id}))))

;; ------------------------
;; Core logic
;; ------------------------

(defn process-event! [{:keys [action tx-id reason]}]
  (case action
    :cancel
    (m/>>= (cancel-transaction! tx-id)
           (fn [_] (either/right (bad-request reason))))

    :confirm
    (m/mlet [_ (save-transaction! tx-id)
             _ (confirm-transaction! tx-id)]
            (either/right (response "OK")))))

;; ------------------------
;; App + Routing
;; ------------------------

(defn handle-webhook [req]
  (either/branch
   (validate-request req)
   ;; Left handler
   (fn [{:keys [error]}] (bad-request error))
   ;; Right handler
   (fn [event]
     (either/branch
      (process-event! event)
      (fn [err] (bad-request (str err)))
      identity))))


(defroutes routes
  (POST "/webhook" req (handle-webhook req))
  (not-found "Route not found"))

(def app
  (-> routes
      (wrap-json-body {:keywords? true})
      (wrap-json-response)))

(defn -main [& _]
  (jdbc/execute! db-spec ["DROP TABLE IF EXISTS transactions"])
  (jdbc/execute! db-spec ["CREATE TABLE transactions (transaction_id TEXT PRIMARY KEY)"])

  (future
    (run-jetty app {:port 5000 :host "0.0.0.0" :join? false}))

  (run-jetty app
             {:ssl? true
              :ssl-port 5443
              :keystore "keystore.p12"
              :key-password "changeit"
              :join? true}))
