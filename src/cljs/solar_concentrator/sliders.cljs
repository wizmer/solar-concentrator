(ns solar-concentrator.sliders
  (:require [solar-concentrator.plotter]
            [solar-concentrator.physics]
            [cljs-time.core :refer [now]]
            [cljs-time.format :refer [unparse formatters]]
            [goog.string :as gstring]
            [cljs-time.format :refer [parse formatters]]
            [domina.core :refer [by-id set-text! set-value!]]
            [domina.css :refer [sel]]
            [domina.events :refer [listen!]]
            ))

(enable-console-print!)

(defn new-coord!
  [coordinates key value]
  (println "New coord" @coordinates)
  (swap! coordinates assoc key value))

(defn refresh-coordinates!
  [coordinates]
  (set-text! (by-id "latitude-number") (gstring/format "%.1f" (coordinates :latitude)))
  (set-text! (by-id "longitude-number") (gstring/format "%.1f" (coordinates :longitude)))
  )

(defn on-slide! [coordinates coord-name evt value]
  "Callback called when sliders are moved"
  (new-coord! coordinates coord-name value)
  (refresh-coordinates! @coordinates)
  (solar-concentrator.plotter/update-plot (solar-concentrator.physics/get-data @coordinates)))

(defn on-date-changed! [coordinates]
  (let [date-str (domina.core/value (by-id "date"))
        date (parse (formatters :date) date-str)]
    (swap! coordinates #(assoc % :date date) )
    (solar-concentrator.plotter/update-plot (solar-concentrator.physics/get-data @coordinates))
    ))

(defn on-utc-offset-changed! [coordinates]
  (println "hello world")
  (let [utc-offset (domina.core/value (by-id "utc-offset-number"))]
    (println utc-offset)
    (swap! coordinates #(assoc % :utc-offset utc-offset) )
    (solar-concentrator.plotter/update-plot (solar-concentrator.physics/get-data @coordinates))
    ))

(defn populate
  "Populate the DOM with the d3.js sliders"
  [coordinates]
  (set-value! (by-id "date") (unparse (formatters :date) (now)))
  (swap! coordinates #(assoc % :date (now)) )

  (refresh-coordinates! @coordinates)
  (doseq [[coord-name range]{:latitude [-45 45]
                             :longitude [-90 90]}]
    (-> js/d3
        (.select (str "#slider_" (name coord-name)))
        (.call (-> js/d3
                   (.slider)
                   (.axis true)
                   (.min (range 0))
                   (.max (range 1))
                   (.step 1)
                   (.on "slide" (partial on-slide! coordinates coord-name))))))

  (-> (.-svg js/d3)
      (.axis)
      (.orient "top")
      (.ticks 8))

  (listen! (by-id "utc-offset-number") :input (fn []
                                                (on-utc-offset-changed! coordinates)))
  (listen! (by-id "date") :input (fn []
                                   (on-date-changed! coordinates)))
  )

