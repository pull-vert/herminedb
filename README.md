# herminedb
Hermine DB is a ADBA SQL Database written in Java

## Building Hermine

Hermine require JDK 10 or later, and for information ADBA require JDK 9 or later. Download ADBA from the 
[OpenJDK sandbox](http://hg.openjdk.java.net/jdk/sandbox/file/JDK-8188051-branch/src/jdk.incubator.adba/share/classes). 
It does not have any dependencies outside of Java SE. 

For building the ADBA jar, and then add this jar to your local Maven Repository :
```
$ mkdir -p mods/jdk.incubator.adba
$ javac -d mods/jdk.incubator.adba/ $(find jdk.incubator.adba  -name "*.java")
$ jar --create --file=jdk.incubator.adba.jar --module-version=1.0 -C mods/jdk.incubator.adba/ .
$ mvn install:install-file -Dfile=jdk.incubator.adba.jar -DgroupId=jdk.incubator -DartifactId=adba -Dversion=1.0 -Dpackaging=jar
```

Clone Hermine from [GitHub](https://github.com/pull-vert/herminedb). Both are modularized with Jigsaw. Hermine depends on ADBA.

##  Modules
Main modules
* herminedb-engine is the database itself
* hermine-driver is the java adba asynchronous driver

Other modules
* hermine-jmh contains microbenchmarks to test performances of Hermine DB
* [hermine-playground](hermine-playground/README.md) is a free place to tests stuff

