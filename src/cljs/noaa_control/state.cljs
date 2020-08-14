(ns noaa-control.state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [reagent.core :as r]))

(def app-state (r/atom {:meta
                        {:title "NOAA Control Website"}
                        :active-noaa nil
                        :query-results [{:name "First component" :key 1}
                                        {:name "Second component" :key 2}]}))

(defn site-meta [] (:meta @app-state))

(defn active-noaa [] (:active-noaa @app-state))

(defn query-results [] (:query-results @app-state))

(defn test-fetch []
  (go
    (let [response (<! (http/get "/api/v1/noaa/dev/noaas"))
          converted (-> response
                        :body
                        (js->clj)
                        :data)]
      (prn converted)
      (swap! app-state assoc :query-results (:noaas converted)))))
