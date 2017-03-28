(ns solar-concentrator.physics
  (:require [cljs-time.core :refer [year month day]])
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

(defn to-radians
  [angle]
  (* angle (/ (.-PI js/Math) 180)))

(defn get-data
  [coordinates]

  (let* [offset (* 60 (coordinates :utc-offset))
         minutes (.range js/d3 0 (* 24 60) 15)
         date (coordinates :date)
         times (map (fn [minute] (js/Date.
                                  (year date)
                                  (month date)
                                  (day date)
                                  (quot (+ minute offset) 60)
                                  (rem (+ minute offset) 60)
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


(defn cos [angle] (.cos js/Math angle))
(defn air-mass
  [elevation-angle]
  ;; # According to : https://en.wikipedia.org/wiki/Air_mass_(solar_energy)
  (let [incidence-angle (to-radians (- 90 elevation-angle))
        radius_earth_km 6371
        atm_thickness_km 9
        ratio (/ radius_earth_km atm_thickness_km)]

    (- (.sqrt js/Math (+ (.pow js/Math (* ratio (cos incidence-angle)) 2)
                         (+ (* 2 ratio) 1)))
       (* ratio (cos incidence-angle)))))

(defn get-air-mass-data
  []
  (doall (map #(vector % (air-mass %)) (range 1 90 0.2)))
  )

(defn intensity
  [air-mass]
  (let [i-max 1353]
    (* i-max (.pow js/Math 0.7 (.pow js/Math air-mass 0.678)))))

(defn get-solar-intensity-data
  []
  (doall (map #(vector % (intensity (air-mass %))) (range 1 90 0.2))))

(defn get-daily-power
  "Compute the solar energy integrated on one day"
  [data]
  (let [time-steps (keys data)
        step (- (second time-steps) (first time-steps))]
    (* step
       (apply + (map #(intensity (air-mass %)) (vals data))))) ;; Integral of intensity on the entire day
  )

