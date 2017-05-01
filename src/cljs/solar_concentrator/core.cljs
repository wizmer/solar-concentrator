(ns solar-concentrator.core
  (:require [solar-concentrator.sliders :refer [populate]]
            [solar-concentrator.plotter]))

(defn my-main
  []
  (enable-console-print!)
  (println "Reloading...")
  (defonce coordinates (atom {:longitude -45
                              :date nil
                              :latitude -45
                              :utc-offset 0})) ;; The offset between UTC and timezone to use (eg. Paris = -1, New York = 5)

  (populate coordinates)

  (let [elevation-data (solar-concentrator.physics/get-data @coordinates)
        air-mass-data  (solar-concentrator.physics/get-air-mass-data)
        intensity-data (solar-concentrator.physics/get-solar-intensity-data)]
    (solar-concentrator.plotter/plot-air-mass-data air-mass-data)
    (solar-concentrator.plotter/plot-intensity-data intensity-data)
    (solar-concentrator.plotter/plot-elevation-data elevation-data)
    (solar-concentrator.sliders/recompute-and-refresh-plots! @coordinates)))
