Cobalt Installation
===================

Copyright 2013 The Netherlands eScience Center

Requirements
------------

To run Cobalt, the following tools are required:

- A release of the Cobalt library.
- Java 7 SE JDK.

To compile Cobalt from source, you will also need:  

- Apache ANT


Obtaining Cobalt
----------------

Cobalt releases can be obtained from the Netherlands eScience Center
GitHub page at https://github.com/NLeSC/Cobalt/releases. You can 
download it there as a zip archive, and unpack to the desired 
location. Alternatively, you can checkout the latest development 
version using Git.


Obtaining Java
--------------

Java can be be downloaded from: 

<http://www.oracle.com/technetwork/java/index.html>

Please ensure that you download Java SE 7 or higher. 

In Linux, Java can also be installed using various package managers,
such as "apt-get" or "yum".


Obtaining Ant
-------------

Ant can be be downloaded from: 

<http://ant.apache.org/>

In Linux, Ant can also be installed using various package managers,
such as "apt-get" or "yum".


Compiling
---------

To compile Cobalt, run `ant` in the main Cobalt directory. This 
will: 

- Compile Cobalt and create an `Cobalt-<version>.jar` in the `dist`
  directory. 
- Compile the examples, and create an 
  `Cobalt-examples-<version>.jar` to the `dist` directory. 
- Generate the Javadoc for the public Cobalt API and create an 
  `Cobalt-javadoc-<version>.jar` in the `dist` directory.
- Copy all dependencies of Cobalt from `lib` to `dist`.
- Copy the `logback.xml` from the root directory to `dist`.

After compiling, the `dist` directory contains a complete binary 
distribution of Cobalt without any external depencencies. If needed, 
you can copy the contents of `dist` into your own software project.


Examples
--------

Cobalt comes with a set of examples, located in the `examples` 
directory. The examples are build as part of the compile process 
described above. A list of the available examples can be found 
in the user documentation.

To run the examples, ensure that all jar files in the `dist` 
directory are in the classpath. In addition, the directory 
containing the `logback.xml` file must also be in your classpath. 

For example, running the following command from the directory where
Cobalt was installed should run the `CreatingCobalt` example:

   java -cp dist:dist/* nl.esciencecenter.cobalt.examples.CreatingCobalt

Note that the classpath is specified in Linux/OSX format here. On 
Windows use `dist;dist\*`.




