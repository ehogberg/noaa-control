(ns noaa-control.handler
  (:import [java.util UUID])
  (:require [cheshire.core :refer [parse-string]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hikari-cp.core :refer :all]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]))


(def ds-options {:pool-name     "noaas-control-pool"
                 :adapter       "postgresql"
                 :server-name   "localhost"
                 :database-name "noaa_dev"
                 :username      "postgres"})

(defonce ds (delay (make-datasource ds-options)))


(defn noaa->json-safe [noaa]
  (-> noaa
      (update-in [:noaa_generation_data]
                 (fn [v] (-> v str (parse-string keyword))))))


(defn get-noaa [id]
  (-> (sql/get-by-id @ds :noaas id {:builder-fn rs/as-unqualified-lower-maps})
      noaa->json-safe))


(defn find-by-email [email]
  (->> (sql/query @ds
                 ["select * from noaas where noaa_destination_email = ?"
                  email]
                 {:builder-fn rs/as-unqualified-lower-maps})
       (map noaa->json-safe)))


(defroutes app-routes
  (GET "/" [] "NOAA Control version 1")
  (context "/noaa" {:keys [body] :as req}
    (context "/:noaa-id" [noaa-id]
      (GET "/" [] (response
                   (get-noaa (UUID/fromString noaa-id)))))
    (context "/find" []
      (GET "/multiple-noaas" []
        (response (->> body
                       :noaa-ids
                       (map #(UUID/fromString %))
                       (map get-noaa))))))
  (route/not-found "Not Found"))


(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-defaults api-defaults)))
