# herminedb
Hermine DB is a Asynchronous SQL Database written in Java, and the associated ADBA driver (Asynchronous Database Access)

## Building Hermine

Hermine require JDK 10 or later. It is modularized with Jigsaw.

Clone Hermine from [GitHub](https://github.com/pull-vert/herminedb).

##  Modules
Main modules
* herminedb-engine is the database itself
* [herminedb-driver](herminedb-driver/README.md) is the java asynchronous driver to use to connect to Hermine DB, depends on [ADBA mirror](https://github.com/pull-vert/adba-mirror).
  *  ADBA is Asynchronous Database Access, a non-blocking database access API that Oracle is proposing as a Java standard
* hermine-io contains the IO used by hermine DB engine and driver

Other modules
* hermine-jmh contains microbenchmarks to test performances of Hermine DB
* [hermine-playground](hermine-playground/README.md) is a free place to test various stuff, can contain influences and ideas

