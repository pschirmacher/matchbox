(ns matchbox.core)

(defmulti match? (fn [pattern testee] [(type pattern) (type testee)]))

(defmethod match? :default
  [pattern testee]
  (= pattern testee))

(defn _
  [testee]
  true)

(def _* :dummy)

(defmethod match? [java.util.List java.util.List]
  [pattern testee]
  (cond
    (= _* (first pattern))
    true
    (and (empty? pattern) (empty? testee))
    true
    (or (empty? pattern) (empty? testee))
    false
    :else
    (and
      (match? (first pattern) (first testee))
      (match? (rest pattern) (rest testee)))))

(defmethod match? [java.lang.Class java.lang.Object]
  [pattern testee]
  (instance? pattern testee))

(defmethod match? [java.util.List java.lang.Number]
  [[lower upper :as pattern] testee]
  (if (match? [java.lang.Number java.lang.Number] pattern)
    (<= lower testee upper)
    false))

(defmethod match? [clojure.lang.Keyword java.lang.Object]
  [pattern testee]
  (= pattern testee))

(defmethod match? [clojure.lang.IFn java.lang.Object]
  [pattern testee]
  (pattern testee))

(defmethod match? [java.util.List nil]
  [pattern testee]
  (empty? pattern))

(defmethod match? [clojure.lang.IFn nil]
  [pattern testee]
  (pattern testee))

(defmethod match? [java.util.regex.Pattern String]
  [pattern testee]
  (re-find pattern testee))

(defmethod match? [java.util.Map java.util.Map]
  [pattern testee]
  (every?
    true?
    (for [k (keys pattern)]
      (match? (get pattern k) (get testee k)))))

(prefer-method match? [java.util.List java.util.List] [clojure.lang.IFn java.lang.Object])
(prefer-method match? [java.util.List java.lang.Number] [clojure.lang.IFn java.lang.Object])
(prefer-method match? [java.util.Map java.util.Map] [clojure.lang.IFn java.lang.Object])
(prefer-method match? [java.util.List java.util.List] [clojure.lang.PersistentVector java.lang.Object])
(prefer-method match? [java.util.List java.lang.Number] [clojure.lang.PersistentVector java.lang.Object])
(prefer-method match? [java.util.List nil] [clojure.lang.IFn nil])

(defn flatten-once
  [coll]
  (reduce
    (fn [acc i]
      (if (coll? i)
        (concat acc i)
        (concat acc (list i))))
    '()
    coll))

(defmacro pattern-match
  [exp & cases]
  `(condp match? ~exp
     ~@(flatten-once 
         (for [c cases]
           (cond
             (match? [_ :> _ :> _ _*] c) 
             (let [[pattern _ destruct _ & body] c] (list pattern `(let [~destruct ~exp] (do ~@body))))
             (match? [_ :> _ _*] c) 
             (let [[pattern _ & body] c] (list pattern `(do ~@body)))
             :else
             (throw (Exception. (str c " does neither match [_ :> _ :> _ _*] nor [_ :> _ _*]"))))))))