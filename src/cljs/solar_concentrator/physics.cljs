(ns solar-concentrator.physics
  ;; (:require [clojure.math.numeric-tower :refer [sqrt sin cos expt]])
  )


;; (defn air-mass
;;   "According to : https://en.wikipedia.org/wiki/Air_mass_(solar_energy)"
;;   [angle]
;;   (let [radius_earth_km 6371
;;         atm_thickness_km 9
;;         ratio (/ radius_earth_km atm_thickness_km)]
;;     (- (sqrt (+ (expt (* ratio (cos angle)) 2) (* 2 ratio) 1)) (* ratio (cos angle)))))

(defn to-degrees
  [angle]
  (* angle (/ 180 (.-PI js/Math))))

(defn get-data
  [coordinates]
  (let* [year 2017
         month 4
         day 20
         minutes (.range js/d3 0 (* 24 60) 15)
         rad (.-PI js/Math)
         times (map (fn [minute] (js/Date.
                                  year
                                  month
                                  day
                                  (quot minute 60)
                                  (rem minute 60)
                                  0 0))
                    minutes)

         compute-position (fn [time]
                            (-> js/window
                                (.-SunCalc)
                                (.getPosition time
                                              (get coordinates :latitude)
                                              (get coordinates :longitude))))

         compute-altitude-in-degrees #(to-degrees
                                       (get (js->clj (compute-position %1)) "altitude"))

         x (vec (map #(+ (quot %1 60) (/ (rem %1 60) 60.)) minutes))
         y (vec (map compute-altitude-in-degrees times))]

    (doall (map vector x y))))
