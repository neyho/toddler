(ns toddler.showcase.calendar
  (:require
   [helix.core :refer [$ defnc]]
   [helix.hooks :as hooks]
   [vura.core :as vura]
   ; [helix.dom :as d]
   [toddler.ui :as ui]
   [toddler.ui.components :as default]
   [toddler.ui.elements :as e]
   [toddler.layout :as layout]
   [toddler.dev :as dev]))


(defnc Calendar
   []
   (let [today (vura/date)
         value (vura/date->value today)
         context (vura/day-time-context value) 
         [{:keys [week]} :as week-context] (vura/calendar-frame value :week)
         month-context (vura/calendar-frame value :month)
         [{:keys [month year]} set-dropdown!] (hooks/use-state nil)
         [timestamp set-timestamp!] (hooks/use-state (vura/date 2023 6 1))
         [period set-period!] (hooks/use-state [(vura/date 2022 1 1) (vura/date 2022 7 9)])
         {:keys [height width]} (layout/use-container-dimensions)]
      ($ default/Provider
         ($ ui/simplebar
            {:style {:height height
                     :width width
                     :boxSizing "border-box"}}
            ; ($ ui/row
            ;    {:label "Day"}
            ;    ($ ui/calendar-day {& context}))
            ($ ui/row
               {:label "Week"}
               ($ ui/calendar-week {:week week :days week-context}))
            ($ ui/row
               {:label "Month"}
               ($ ui/calendar-month {:days month-context}))
            ($ ui/row
               {:label "Month dropdown"}
               ($ ui/calendar-month-dropdown
                  {:value month
                   :onChange #(set-dropdown! assoc :month %)}))
            ($ ui/row
               {:label "Year dropdown"}
               ($ ui/calendar-year-dropdown
                  {:value year
                   :onChange #(set-dropdown! assoc :year %)}))
            ($ ui/row
               {:label "Timestamp calendar"}
               ($ e/timestamp-calendar
                  {:value timestamp
                   :onChange set-timestamp!}))
            ($ ui/row
               {:label "Period calendar"}
               ($ e/period-calendar
                  {:value period
                   :onChange set-period!}))))))



(dev/add-component
   {:key ::calendar
    :name "Calendar"
    :render Calendar})
