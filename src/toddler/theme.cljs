(ns toddler.theme
  (:require
   clojure.string
   [toddler.head :as head]
   [helix.styled-components :as sc :refer [--themed]]
   [helix.placenta.util :refer [deep-merge]]))

(def text "#275f82")

(def green "#0dda87")

(defmethod sc/color [{} :positive] [] green)

(def dark-green "#02bf72")
(def pastel-green "#15B586")

(def teal "#80d6d6")
(def dark-teal "#598ea7")
(def deep-teal "#44666d")
(def saturated-teal "#00cccc")

(def blue "#85b7f1")
(def dark-blue "#144bab")

(def red "#fd6286")
(def dark-red "#ff0942")


(defmethod sc/color [{} :error] [] red)


(def label-gray "#5f5f5f")
(def gray "#616161")
(def dark-gray "#2c2c2c")
(def light-gray "#f2eff2")

(def purple "#8754d8")

(def asphalt "#9cb4d8")
(def dark-asphalt "#3562a2")
(def bleached-asphalt "#598ea71a")

(def orange "#ff872d")
(def dark-orange "#ff4007")

(def yellow "#ffec72")
(def dark-yellow "#FFDC00")
(def deep-yellow "#c7ac00")

(def dark-purple "#593198")

(def color "#275f82")
(def link "rgba(44,29,191,0.78)")
(def drawer-background-color "#edffff")

(def disabled "#bbbbbb")

(def transparent-white "#ffffffee")

(def level
  ["#FFFFFF"
   "#E8FAFA"
   "#D1F6F6"
   "#B9F1F1"
   "#A2ECEC"
   "#8BE8E8"
   "#74E3E3"
   "#5DDFDF"
   "#46DADA"
   "#2ED5D5"
   "#17D1D1"
   "#00CCCC"
   "#05C7C2"
   "#0AC2B8"
   "#0FBDAD"
   "#14B8A3"
   "#1AB399"
   "#1FAD8F"
   "#24A885"
   "#29A37A"
   "#2E9E70"
   "#339966"
   "#308F60"
   "#2D8659"
   "#297C53"
   "#26734C"
   "#236946"
   "#205F40"
   "#1D5639"
   "#194C33"
   "#16432C"
   "#133926"])


;;; toddler.elements
(defmethod --themed [{} 'toddler.elements/simplebar]
  [{:keys [$shadow-top $shadow-bottom $hidden]}]
  (let [box-shadow (cond-> []
                     $shadow-top (conj "inset 0px 11px 8px -10px #CCC")
                     $shadow-bottom (conj "inset 0px -11px 8px -10px #CCC"))]
    (cond-> nil
      (not-empty box-shadow)
      (assoc :box-shadow (clojure.string/join ", " box-shadow))
      $hidden
      (assoc ".simplebar-track" {:display "none"}))))


(def $element-block
  {".label"
   {:margin "2px 0 4px 4px"
    :padding-bottom 2
    :text-transform "uppercase"
    :font-size "14"
    :color gray
    :border-bottom (str "1px solid " gray)}
   :padding 3})

(defmethod --themed [{} 'toddler.elements/column]
  [_]
  $element-block)

(defmethod --themed [{} 'toddler.elements/row]
  [_]
  $element-block)

(letfn [(themed-action
          [{:keys [theme context]
            _disabled :disabled}]
          (let [context (if _disabled :disabled context)
                color "#7c7c7c"
                hover "#595959"]
            (case (:name theme)
              {:color (if _disabled disabled color)
               ":hover" {:color (case context
                                  :positive green
                                  :negative red
                                  :fun saturated-teal
                                  :fresh yellow
                                  :stale light-gray
                                  :disabled _disabled
                                  hover)}
               :cursor (if _disabled "default" "pointer")
               :pointer-events (if _disabled "none" "inherit")})))]
  (defmethod --themed [{} 'toddler.elements/action] [props] (themed-action props))
  (defmethod --themed [{} 'toddler.elements/named-action] [props] (themed-action props)))


(defn button-colors
  [{:keys [context disabled]}]
  (let [context (if disabled :disabled context)]
    (case context
      ;;
      :positive
      {:color "white"
       :background-color dark-green
       :hover {:color "white"
               :background-color green}}
      ;;
      :negative
      {:color red
       :background-color light-gray
       :hover {:color "white"
               :background-color red}}
      ;;
      :fun
      {:color "white"
       :background-color teal
       :hover {:background-color saturated-teal}}
      ;;
      :fresh
      {:color gray
       :background-color light-gray
       :hover {:color "black"
               :background-color yellow}}
      ;;
      :stale
      {:color gray
       :background-color light-gray
       ::hover {:color "white"
                :background-color dark-gray}}
      ;;
      :disabled
      {:color "white"
       :background-color "#bbbbbb"
       :cursor "initial"}
      ;;
      {:color "white"
       :background-color "#5e82b8"
       :hover {:background-color dark-asphalt}})))


(defn interactions-drop-on-active
  [_]
  {":active" {:transform "translate(0px,2px)" :box-shadow "none"}})

(defn interactions-button
  [{:keys [disabled] :as props}]
  (let [{:keys [background-color color hover]}
        (button-colors props)]
    (cond->
     {:color color
      :background-color background-color
      ":hover" (assoc hover :box-shadow "0px 2px 4px 0px #aeaeae")}
      disabled (assoc :pointer-events "none"))))

(defmethod --themed [{} 'toddler.elements/button] [props]
  (merge (interactions-button props) (interactions-drop-on-active props)))


(defmethod --themed [{} 'toddler.elements/checkbox-button]
  [{:keys [theme $active disabled]}]
  (let [[c bc] (case (:name theme)
                 ["white" (case $active
                            true green
                            false disabled
                            light-gray)])]
    (cond->
     {:color c
      :background-color bc}
      disabled (assoc :pointer-events "none"))))


(defmethod --themed [{} 'toddler.elements/checkbox-field]
  [_]
  {:color gray})


(defmethod --themed [{} 'toddler.elements/slider]
  [{:keys [width height orient]
    {twidth :width theight :height
     :or {twidth 12 theight 12}} :thumb
    :or {width "100%"
         height 5}}]
  (cond->
   {:width width :height height
    :background-color light-gray
    "&::-webkit-slider-runnable-track"
    {:border-radius 4}
    "&::-moz-range-track"
    {:border-radius 4}
    "&::-webkit-slider-thumb"
    {:-webkit-appearance "none"
     :appearance "none"
     :width twidth :height theight
     :background gray
     :border-radius "50%"
     :cursor "pointer"}
    "&::-moz-range-thumb"
    {:width twidth :height theight
     :background gray
     :cursor "pointer"}
    ":hover::-webkit-slider-thumb"
    {:background dark-gray}}
    ;;
    (= orient "vertical")
    (as-> css
          (let [hw (/ width 2)]
            (assoc css
                   :transform (str "translate(" (* -1 hw) "px,0) rotate(270deg)")
                   :margin-top hw)))
    #_(assoc
       :writing-mode "bt-lr"
       :-webkit-appearance "slider-vertical")))


(defmethod --themed [{} 'toddler.elements/dropdown-option]
  [{:keys [theme selected]}]
  (let [[c h b bh]
        (case theme
          (let [c "#00b99a"
                h "#d7f3f3"]
            [(if selected c gray) gray "white" h]))]
    {:color c
     :cursor "pointer"
     :background-color b
     :transition "color .2s ease-in,background-color .2s ease-in"
     :padding "4px 6px 4px 4px"
     ; :border-radius 3
     ; :font-weight "500" 
     " :hover" {:color h
                :background-color bh}
     ":last-child" {:border-bottom "none"}}))

(def $dropdown-shadow
  {:background-color "white"
   :box-shadow "0px 3px 10px -3px black"
   " .simplebar-scrollbar:before"
   {:background gray
    :pointer-events "none"}})



(def $with-delete-marker
  {".tbody .trow"
   {":hover"
    {" .delete-marker"
     {:opacity "1 !important"
      :transition "opacity .6s ease-in-out"}}
    " .delete-marker:not(.selected)" {:opacity "0"}}})

(def $zebra-table
  {".tbody .trow"
   {:min-height 30
    "&.odd"
    {:background-color "#f8f8ff"}
    "&.even"
    {:background-color "#ececff"}
    "&:first-child" {:padding-top 6}
    "&:last-child" {:padding-bottom 6}
    "&:first-child:last-child" {:padding "6px 0"}}})

(def $rounded-table
  {".thead"
   {:box-sizing "border-box"
    ".trow" {:padding-bottom 8}}
   ".tbody"
   {:box-sizing "border-box"
    :border-radius 13}
   ; ".tbody:not(.empty)" {:box-shadow "1px 3px 7px 2px #00000033"}
   ".tbody:not(.empty)" {:box-shadow "1px 4px 11px 1px #0000001f"}
   ".tbody .simplebar-content-wrapper"
   {:border-radius 13}
   ".tbody .trow"
   {:box-sizing "border-box"
    "&:first-child" {:border-radius "13px 13px 0px 0px"}
    "&:last-child" {:border-radius "0px 0px 13px 13px"}
    "&:first-child:last-child" {:border-radius 4}}})


(def $dropdown
  {:max-height 400})

(defmethod --themed [{} 'toddler.elements/dropdown-popup]
  [_]
  (deep-merge
   $dropdown-shadow
   $dropdown))


(defmethod --themed [{} 'toddler.elements/dropdown-element-decorator]
  [_]
  {"&.opened" {:color "transparent"}
   :cursor "default"})

(defmethod --themed [{} 'toddler.elements/dropdown-field-discard]
  [_]
  {":hover" {:color (str red "!important")}})

(defmethod --themed [{} 'toddler.elements/calendar-day]
  [_]
  {".day"
   {:color gray
    :font-size 12
    ;;
    "&.disabled, &:hover.disabled"
    {:background-color "white"
     :color disabled
     :border-color "transparent"
     :cursor "default"}
    ;;
    ":hover:not(.empty),&.selected"
    {:color "white"
     :background-color saturated-teal
     :border-collapse "collapse"
     :border (str "1px solid " dark-teal)
     :border-radius 2
     :font-weight 500}
    "&.today"
    {:border "1px solid teal"}
    "&.weekend"
    {:color red}}})


(defmethod --themed [{} 'toddler.elements/calendar-month-header]
  [_]
  {".day-wrapper .day"
   {:color gray
    :font-size 12
    "&.weekend"
    {:color red}}})

(defmethod --themed [{} 'toddler.elements/calendar-month]
  [_]
  {".week-days-header .day-wrapper .day"
   {:color gray}
   ".week-row .week-days .day-wrapper .day"
   {:color gray
    ;;
    "&.disabled, &:hover.disabled"
    {:background-color "white"
     :color disabled
     :border-color "transparent"
     :cursor "default"}
    ;;
    ":hover:not(.empty),&.selected"
    {:color "white"
     :background-color saturated-teal
     :border-collapse "collapse"
     :border (str "1px solid " dark-teal)
     :border-radius 2
     :font-weight 500}}})

(defmethod --themed [{} 'toddler.elements/default-field]
  [{_disabled :disabled}]
  {".field-name"
   {:color (cond
             _disabled disabled
             :else gray)
    :user-select "none"
    :transition "all .3s ease-in-out"
    :font-size 12
    :font-weight "600"
    :text-transform "uppercase"}})

(defmethod --themed [{} 'toddler.elements/field-wrapper]
  [{:keys [context]}]
  (case context
    {:border-color "#b3b3b3 !important"
     :background "#e5e5e5"
     :transition "all .3s ease-in-out"
     ":focus-within" {:border-color (str teal "!important")
                      :box-shadow (str "0 0 3px " teal)
                      :background-color "transparent"}
     "input,textarea"
     {:color gray}}))


(defmethod --themed [{} 'toddler.elements/search]
  [_]
  {:border-color "#b3b3b3 !important"
   :background "#eaeaea"
   :transition "all .3s ease-in-out"
   ".icon" {:transition "all .3s ease-in-out"
            :color "#7c7c7c"}
   ":focus-within" {:border-color (str teal "!important")
                    :box-shadow (str "0 0 3px " teal)
                    :background-color "transparent"
                    ".icon" {:color teal}}
   "input"
   {:color gray}})

(defmethod --themed [{} 'toddler.elements/dropdown-wrapper]
  [{:keys [disabled]}]
  (when disabled
    {:pointer-events "none"}))
(defmethod --themed [{} 'toddler.elements/dropdown-field-wrapper] [_] {:color "#adacac"})

(defmethod --themed [{} 'toddler.elements/multiselect-wrapper]
  [{:keys [opened]}]
  {:cursor (if opened "default" "pointer")})


(defmethod --themed [{} 'toddler.elements/timestamp-clear] [_]
  {:background-color light-gray
   :color "white"
   :transition "background .3s ease-in-out"
   :border-radius 20
   :cursor "pointer"
   ":hover" {:background-color red}})


(defmethod --themed [{} 'toddler.elements/time-period-popup] [_]
  $dropdown-shadow)


(defmethod --themed [{} 'toddler.elements/user]
  [{:keys [disabled read-only]}]
  (let [cursor (if (or disabled read-only)
                 "default"
                 "pointer")]
    (cond->
     {:color gray
      :cursor cursor
      :input {:cursor cursor}}
      (or disabled read-only) (assoc :pointer-events "none"))))



(defmethod --themed [{} 'toddler.elements/group]
  [{:keys [disabled read-only]}]
  (let [cursor (if (or disabled read-only)
                 "default"
                 "pointer")]
    (cond->
     {:color gray
      :cursor cursor
      :input {:cursor cursor}}
      (or disabled read-only) (assoc :pointer-events "none"))))


(defmethod --themed [{} 'toddler.elements/user-multiselect]
  [{:keys [disabled read-only]}]
  (let [cursor (if (or disabled read-only)
                 "default"
                 "pointer")]
    (cond->
     {:color gray
      :cursor cursor
      :input {:cursor cursor}}
      (or disabled read-only) (assoc :pointer-events "none"))))


(defmethod --themed [{} 'toddler.elements/group-multiselect]
  [{:keys [disabled read-only]}]
  (let [cursor (if (or disabled read-only)
                 "default"
                 "pointer")]
    (cond->
     {:color gray
      :cursor cursor
      :input {:cursor cursor}}
      (or disabled read-only) (assoc :pointer-events "none"))))


(defmethod --themed [{} 'toddler.elements/user-cell]
  [_]
  {:input {:color color}})



(defmethod --themed [{} 'toddler.elements/table-header]
  [_]
  {:border-color dark-teal})


(defn interactive-cell? [{:keys [cell/disabled cell/read-only]}]
  (every? not [disabled read-only]))

(defmethod --themed [{} 'toddler.elements/timestamp-cell]
  [{:keys [opened] :as props}]
  (let [interactive? (interactive-cell? props)]
    {".decorator" {:color (if (or (not interactive?) opened) "transparent" "#adacac")
                   :user-events "none"}
     ".time" {:input {:color dark-gray}}
     :cursor (if interactive? "pointer" "default")
     :input {:cursor (if interactive? "pointer" "default")}}))



(defmethod --themed [{} 'toddler.elements/table-row] [_]
  {":hover" {:border-color dark-teal}})

(defmethod --themed [{} 'toddler.elements/table-header-cell] [_]
  {:color gray})


;; toddler.elements.light
(defmethod --themed [{} 'toddler.elements.light/field-input]
  [{:keys [readOnly]}]
  (cond->
   {:color text
    :transition "border-color 0.2s ease-in"
    :border-bottom (str "1px solid " bleached-asphalt)
    "&::placeholder" {:color asphalt}
    "&:focus,&:hover"
    {:border-color text}}
    readOnly (assoc :cursor "pointer")))


(defmethod --themed [{} 'toddler.elements.light/search]
  [{:keys [position]
    :or {position :right}}]
  (let [[c bc pc] [color dark-asphalt asphalt]]
    {:color c
     :input
     {:color c
      "&::placeholder" {:color pc}
      "&:focus::placeholder" {:color c}
      :border-bottom (str "1px solid " bc)}
     :justify-content (case position
                        :left "flex-start"
                        :center "center"
                        "flex-end")}))


(defmethod --themed [{} 'toddler.elements.light/dropdown-option]
  [{:keys [selected]}]
  {:color (if selected
            saturated-teal
            "#dbdbdb")
   :cursor "pointer"
   :transition "color .2s ease-in,background-color .2s ease-in"
   :padding "4px 6px 4px 4px"
   " :hover" {:color "white"
              :text-shadow "0 0 8px #ffffff99, 0 0 8px #ffffff99"}
   ":last-child" {:border-bottom "none"}})


(letfn [(--themed-action
          [{:keys [context disabled]}]
          (let [context (if disabled :disabled context)]
            (cond->
             {:color color
              ":hover" {:color (case context
                                 :positive pastel-green
                                 :negative red
                                 :fun saturated-teal
                                 :fresh yellow
                                 :stale light-gray
                                 :disabled disabled
                                 color)}
              :cursor (if disabled "default" "pointer")}
              disabled (assoc
                        :visibility "hidden"
                        :cursor "default"
                        :pointer-events "none"))))]
  (defmethod --themed [{} 'toddler.elements.light/action] [props] (--themed-action props))
  (defmethod --themed [{} 'toddler.elements.light/named-action] [props] (--themed-action props)))


(defmethod --themed [{} 'toddler.elements.light/tabs]
  [_]
  (let [[c ic bc bcl]
        [color asphalt color asphalt]]
    {:box-shadow "0px 2px 4px -4px black"
     :border-bottom (str "1px solid " bc)
     " .tab"
     {:color ic
      "&:not(:first-child)"
      {:border-left (str "1px solid " bcl)}
      "&.selected, &:hover"
      {:color c
       :transition "color .3s ease-in"
       :cursor "default"}}}))


(defmethod --themed [{} 'toddler.elements/textarea-field]
  [_]
  {:textarea {:font-family "Roboto"}})

(defmethod --themed [{} 'toddler.elements.light/user-dropdown-input]
  [_]
  {"input::placeholder" {:color "#8fa7c4"}
   :input {:color color}})


(letfn [(--editable-tag [{:keys [editable?]}]
          (when-not editable?
            {:user-select "none"}))

        (--colored-tag
          [{:keys [theme context]}]
          (let [[c b rc rch]
                (case theme
                  (case context
                    :positive ["white" green dark-green "black"]
                    :negative ["white" red dark-red "black"]
                    :fun ["white" saturated-teal dark-teal "black"]
                    :fresh ["black" yellow dark-yellow "black"]
                    :stale ["white" gray dark-gray "black"]
                    :exception ["white" "black" gray red]
                    ["white" teal dark-teal red]))]
            {:color c
             :background-color b
             " .remove" {:color rc
                         :cursor "pointer"
                         :transition "color .2s ease-in"
                         :path {:cursor "pointer"}}
             " .remove:hover" {:color rch}}))]
  (defmethod --themed [{} 'toddler.elements.light/user-tag]
    [props]
    (deep-merge
     {:img {:margin-right 4}}
     (--editable-tag props)
     (--colored-tag props)))

  (defmethod --themed [{} 'toddler.elements.light/tag]
    [props]
    (deep-merge
     (--editable-tag props)
     (--colored-tag props)))

  (defmethod --themed [{} 'toddler.elements/user-tag]
    [props]
    (deep-merge
     {:img {:margin-right 4}}
     (--editable-tag props)
     (--colored-tag props)))

  (defmethod --themed [{} 'toddler.elements/tag]
    [props]
    (deep-merge
     (--editable-tag props)
     (--colored-tag props))))



(defmethod --themed [{} 'toddler.elements.modal/close-button] [_]
  {:color "#bfbfbf"
   "&:hover" {:color red}})


(defmethod --themed [{} 'toddler.elements.modal/feedback-footer] [_]
  {".warning" {:color orange}
   ".error" {:color red
             :svg {:margin-right 6}}})

(defmethod --themed [{} 'toddler.elements.tabs/tab-container] [_]
  (let [[c ic bc bcl]
        [dark-gray "#b4b4b4" dark-gray "#b4b4b4"]]
    {:border-bottom (str "1px solid " bc)
     " .tab"
     {:color ic
      "&:not(:first-child)"
      {:border-left (str "1px solid " bcl)}
      "&.selected, &:hover"
      {:color c
       :transition "color .3s ease-in"
       :cursor "default"}}}))


(defmethod --themed [{} 'toddler.elements.tabs/tab-container]
  [{:keys [selected]}]
  {:color "white"
   :background dark-asphalt
   :opacity (if selected "1" "0.2")
   :border "2px solid #d2dfec"
   ":hover"
   {:opacity "1"}
   ".negative"
   {:border "2px solid #ffe0e7"
    :background-color red
    :opacity "0.2"
    ":hover"
    {:opacity "1"}}
   ".positive"
   {:border "2px solid #d9f7e7"
    :opacity "0.2"
    ":hover"
    {:opacity "1"}}
   ".disabled"
   {:cursor "default"
    :opacity "0.2 !important"}})


(defmethod --themed [{} 'toddler.elements.tabs/tab-container]
  [_]
  $dropdown-shadow)


(defmethod --themed [{} 'toddler.elements.avatar/avatar-dropzone]
  []
  {:color gray
   :label {":hover" {:color dark-gray}}})


(defmethod --themed [{} 'toddler.elements.popup/element]
  []
  $dropdown-shadow)


(defmethod --themed [{} 'toddler.elements.drawers/drawer-action]
  [{:keys [selected]}]
  {:color "white"
   :background dark-asphalt
   :opacity (if selected "1" "0.2")
   :border "2px solid #d2dfec"
   ":hover"
   {:opacity "1"}
   ".negative"
   {:border "2px solid #ffe0e7"
    :background-color red
    :opacity "0.2"
    ":hover"
    {:opacity "1"}}
   ".positive"
   {:border "2px solid #d9f7e7"
    :opacity "0.2"
    ":hover"
    {:opacity "1"}}
   ".disabled"
   {:cursor "default"
    :opacity "0.2 !important"}})


(defmethod --themed [{} 'toddler.elements/card-action]
  [{:keys [context]}]
  {:background-color "white"
   ".action:hover"
   (case context
     :negative
     {:background-color red
      :color "#fff8f3"}
     {:background-color teal
      :color "#fff8f3"})
   ".action"
   {:background-color "#929292"
    :color light-gray}})


(def global (sc/import-resource "css/toddler.css"))
(def simplebar (sc/import-resource "css/simplebar.css"))

(head/add
 :link
 {:href "https://fonts.googleapis.com/css2?family=Roboto&display=swap"
  :rel "stylesheet"})

(comment
  (head/remove
   :link
   {:href "https://fonts.googleapis.com/css2?family=Roboto&display=swap"
    :rel "stylesheet"}))
