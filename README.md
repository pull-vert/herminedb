# herminedb
Hermine DB is a Asynchronous SQL Relational Database written in Kotlin, and the associated ADBA driver (Asynchronous DataBase Access)

It is based on Kotlin coroutines, and uses [kotlinx-coroutines library](https://github.com/Kotlin/kotlinx.coroutines)

## Building Hermine
Clone Hermine from [GitHub](https://github.com/pull-vert/herminedb).

Hermine require JDK 9 or later. It is modularized with Jigsaw.

## Modules
Main modules
* herminedb-engine is the database itself
* [herminedb-driver](herminedb-driver/README.md) is the asynchronous driver to use to connect to Hermine DB, implements [ADBA mirror](https://github.com/pull-vert/adba-mirror).
  * ADBA is Asynchronous Database Access, a non-blocking database access API that Oracle is proposing as a Java standard
* hermine-io contains the IO used by hermine DB engine and driver

Other modules
* hermine-jmh contains microbenchmarks for Hermine components
* [hermine-playground](hermine-playground/README.md) is a free place to test various stuff, can contain influences and ideas

## Inspirations and code origin
hermine-io's TCP is inspired by java.net.http from openjdk, that is a sandbox revision targeted for JDK11 of reactive (Flow) async http and http2 client created in JDK 9.
hermine-driver is inspired by [Oracle AoJ : ADBA over JDBC](https://github.com/oracle/oracle-db-examples/tree/master/java/AoJ)

Alternative to ADBA :
* [R2DBC](https://github.com/r2dbc/r2dbc-client) initiative to draft an entirely reactive database access API based on Reactive Streams and the [proposal from David Karnok](http://mail.openjdk.java.net/pipermail/jdbc-spec-discuss/2017-October/000164.html)
* [RDBC](https://pado.io/articles/2018-06/rdbc-asynchronous-database-api-scala-java) asynchronous database access API for Scala and Java
