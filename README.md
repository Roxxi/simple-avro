# simple-avro
Clojure wrapper for Avro schema and serialization.


## Quick Start

### Schema definition

    (defavro-enum State
      "AL" "AK" "AS" "AZ" "AR" "CA" "CO" ; ...
      )

    (defavro-record Address
      :street  avro-string
      :city    avro-string
      :state   State
      :zip     avro-int
      :country avro-string)

    (defavro-record Contact
      :first   avro-string
      :last    avro-string
      :address Address
      :email   avro-string
      :phone   (avro-union avro-string avro-null))

_simple-avro_ implements all types defined in [Avro schema specification](http://avro.apache.org/docs/1.7.2/spec.html).
Just prepend _avro-_ to the type name or use plain string names. _defavro-_ macros defined for all named types
(_defavro-record_, _defavro-enum_ and _defavro-fixed_) create var objects convenient for hierarchical schema compositions.
Parameters _namespace_, _aliases_ and _doc_ can by provided in an optional argument map. In recursive type definitions use 
string names for type references, for example:

    (defavro-record IntList
      :value avro-int 
      :next  (avro-union "IntList" avro-null))

### Data serialization

    (def contact {:first "Mike" :last ...})
    (def packed (pack Contact contact <optional encoder>))
    (assert (= contact (unpack Contact packed <optional decoder>)))

_pack_ serializes objects into generic Avro objects. For json or binary serialization provide an optional _json-encoder_ or _binary-encoder_.
Use equivalent decoder to de-serialize objects using _unpack_.  _unpack_ takes an optional list of fields to deserialize from a record.
Use singe filed names or path vectors for nested records, for example _[:first [:address :city]]_ will deserialize only
the two fields first and city. If no fields provided, the entire record is deserialized. 

### Custom types API

_simple-avro.core_ supports only basic Avro types. For custom types import _simple-avro.api_ instead of _core_.
To add support for a new custom type first add a schema best matching the type. For example a Date object can be represented as:

    (defavro-type avro-date
      :time avro-long)

Second, register mapping functions from the custom object to Avro record and back using _pack-avro-instance_ and _unpack-avro-instance_:

    (pack-avro-instance Date
      (fn [date] 
        (avro-instance avro-date "time" (.getTime date))))
      
    (unpack-avro-instance avro-date
      (fn [rec]
        (Date. (rec "time"))))

Now you can use default pack/unpack methods to serialize Date objects:

    (unpack avro-date (pack avro-date (Date.)))

_simple-avro.api_ adds serialization support for Date, UUID and an _avro-maybe_ helper for optional values.
For more details see examples and unit tests.

## Installation

### Leiningen

    [roxxi/simple-avro "0.0.6"]
 
### Maven

    <dependency>
      <groupId>roxxi</groupId>
      <artifactId>simple-avro</artifactId>
      <version>0.0.6</version>
    </dependency>


Found a bug? Have a question? Drop me an email at roxxibear \_at\_ gmail.com.

