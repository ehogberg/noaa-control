(ns noaa-control.core
  (:require [noaa-control.state :as state]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn noaa-detail-ui [noaa children]
  [:div
   [:h3 "NOAA Detail"]])

(defn noaa-line-item-ui [{:keys [key name]}]
  ^{:key key}
  [:tr
   [:td.col-md-10 name]
   [:td.col-md-2 "Controls"]])

(defn update-input [evt val]
  (let [new-val (-> evt
                    .-target
                    .-value)]
    (reset! val new-val)))

(defn text-input [value]
  [:input.col-md-11 {:type "text"
                     :value @value
                     :on-change #(update-input % value)}])

(defn reload-noaas []
  [:input.col-md-1 {:type "button" :value "Reload"
           :on-click #(state/test-fetch)}])

(defn noaa-table-ui [query-results children]
  (let [query-str (r/atom "")]
    [:div
     [:div.row
      [text-input query-str]
      [reload-noaas]]
     [:div.row
      [:table.table.table-hover
       [:tbody
        (for [r query-results]
          (noaa-line-item-ui r))]]]]))

(defn subordinate-grouping-ui [{:keys [query-results
                                       active-noaa]} children]
  (if active-noaa
    [noaa-detail-ui active-noaa nil]
    [noaa-table-ui query-results nil]))


(defn noaa-control-ui [{:keys [title]}]
  [:div.container
   [:div.row
    [:div.col
     [:h2 "Header"]]]
   [:div.row
    [:div.col
     [:h2 title]
     [subordinate-grouping-ui {:query-results (state/query-results)
                               :active-noaa   (state/active-noaa)} nil]]]
   [:div.row
    [:div.col
     [:h2 "Footer"]]]])


(rdom/render [noaa-control-ui (state/site-meta)]
             (.getElementById js/document "app"))


(comment
  (r/rswap! app-state assoc :title "NOAA Control")
  @app-state
  )
