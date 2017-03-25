(ns solar-concentrator.core
  (:require [solar-concentrator.sliders :refer [populate]]))
             ;; [solar-concentrator.physics]

(enable-console-print!)

(println "This textes is printed from src/solar-concentrator/core.cljs. Go ahead and edit it and see reloading in action.")
(defonce coordinates (atom {:longitude -45 :latitude -45}))



(populate coordinates)
(solar-concentrator.plotter/plot (solar-concentrator.physics/get-data @coordinates))

