(defproject noaa-control "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.758"]
                 [cheshire "5.10.0"]
                 [cider/piggieback "0.4.2"]
                 [cljs-http "0.1.46"]
                 [clojure.java-time "0.3.2"]
                 [com.bhauman/figwheel-main "0.2.4"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                 [com.taoensso/timbre "4.10.0"]
                 [compojure "1.6.1"]
                 [hikari-cp "2.10.0"]
                 [org.postgresql/postgresql "42.2.11"]
                 [reagent "1.0.0-alpha1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [seancorfield/next.jdbc "1.0.395"]]
  :paths ["src" "target" "resources"]
  :plugins [[lein-ring "0.12.5"]]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}}
  :ring {:handler noaa-control.handler/app}
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :resource-paths ["target" "resources"]
  :source-paths ["src/clj" "src/cljs"]
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]}
  )
