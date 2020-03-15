(ns noaa-control.handler
  (:import [java.util UUID])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [java-time :refer [offset-date-time]]
            [noaa-control.db :as db]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response status]]))


(defn standard-meta
  "Minimum standard meta data to be included on every response, allowing for
   additions to be made on a response-by-response basis."
  ([] (standard-meta {}))
  ([meta] (merge {:generated_at (str (offset-date-time))} meta)))


(defn json-data-response
  "Assembles a stardard format JSON-API compliant payload."
  ([data] (json-data-response data {}))
  ([data meta]
   (response
    {:meta (standard-meta meta)
     :data data})))


(defroutes app-routes
  "Routing for noaa control.  There are two primary branches in the API:
   1.  /api/<v>/noaa/:id which allows a NOAA to be retrieved and marked for
       regeneration and or resending.
   2.  /api/<v>/noaa/find which exposes a variety of different query finders."
  (GET "/" [] "NOAA Control version 1")
  (context "/api" []
    (context "/v1" []
      (context "/noaa" []
        (context "/:noaa-id" [noaa-id]
          (GET "/" [] (json-data-response
                       (db/get-noaa (UUID/fromString noaa-id))))
          (POST "/regenerate" []
            (db/mark-noaa-for-regeneration (UUID/fromString noaa-id))
            (json-data-response nil
                                {:message "Marked for regeneration."}))
          (POST "/resend" []
            (db/mark-noaa-for-resend (UUID/fromString noaa-id))
            (json-data-response nil
                                {:message "Marked for resend."})))
        (context "/find" []
          (GET "/by-identified-date" [identified-on]
            (json-data-response
             (db/find-by-identified-date identified-on)))
          (GET "/by-sent-date" [sent-on]
            (json-data-response
             (db/find-by-sent-date sent-on)))
          (GET "/by-email" [email]
            (json-data-response
             (db/find-by-email email)))
          (POST "/multiple-noaas" {:keys [body]}
            (json-data-response (->> body
                                     :noaa_ids
                                     (map #(UUID/fromString %))
                                     (map db/get-noaa))))))))
  (route/not-found "Not Found"))


(defn wrap-exception [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :body {:meta (standard-meta)
                   :error (str e)}}))))


(def app
  (-> app-routes
      wrap-exception
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-defaults api-defaults)))
