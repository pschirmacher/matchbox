(ns matchbox.core-test
  (:use
    [matchbox.core]
    [clojure.test]))

(deftest list-matching
  (is (= true (match? [] [])))
  (is (= true (match? [1] [1])))
  (is (= false (match? [1] [1 2])))
  (is (= false (match? [1 2] [1])))
  (is (= true (match? [1 2 _] [1 2 3])))
  (is (= false (match? [1 2 _] [1 2 3 4])))
  (is (= true (match? [1 2 _ 4] [1 2 3 4])))
  (is (= true (match? [1 2 _*] [1 2 3 4])))
  (is (= true (match? [1 2 _*] [1 2 3])))
  (is (= true (match? [1 2 _*] [1 2])))
  (is (= false (match? [1 2 _*] [1])))
  (is (= true (match? [1 2 _* 4 5 6] [1 2 3])))
  (is (= true (match? [_*] [1 2 3])))
  (is (= true (match? [_*] [])))
  (is (= true (match? [{:a 1}] [{:a 1}]))))

(deftest type-matching
  (is (= true (match? java.util.List '(1 2 3))))
  (is (= false (match? java.util.List 1))))

(deftest interval-matching
  (is (= true (match? [1 10] 5)))
  (is (= false (match? [1 10] 15)))
  (is (= true (match? [-0.5 10] 0.2)))
  (is (= false (match? [-3/4 -1/2] -1))))
  
(deftest list-matching
  (is (= true (match?
                [1 [3 _]]
                [1 [3 5]])))
  (is (= true (match?
                [1 [3 _ [_]] _ 5 _*]
                [1 [3 5 [7]] 4 5])))
  (is (match? [[1 2 3] _ [] _*] [[1 2 3] 4 [] 6 7 8 9]))
  (is (not (match? [[1 2 3] _ [] _*] [[1 2 3] 4 5 6 7 8 9]))))

(deftest map-matching
  (is (= true (match? {:a even? :b true :c [1 2 _]} {:a -4 :b true :c [1 2 5]})))
  (is (= false (match? {:a even? :b false :c [1 2 _]} {:a -4 :b true :c [1 2 5]})))
  (is (= true (match?
                {:in-stock #(> % 100)
                 :price [100 500]}
                {:in-stock 200
                 :price 120
                 :name "whatever"
                 :id 4711})))
  (is (= false (match?
                {:in-stock #(> % 100)
                 :price [100 500]}
                {:in-stock 200
                 :price 99})))
  (is (= true (match? {:a _} {:a 5})))
  (is (= true (match? {:a _} {:b 5})))
  (is (= true (match? [_ :>] [[1 2 4] :>])))
  (is (match? #{1 2 3} 1))
  (is (not (match? #{1 2 3} 4))))

(run-tests)