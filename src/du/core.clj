(ns du.core
  (:gen-class)
  (:require [clojure.java.shell :refer [sh]]
            [clojure.pprint :refer [cl-format]]
            [clojure.string :as str]
            [jansi-clj.core :refer :all]))

(declare formatizer)

(defonce si-units (map keyword '[B K M G]))
(defonce hfs-max-name 31)

(def si-map
  (zipmap
    si-units
    (map #(-> (Math/pow 1024 %1) int) (range 0 4))))

(defn extract-unit [s]
  (->> s str/trim last str str/upper-case keyword))

(defn extract-num [s]
  (let [num (read-string (->> s str/trim butlast (apply str)))]
    (if (number? num)
      num
      (throw (NumberFormatException.
               (format "don't know how to parse %s" s))))))

(defn truncate-name [s len]
  (if (> (count s) len)
    (str (subs s 0 (- len 2)) "..")
    s))

(defn si-encoded? [x]
  (some #(= (extract-unit x) %)
        si-units))

(defn si->int [x]
  {:pre [(string? x) (si-encoded? x)]}
  (* (extract-num x) (get si-map (extract-unit x))))

(defn df
  ([] (df "."))
  ([dir] (sh "du" "-d1" "-h" (or dir "."))))

(defn df-success? [df-out]
  (= 0 (:exit df-out)))

(defn colorizer [s]
  (cond
    (= (extract-unit s) :G) magenta
    (> (si->int s) (* 1024 1024 128)) cyan
    :else nil))

(defn inject-color [xs]
  (map #(if-let [color-fn (colorizer (first %))]
          (vector (-> % first color-fn) (-> % second color-fn))
          %)
       xs))

(defn parse-df-output [out]
  (->> out
       :out
       str/split-lines
       (map str/trim)
       (map #(str/split % #"\t" ))
       ((fn [x]
          (let [max-pad (min hfs-max-name (apply max (map (comp count second) x)))]
            (map #(vector (-> %1 first
                              (formatizer :pad 4 :align :left))
                          (-> %1 second (str/split #"/") last
                              (formatizer :pad max-pad :align :right)
                              (truncate-name max-pad)))
                 x))))
       (sort-by (comp si->int first) >)))

(defn formatizer [s & {:keys [pad align] :or {pad 0 align :right}}]
  (cl-format nil (str "~" pad (if (= :right align) "@") "a") s))

(defn split-lc
  "split col in n pseudo columns/lines

  (split-lc 5 (range 0 20) :by :lines)
  ((0 5 10 15)
   (1 6 11 16)
   (2 7 12 17)
   (3 8 13 18)
   (4 9 14 19))

  (split-lc 5 (range 0 20) :by :columns)
  ((0 4  8 12 16)
   (1 5  9 13 17)
   (2 6 10 14 18)
   (3 7 11 15 19))"
  [n by col]
  (let [by-fn (fn [i]
                (condp = by
                  :columns (-> (mod i (/ (count col) n)) Math/ceil int)
                  :lines   (mod i n)))]
    (loop [aux col
           res (sorted-map)
           i 0]
      (if (empty? aux)
        (map reverse (vals res))
        (recur (rest aux)
               (update-in res [(by-fn i)] conj (first aux))
               (inc i))))))

(defn not-zero? [x]
  (-> x first extract-num (= 0) not))

(defn print-the-fukr? [x]
  (->> x
       (map (comp
              println
              (partial reduce str)
              (partial map print-str)))))

(defn -main [& args]
  (doall (->> args
              first
              df
              parse-df-output
              (filter not-zero?)
              inject-color
              (split-lc 4 :columns)
              print-the-fukr?))
  (System/exit 0))
