(ns solar-concentrator.plotter)

(def margin {:top 20 :right 15 :bottom 60 :left 60})
(def width (- 960 (margin :left) (margin :right)))
(def height (- 500 (margin :top) (margin :bottom)))


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
                   (.select "body")
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
    )
  )

(defn update-plot
  [data]
  (let [svg (.select js/d3 "body")
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
