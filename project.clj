(defproject payment-webhook-handler "0.1.0-SNAPSHOT"
  :description "A Clojure webhook handler for processing payment events"
  :url "https://github.com/your-username/payment-webhook-handler"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]]
  :main ^:skip-aot payment-webhook-handler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})