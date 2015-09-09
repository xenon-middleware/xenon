Xenon Examples
==============

Copyright 2013 The Netherlands eScience Center

Requirements
------------

To compile and run Xenon examples, a working _binary distribution_
of Xenon is required. This binary distribution can either be
download or create from the source distribution, as described in
the "INSTALL.md" file.


Compiling the examples
----------------------

The Xenon examples are located in the `examples` directory of the
binary distribution. To compile these examples, go to this directory
and run `ant`.  

This will compile the examples and create a `dist/xenon-examples.jar`
file.


Running the examples
--------------------

To run the examples, ensure that the following jar files are in
the Java classpath:

- The `xenon-examples.jar` that was just created (see above).

- The `xenon-<version>.jar` and all its dependencies. These can be
  found in the `lib` directory of the binary distribution of Xenon.

- The directory that contains the `logback.xml` file. In the binary
  distribution this can be found in `etc`.

For example, running the following command from the `examples`
directory should run the `CreatingXenon` example:

   java -cp dist/*:../lib/*:../etc nl.esciencecenter.xenon.examples.CreatingXenon

Note that the classpath is specified in Linux/OSX format here. On
Windows use `dist\*;..\lib\*;..\etc`.


# Run

```
java -cp ../lib/*:Xenon-gradleize-1.1.0-SNAPSHOT-examples.jar nl.esciencecenter.xenon.examples.CreatingXenon
```

# Compile

After changing a example it can be compiled and run with

```
javac -cp "../lib/*" java/nl/esciencecenter/xenon/examples/CreatingXenon.java
java -cp ../lib/*:java nl.esciencecenter.xenon.examples.CreatingXenon
```
