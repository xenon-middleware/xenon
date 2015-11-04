Xenon Examples
==============

Copyright 2013 The Netherlands eScience Center

WARNING
-------

This directory contains the source of the Xenon examples 
and a build file to compile then.

This build file will ONLY work if this directory is part 
of a binary Xenon distribution. It is not guaranteed 
to work in a source distribution. 

Requirements
------------

To run and compile Xenon examples, a working _binary distribution_
of Xenon is required. This binary distribution can either be
downloaded or create from the source distribution using `./gradlew assemble`.

Running the examples
--------------------

To run the examples, ensure that the following jar files are in
the Java classpath:

- The `Xenon-<version>-examples.jar` that was just created (see above).

- The `Xenon-<version>.jar` and all its dependencies. These can be
  found in the `lib` directory of the binary distribution of Xenon.

For example, running the following command from the `examples`
directory should run the `CreatingXenon` example:

```
java -cp ../lib/*:Xenon-*-examples.jar nl.esciencecenter.xenon.examples.CreatingXenon
```

Note that the classpath is specified in Linux/OSX format here. On
Windows use `;..\lib\*;Xenon-*-examples.jar`.

Compiling examples
------------------

After changing the source code of an example it can be compiled and run with

```
javac -cp "../lib/*" java/nl/esciencecenter/xenon/examples/CreatingXenon.java
java -cp ../lib/*:java nl.esciencecenter.xenon.examples.CreatingXenon
```
