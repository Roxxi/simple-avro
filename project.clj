(defproject simple-avro "0.0.3-1.4-SNAPSHOT"
  :description "Clojure wrapper around Avro schema and serialization."
  :url          "http://github.com/asmyczek/simple-avro"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.apache.avro/avro "1.4.1"]
                 ]
  :disable-deps-clean false
  :warn-on-reflection true
  :source-path "src"
  :test-path "test"
  :license {:name "Apache License - Version 2.0"
            :url "http://www.apache.org/licenses/"})

