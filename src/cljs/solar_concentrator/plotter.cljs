(ns solar-concentrator.plotter)

(def margin {:top 20 :right 15 :bottom 20 :left 100})
(def width (- 460 (margin :left) (margin :right)))
(def height (- 300 (margin :top) (margin :bottom)))

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
  [data]
  (let* [x (-> js/d3
               (.-scale)
               (.linear)
               (.domain #js [0 24])
               (.range #js [0 width]))
         y (-> (.-scale js/d3)
               (.linear)
               (.domain #js [0 90])
               (.range #js [height 0]))

         chart (-> js/d3
                   (.select "#container-elevation-plot")
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
        (.attr "r" 2))
    (add-axis-labels! main "Time (h)" "Sun elevation (degrees)")
))



(defn plot-air-mass-data
  [data]
  (let* [x (-> js/d3
               (.-scale)
               (.linear)
               (.domain #js [0 90])
               (.range #js [0 width]))

         y (-> (.-scale js/d3)
               (.linear)
               (.domain #js [0 40])
               (.range #js [height 0]))

         chart (-> js/d3
                   (.select "#container-air-mass-plot")
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
        (.attr "r" 2))
  (add-axis-labels! main "Sun elevation (degrees)" "Air mass")))


(defn plot-intensity-data
  [data]
  (let* [x (-> js/d3
               (.-scale)
               (.linear)
               (.domain #js [0 90])
               (.range #js [0 width]))

         y (-> (.-scale js/d3)
               (.linear)
               (.domain #js [0 1000])
               (.range #js [height 0]))

         chart (-> js/d3
                   (.select "#container-intensity-plot")
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
        (.attr "r" 2))
    (add-axis-labels! main "Sun elevation (degrees)" "Sun power (Watt)")))

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
