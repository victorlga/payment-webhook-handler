(defproject payment-webhook-handler "0.1.0-SNAPSHOT"
  :description "A Clojure webhook handler for processing payment events"
  :url "https://github.com/victorlga/payment-webhook-handler"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.xerial/sqlite-jdbc "3.45.1.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [ring/ring-json "0.5.1"]
                 [clj-http "3.13.0"]
                 [cheshire "5.12.0"]
                 [compojure "1.7.1"]]
  :main ^:skip-aot payment-webhook-handler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})