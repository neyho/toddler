(ns toddler.hooks
  (:require
    [clojure.core.async :as async :refer-macros [go-loop]]
    [helix.core :refer-macros [defhook]]
    [helix.hooks :as hooks]
    ["react" :as react]
    [toddler.app :as app]
    [toddler.i18n :refer [translator get-in-calendar]]))


(defhook use-current-user
  []
  (let [[{:keys [user]}] (hooks/use-context app/*user*)]
    user))


(defhook use-current-locale
  []
  (let [{{:strs [locale]
          :or {locale "en"}} :settings} (use-current-user)]
    (keyword locale)))


(defhook use-translate
  []
  (let [locale (use-current-locale)
        translate (hooks/use-memo
                    [locale] 
                    (fn [key & args]
                      (when @translator
                        (apply @translator locale key args))))] 
    translate))


(defhook use-calendar
  [key]
  (let [locale (use-current-locale)]
    (hooks/use-memo
      [locale]
      (get-in-calendar locale key))))



(defn- bounding-client-rect [node]
  (let [rect (.getBoundingClientRect node)]
    {:height (.-height rect)
     :width (.-width rect)
     :top (.-top rect)
     :left (.-left rect)
     :right (.-right rect)
     :bottom (.-bottom rect)}))

(defhook use-dimensions 
  "Hook returns ref that should be attached to component and
  second result dimensions of bounding client rect"
  []
  (let [r (react/useRef nil)
        [dimensions set-dimensions!] (hooks/use-state nil)]
    ; (hooks/use-layout-effect
    (hooks/use-effect
      [(.-current ^js r)]
      (when (some? (.-current ^js r)) 
        (let [new-dimensions (bounding-client-rect (.-current ^js r))]
          (set-dimensions! new-dimensions)))
      (fn []))
    [r dimensions]))


(defn make-idle-service
  ([period f]
   (assert (and (number? period) (pos? period)) "Timeout period should be positive number.")
   (assert (fn? f) "Function not provided. No point if no action is taken on idle timeout.")
   (let [idle-channel (async/chan)]
     ;; When some change happend
     (async/go-loop [v (async/<! idle-channel)]
       (if (nil? v)
         :IDLED
         ;; If not nil new value received and now idle handling should begin
         (let [aggregated-values 
               (loop [[value _] 
                      (async/alts! 
                        [idle-channel (async/go (async/<! (async/timeout period)) 
                                                ::TIMEOUT)])
                      r [v]]
                 (if (or
                       (= ::TIMEOUT value)
                       (nil? value))
                   (conj r value)
                   (recur (async/alts! [idle-channel (async/go (async/<! (async/timeout period)) ::TIMEOUT)]) (conj r value))))]
           ;; Apply function and if needed recur
           (f aggregated-values)
           (if (nil? (last aggregated-values))
             nil
             (recur (async/<! idle-channel))))))
     idle-channel)))


(defhook use-idle 
  "Idle hook. Returns cached value and update fn. Input arguments
  are initial state, callback that should will be called on idle
  timeout."
  ([state callback] (use-idle state callback 500))
  ([state callback timeout]
   (assert (fn? callback) "Callback should be function")
   (let [[v u] (hooks/use-state state)
         call (hooks/use-ref callback)
         initialized? (hooks/use-ref false)
         idle-channel (hooks/use-ref nil)]
     ;; Create idle channel
     (hooks/use-effect
       :once
       (reset! 
         idle-channel
         (make-idle-service 
           timeout
           (fn [values] 
             (let [v' (last (butlast values))]
               (if @initialized?
                 (when (ifn? @call) (@call v'))
                 (reset! initialized? true))))))
       (fn [] 
         (when @idle-channel (async/close! @idle-channel))))
     ;; When callback is changed reference new callback
     (hooks/use-effect
       [callback]
       (reset! call callback))
     ;; When value has changed and there is idle channel
     ;; put new value to idle-channel
     (hooks/use-effect
       [v]
       (when @idle-channel 
         (async/put! @idle-channel (or v :NULL))))
     ;; Return local state and update fn
     [v u])))

(defhook use-delayed
  ([state] (use-delayed state 500))
  ([state timeout]
   (let [current-value (hooks/use-ref state)
         [v u] (hooks/use-state state)
         idle-channel (hooks/use-ref nil)]
     (hooks/use-effect
       :once
       (reset! 
         idle-channel
         (make-idle-service 
           timeout
           (fn [values]
             (let [v (last (butlast values))
                   v (if (= v ::NULL) nil v)]
               (when (not= @current-value v)
                 (reset! current-value v)
                 (u v))))))
       (fn [] 
         (when @idle-channel (async/close! @idle-channel))))
     (hooks/use-effect
       [state]
       (when @idle-channel 
         (async/put! @idle-channel (or state ::NULL))))
     v)))


(defhook use-publisher
  ([topic-fn] (use-publisher topic-fn 5000))
  ([topic-fn buffer-size]
   (let [[pc set-pc] (hooks/use-state nil) 
         [publisher set-publisher] (hooks/use-state nil)
         publish (hooks/use-memo
                   [publisher]
                   (fn [data] 
                     (when pc (async/put! pc data))))]
     (hooks/use-effect
       :once
       (let [pc' (async/chan buffer-size)
             p (async/pub pc' topic-fn)]
         (set-pc pc')
         (set-publisher p))
       #(do
          (when pc (async/close! pc))))
     [publisher publish])))


(defhook use-listener
  [publisher topic handler]
  (hooks/use-effect
    [publisher handler]
    (when publisher
      (let [c (async/chan 100)]
        (async/sub publisher topic c)
        (go-loop []
          (let [v (async/<! c)]
            (when v
              (handler v)
              (recur))))
        #(when publisher
           (async/unsub publisher topic c)
           (async/close! c))))))



(defhook use-toddler-listener
  [topic handler]
  (hooks/use-effect
    :once
    (let [c (async/chan 10)]
      (async/sub app/signal-publisher topic c)
      (async/go-loop []
        (let [v (async/<! c)]
          (when v
            (handler v)
            (recur))))
      (fn [] (async/close! c)))))


(defhook use-toddler-toddler-publisher []
  (let [publisher (hooks/use-memo
                    :once
                    (fn [data]
                      (async/put! app/signal-channel data)))]
    publisher))