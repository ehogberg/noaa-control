(ns noaa-control.db
  (:require [cheshire.core :refer [parse-string]]
            [hikari-cp.core :refer :all]
            [java-time :refer [offset-date-time]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))


(def ds-options {:pool-name     "noaas-control-pool"
                 :adapter       "postgresql"
                 :server-name   "localhost"
                 :database-name "noaa_dev"
                 :username      "postgres"})

(defonce ds (delay (make-datasource ds-options)))


(defn standard-builder
  ([] (standard-builder {}))
  ([opts] (merge {:builder-fn rs/as-unqualified-lower-maps} opts)))


(defn noaa->json-safe
  "NOAA generation data comes back stored as a PGObject which gives
   Ring HTTP serialization fits.  Coerce this field to a string then
   parse into a map so that things serialize sanely."
  [noaa]
  (update-in noaa [:noaa_generation_data]
             (fn [v] (-> v str (parse-string keyword)))))


(defn get-noaa [id]
  (-> (sql/get-by-id @ds :noaas id (standard-builder))
      noaa->json-safe))


(defn find-by-email [email]
  (->> (sql/query @ds
                 ["select * from noaas where noaa_destination_email = ?"
                  email]
                 (standard-builder))
       (map noaa->json-safe)))


(defn find-by-identified-date [on]
  (->> (sql/query
        @ds
        ["select * from noaas
          where date(noaa_identified_at) = ?::date"
         on ]
        (standard-builder))
       (map noaa->json-safe)))


(defn find-by-sent-date [sent-on]
  (->> (sql/query
        @ds
        ["select * from noaas
          where date(noaa_transmitted_at) = ?::date"
         sent-on ]
        (standard-builder))
       (map noaa->json-safe)))


(defn mark-noaa-for-resend [id]
  (sql/update! @ds
               :noaas
               {:noaa_transmitted_at nil
                :updated_at (offset-date-time)}
               {:id id}
               (standard-builder)))


(defn mark-noaa-for-regeneration
  "Clear out generation and transmission fields in the NOAA.
   By doing so, the NOAA text will be re-generated the next
   time the generate-noaa process is run."
  [id]
  (sql/update! @ds
               :noaas
               {:noaa_transmitted_at nil
                :noaa_generated_at nil
                :noaa_generation_data nil
                :noaa_text nil
                :noaa_template_type nil
                :updated_at (offset-date-time)}
               {:id id}
               (standard-builder)))
