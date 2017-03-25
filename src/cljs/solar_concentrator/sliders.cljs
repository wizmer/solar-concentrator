(ns solar-concentrator.sliders
  (:require [solar-concentrator.plotter]
            [solar-concentrator.physics]))

(defn new-coord!
  [coordinates key value]
  (println "New coord" @coordinates)
  (swap! coordinates assoc key value))

(defn populate
  "Populate the DOM with the d3.js sliders"
  [coordinates]
  (println "populate")

  (doseq [[coord_name range]{:latitude [-45 45]
                             :longitude [-90 90]}]
    (-> js/d3
        (.select (str "#slider_" (name coord_name)))
        (.call (-> js/d3
                   (.slider)
                   (.axis true)
                   (.min (range 0))
                   (.max (range 1))
                   (.step 1)
                   (.on "slide" (fn [evt value]
                          (new-coord! coordinates coord_name value)
                          (solar-concentrator.plotter/update-plot (solar-concentrator.physics/get-data @coordinates))))))))

  (-> (.-svg js/d3)
      (.axis)
      (.orient "top")
      (.ticks 8))
  )


