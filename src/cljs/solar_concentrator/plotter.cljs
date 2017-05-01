(ns solar-concentrator.plotter)

(def margin {:top 20 :right 15 :bottom 20 :left 100})
(def width (- 360 (margin :left) (margin :right)))
(def height (- 300 (margin :top) (margin :bottom)))
(def plot-range {:x #js [0 width] :y #js [height 0]})
(def plot-color "blue")

(defn add-axis-labels!
  [element label-x label-y]
  "Add labels to the axes of the plot"
  (-> (.append element "text")
      (.attr "text-anchor" "middle")
      (.attr "transform" (str "translate(" (/ width 2) "," (+ 10
                                                              height
                                                              (:bottom margin)
                                                              (:top margin))  ")"))
      (.attr "class" "axis-label")
      (.text label-x))

  (-> (.append element "text")
      (.attr "text-anchor" "middle")
      (.attr "transform" (str "translate(" (/ (- (:left margin)) 2) "," (/ height 2) ")rotate(-90)"))
      (.attr "class" "axis-label")
      (.text label-y)))


(defn plot
  [container data domain range label]
  (let* [x (-> js/d3
               (.-scale)
               (.linear)
               (.domain (:x domain))
               (.range  (:x range)))

         y (-> (.-scale js/d3)
               (.linear)
               (.domain (:y domain))
               (.range (:y range)))

         chart (-> js/d3
                   (.select container)
                   (.append "svg:svg")
                   (.attr "width" (+ width (margin :right) (margin :left)))
                   (.attr "height" (+ width (margin :top) (margin :bottom)))
                   (.attr "class" "chart"))

         main (-> (.append chart "g")
                  (.attr "transform"
                         (str "translate(" (margin :left) "," (margin :top ) ")"))
                  (.attr "width" width)
                  (.attr "height" height)
                  (.attr "class" "main"))

         x-axis (-> (.-svg js/d3)
                    (.axis)
                    (.scale x)
                    (.orient "bottom"))

         y-axis (-> (.-svg js/d3)
                    (.axis)
                    (.scale y)
                    (.orient "left"))
         ]

    (-> main
        (.append "g")
        (.attr "transform"
               (str "translate(" 0 "," height ")"))
        (.attr "class" "main axis date")
        (.call x-axis))


    (-> (.append main "g")
        (.attr "transform" "translate(0,0)")
        (.attr "class" "main axis date")
        (.call y-axis))

    (-> (.append main "svg:g")
        (.selectAll "scatter-dots")
        (.data (apply array data))
        (.enter)
        (.append "svg:circle")
        (.attr "cx" (fn [d i] (x (d 0))))
        (.attr "cy" (fn [d i] (y (max 0 (d 1)))))
        (.attr "fill" plot-color)
        (.attr "r" 2))
    (add-axis-labels! main (:x label) (:y label))))


(defn plot-elevation-data
  [data]
  (plot "#container-elevation-plot"
        data
        {:x #js [0 24] :y #js [0 90]} ;; domain
        plot-range
        {:x "Time (h)" :y "Sun elevation (degrees)"}) ;; labels
)

(defn plot-air-mass-data
  [data]
  (plot "#container-air-mass-plot"
        data
        {:x #js [0 90] :y #js [0 40]}  ;; domain
        plot-range
        {:x "Sun elevation (degrees)" :y "Air mass"}) ;; labels
  )

(defn plot-intensity-data
  [data]
  (plot "#container-intensity-plot"
        data
        {:x #js [0 90] :y #js [0 1000]}  ;; domain
        plot-range
        {:x "Sun elevation (degrees)" :y "Sun power (Watt)"}) ;; labels
  )

(defn update-plot
  [data]
  (let [svg (.select js/d3 "#container-elevation-plot")
        x (-> (.-scale js/d3)
              (.linear)
              (.domain #js [0 24])
              (.range #js [0 width]))

        y (-> (.-scale js/d3)
              (.linear)
              (.domain #js [0 90])
              (.range #js [height 0]))

        chart (-> (.selectAll svg "circle")
                  (.data (apply array data))
                  (.transition)
                  (.duration 1000)

          (.each "start" (fn [] (this-as this
                                          (-> (.select js/d3 this)
                                              (.attr "cx" #(x (%1 0)))
                                              (.attr "cy" #(y (max 0 (%1 1))))
                                              (.attr "r" 2))
                                          ))))
        ]))

