(ns simple-avro.core-tests
  (:use (simple-avro schema core)
        (clojure test)))

(deftest test-prim-types
  (is (= (pack avro-null    nil)          nil))
  (is (= (pack avro-null    5)            nil))
  (is (= (pack avro-boolean true)         true))
  (is (= (pack avro-boolean nil)          false))
  (is (= (pack avro-int     5)            5))
  (is (= (pack avro-long    10)           (long 10)))
  (is (= (pack avro-long    (long 10))    (long 10)))
  (is (= (pack avro-float   2.5)          (float 2.5)))
  (is (= (pack avro-float   (float 2.5))  (float 2.5)))
  (is (= (pack avro-double  2.5)          (double 2.5)))
  (is (= (pack avro-double  (double 2.5)) (double 2.5)))
  (is (= (str (pack avro-string  "test")) "test")))

; Some types
(def bool-array (avro-array avro-boolean))
(def int-map    (avro-map avro-int))
(def a-union    (avro-union avro-string avro-int avro-null))

(defavro-fixed MyFixed 2)

(defavro-enum MyEnum "A" "B" "C")

(defavro-record MyRecord
  "f1" avro-int
  "f2" avro-string)

(defavro-record MyNestedRecord
  "f1" avro-int
  "f2" MyRecord
  "f3" MyRecord)

(defavro-record List
  "value" avro-int 
  "next"  (avro-union "List" avro-null))

(def recursive 
  {"value" 1 
   "next"  {"value" 2
            "next"  {"value" 3
                     "next"  nil}}})

(def nested-record {"f1" 10
                    "f2" {"f1" 20
                          "f2" "f2-f2"}
                    "f3" {"f1" 20
                          "f2" "f3-f2"}})

(defmacro test-pack-unpack
  [name encoder decoder]
  `(deftest ~name
    (is (= (unpack avro-null    (pack avro-null    nil  ~encoder) :decoder ~decoder)         nil))
    (is (= (unpack avro-null    (pack avro-null    5    ~encoder) :decoder ~decoder)         nil))
    (is (= (unpack avro-boolean (pack avro-boolean true ~encoder) :decoder ~decoder)         true))
    (is (= (unpack avro-int     (pack avro-int     5    ~encoder) :decoder ~decoder)         5))
    (is (= (unpack avro-long    (pack avro-long    10   ~encoder) :decoder ~decoder)         (long 10)))
    (is (= (unpack avro-float   (pack avro-float   2.5  ~encoder) :decoder ~decoder)         (float 2.5)))
    (is (= (unpack avro-double  (pack avro-double  2.5  ~encoder) :decoder ~decoder)         (double 2.5)))
    (is (= (str (unpack avro-string (pack avro-string  "test" ~encoder) :decoder ~decoder))  "test"))

    (is (= (unpack bool-array (pack bool-array [true false false] ~encoder) :decoder ~decoder) [true false false]))
    (is (= (unpack int-map (pack int-map {"a" 1 "b" 2} ~encoder) :decoder ~decoder) {"a" 1 "b" 2}))

    (is (= (unpack a-union (pack a-union "test" ~encoder) :decoder ~decoder) "test"))
    (is (= (unpack a-union (pack a-union 10 ~encoder) :decoder ~decoder) 10))

    (let [pu# (unpack MyFixed (pack MyFixed (byte-array [(byte 1) (byte 2)]) ~encoder) :decoder ~decoder)]
      (is (= (nth pu# 0) 1))
      (is (= (nth pu# 1) 2)))

    (is (= (unpack MyEnum (pack MyEnum "A" ~encoder) :decoder ~decoder) "A"))
    (is (= (unpack MyEnum (pack MyEnum "B" ~encoder) :decoder ~decoder) "B"))
    (is (= (unpack MyEnum (pack MyEnum "C" ~encoder) :decoder ~decoder) "C"))

    (let [pu# (unpack MyRecord (pack MyRecord {"f1" 6 "f2" "test"} ~encoder) :decoder ~decoder)]
      (is (= (pu# "f1") 6))
      (is (= (pu# "f2") "test")))

    (is (= (unpack List (pack List recursive ~encoder) :decoder ~decoder) recursive))
    
    (is (= (unpack MyNestedRecord (pack MyNestedRecord nested-record ~encoder) :decoder ~decoder) nested-record))
    (is (= (unpack MyNestedRecord (pack MyNestedRecord nested-record ~encoder) :decoder ~decoder :fields [:f1]) {"f1" 10}))
    (is (= (unpack MyNestedRecord (pack MyNestedRecord nested-record ~encoder) :decoder ~decoder :fields [:f1 :f2]) {"f1" 10 "f2" {"f1" 20 "f2" "f2-f2"}}))
    (is (= (unpack MyNestedRecord (pack MyNestedRecord nested-record ~encoder) :decoder ~decoder :fields [[:f3 :f2]]) {"f3" {"f2" "f3-f2"}}))
    (is (= (unpack MyNestedRecord (pack MyNestedRecord nested-record ~encoder) :decoder ~decoder :fields [:f1 [:f3 :f2]]) {"f1" 10 "f3" {"f2" "f3-f2"}}))

  ))

(test-pack-unpack test-prim-types-pack-unpack-no-decoder nil nil)
(test-pack-unpack test-prim-types-pack-unpack-json json-encoder json-decoder)
(test-pack-unpack test-prim-types-pack-unpack-binary binary-encoder binary-decoder)
