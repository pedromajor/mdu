(ns du.core-test
  (:require [clojure.test :refer :all]
            [du.core :refer :all]))

(deftest extract-num-test
  (testing "good with formatting"
    (is (= 123
           (extract-num "123K"))))
  (testing "good with pad spacing formatting"
    (is (= 123
           (extract-num "    123K   ")))))

(deftest si-encoded?-test
  (is (si-encoded? "123K"))
  (testing "with lower case"
    (is (si-encoded? "123k"))))

(deftest si->int-test
  (testing "good number formation"
    (is (= (* 123 1024)
           (si->int "123k"))))
  (testing "bad number formation"
    (is (thrown? NumberFormatException (si->int "1$23k")))
    (is (thrown? NumberFormatException (si->int "$123k")))
    (is (thrown? AssertionError (si->int "123") "show be si encoded, eg. 203M"))))

(deftest parse-df-output-test
  (let [df-out {:out (reduce str
                             (map #(str % "k" \tab "dirx" \newline) (range 1 5)))}
        res (parse-df-output df-out)]
    (is (= (* 1 1024) ((comp si->int first last) res)))))

(deftest formatizer-test
  (is (= "123  " (formatizer 123 :pad 5 :align :left)))
  (is (= "  123" (formatizer 123 :pad 5 :align :right))))

(deftest truncate-name-test
  (is (= "12345" (truncate-name "12345" 5)))
  (is (= "123.." (truncate-name "1234567" 5))))

(deftest split-lc-test
  (testing "small range"
    (is (= '((0 4  8 12 16)
             (1 5  9 13 17)
             (2 6 10 14 18)
             (3 7 11 15 19))
           (split-lc 5 :columns (range 0 20))))
    (is (= '((0 5 10 15)
             (1 6 11 16)
             (2 7 12 17)
             (3 8 13 18)
             (4 9 14 19))
           (split-lc 5 :lines (range 0 20)))))
  (testing "big range"
    (let [max 10]
      (is (= (map list (range 0 max))
             (split-lc 1 :columns (range 0 max)))))))
