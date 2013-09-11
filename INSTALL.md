Octopus Installation
=======

Copyright 2013 The Netherlands eScience Center

Requirements
------------

To run Octopus, the following tools are required:

- A release of the Octopus library.
- Java 7 SE JDK.

To compile Octopus from source, you will also need:  

- Apache ANT


Obtaining Octopus
-----------------

Octopus releases can be obtained from the Netherlands eScience Center
GitHub page at https://github.com/NLeSC/octopus/releases. You can 
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

To compile Octopus, run `ant` in the main Octopus directory. This 
will: 

- Compile Octopus and create an `octopus-<version>.jar` in the `dist`
  directory. 
- Compile the examples, and create an 
  `octopus-examples-<version>.jar` to the `dist` directory. 
- Generate the Javadoc for the public Octopus API and create an 
  `octopus-javadoc-<version>.jar` in the `dist` directory.
- Copy all dependencies of Octopus from `lib` to `dist`.
- Copy the `logback.xml` from the root directory to `dist`.

After compiling, the `dist` directory contains a complete binary 
distribution of Octopus without any external depencencies. If needed, 
you can copy the contents of `dist` into your own software project.


Examples
--------

Octopus comes with a set of examples, located in the `examples` 
directory. The examples are build as part of the compile process 
described above. A short description of the examples can be found 
in the user documentation.

To run the examples, ensure that all jar files in the `dist` 
directory are in the classpath. In addition, the directory 
containing the `logback.xml` file must also be in your classpath. 

For example, running the following command from the directory where
Octopus was installed should run the `CreatingOctopus` example:

   java -cp dist:dist/* nl.esciencecenter.octopus.examples.CreatingOctopus

Note that the classpath is specified in Linux/OSX format here. On 
Windows use `dist;dist\*`.




