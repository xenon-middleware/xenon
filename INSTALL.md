Xenon Installation
==================

Copyright 2013 The Netherlands eScience Center

Requirements
------------

To run Xenon, the following tools are required:

- A release of the Xenon library.
- Java 7 SE JDK.

To compile Xenon from source, you will also need:  

- Apache ANT


Obtaining Xenon
----------------

Xenon releases can be obtained from the Netherlands eScience Center
GitHub page at https://github.com/NLeSC/Xenon/releases. You can 
download it there as a zip archive, and unpack to the desired 
location. Both source and binary distributions of Xenon are avalable.
Alternatively, you can checkout the latest development version using 
Git.


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


Compiling Xenon from source
---------------------------

To compile Xenon, download a source distribution and unpack it in 
a suitable location. Also ensure you have both Java and Ant 
correctly installed (as described above).

Next, run `ant` in the main directory of Xenon. This will compile 
the Xenon library and generate the javadoc. After compilation, the 
`dist` subdirectory will contain a complete binary distribution of 
Xenon.


Examples
--------

Xenon comes with a set of examples. These are located in the 
`examples` directory of the _binary_ distribution (which you 
download or create from source, as described above). A list 
of the available examples can be found in the user 
documentation.

To compile the examples, go to the `examples` directory and 
run `ant`.  



To run the examples, ensure that all jar files in the `dist` 
directory are in the classpath. In addition, the directory 
containing the `logback.xml` file must also be in your classpath. 

For example, running the following command from the directory where
Xenon was installed should run the `CreatingXenon` example:

   java -cp dist:dist/* nl.esciencecenter.Xenon.examples.CreatingXenon

Note that the classpath is specified in Linux/OSX format here. On 
Windows use `dist;dist\*`.




