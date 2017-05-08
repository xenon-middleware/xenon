
Xenon
=====

[![Build Status](https://travis-ci.org/NLeSC/Xenon.svg?branch=develop)](https://travis-ci.org/NLeSC/Xenon)
[![Build status Windows](https://ci.appveyor.com/api/projects/status/h4l4wn158db23kuf?svg=true)](https://ci.appveyor.com/project/NLeSC/xenon)
[![codecov.io](https://codecov.io/github/NLeSC/Xenon/coverage.svg?branch=master)](https://codecov.io/github/NLeSC/Xenon?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/457da68977d1406c9ea93d340720d340)](https://www.codacy.com/app/NLeSC/Xenon)
[![SonarQube Coverage](https://sonarqube.com/api/badges/measure?key=nlesc%3Axenon&metric=coverage)](https://sonarqube.com/component_measures/domain/Coverage?id=nlesc%3Axenon)
[![GitHub license](https://img.shields.io/badge/license-Apache--2.0%20-blue.svg)](https://github.com/NLeSC/Xenon/blob/master/LICENSE)
[![Download](https://api.bintray.com/packages/nlesc/xenon/xenon/images/download.svg) ](https://bintray.com/nlesc/xenon/xenon/_latestVersion)
[![DOI](https://zenodo.org/badge/9236864.svg)](https://zenodo.org/badge/latestdoi/9236864)

Copyright 2013 The Netherlands eScience Center

What is it?
-----------

Xenon is a middleware abstraction library. It provides a simple
programming interface to various pieces of software that can be used
to access distributed compute and storage resources.

Why Xenon?
----------

Xenon is developed by the Netherlands eScience Center as a support
library for our projects. Several projects develop end-user
applications that require access to distributed compute and storage
resources. Xenon provides a simple API to those resources, allowing
those applications to be developed more rapidly. The experience
gained during end-user application development is used to improve
the Xenon API and implementation.

The Latest Version
------------------

Available in [JCenter](https://bintray.com/bintray/jcenter?filterByPkgName=xenon)

Details of the latest official 1.2.1 release of Xenon can be found at:

<https://github.com/NLeSC/Xenon/releases>

Alternatively, the latest development versions of Xenon can be found at:

<https://github.com/NLeSC/Xenon>.

Quick start
-----------

Add Xenon library as a dependency to your project. For a Maven project use
```
<dependency>
  <groupId>nl.esciencecenter.xenon</groupId>
  <artifactId>xenon</artifactId>
  <version>1.2.1</version>
</dependency>
```
For a gradle project make sure to include `jcenter` in the list of repositories, for example:
```
repositories {
    mavenCentral()
    jcenter()
}
```
Then include Xenon as a compile dependency:
```
dependencies {
    compile group: 'nl.esciencecenter.xenon', name: 'xenon', version: '1.2.1'
    // other dependencies ...
}
``` 
To compile Xenon from source, download the source distribution below and type:
```
gradlew installDist
```
This will create a binary distribution in `./build/install`

Simple examples
---------------

Here are some examples of basic operations you can perform with Xenon: 

#### Copy a file

Following code copies local /etc/passwd file using ssh to /tmp/password on somemachine:
```
xenon = XenonFactory.newXenon(null);
Files files = xenon.files();
FileSystem sourceFS = files.newFileSystem("local", null, null, null);
FileSystem targetFS = files.newFileSystem("ssh", "somemachine", null, null);
Path sourcePath = files.newPath(sourceFS, new RelativePath("/etc/passwd"));
Path targetPath = files.newPath(targetFS, new RelativePath("/tmp/passwd"));

files.copy(sourcePath, targetPath, CopyOption.CREATE);

files.close(sourceFS);
files.close(targetFS);
XenonFactory.endXenon(xenon);
```

#### Run a job

Following code performs a wordcount of a file on somemachine using ssh:  
```
xenon = XenonFactory.newXenon(null);
Jobs jobs = xenon.jobs();
Scheduler scheduler = jobs.newScheduler("ssh", "somemachine", null, null);
JobDescription description = new JobDescription();
description.setExecutable("/bin/wc");
description.setArguments("-l", "/tmp/passwd");
description.setStdout("/tmp/stdout.txt");

Job job = jobs.submitJob(scheduler, description);

jobs.close(scheduler);
XenonFactory.endXenon(xenon);
```
The output of the job will be written to /tmp/stdout.txt file on somemachine.

Supported middleware
--------------------

Xenon currently supports the following file access mechanisms:
- local
- ssh
- ftp
- sftp 
- WebDAV

Xenon currently supports the following job submission mechanisms:
- local (interactive jobs only)
- ssh (interactive jobs only)
- Slurm (interactive and batch jobs)
- Torque (batch jobs only)  
- GridEngine (batch jobs only)

Planned extensions include: 
- S3
- Swift
- HDFS 
- YARN
- GridFTP
- glite
- Azure-Batch
- Amazon-Batch

Documentation
-------------

See <https://github.com/NLeSC/Xenon-examples> for examples how to use the Xenon library.

See <https://github.com/NLeSC/Xenon-examples/raw/master/doc/tutorial/xenon-tutorial.pdf> for a tutorial pdf targeting inexperienced users.

The javadoc of Xenon library is available online at <http://nlesc.github.io/Xenon/versions/1.2.0/javadoc>.

See the file <https://github.com/NLeSC/Xenon/blob/master/doc/README.md> for information for developers of the Xenon library.

Copyrights & Disclaimers
------------------------

Xenon is copyrighted by the Netherlands eScience Center and
releases under the Apache License, Version 2.0.

See <http://www.esciencecenter.nl> for more information on the
Netherlands eScience Center.

See the "LICENSE" and "NOTICE" files for more information.

Third party libraries
---------------------

This product includes the SLF4J library, which is Copyright
(c) 2004-2013 QOS.ch See "notices/LICENSE.slf4j.txt" for the licence
information of the SLF4J library.

This product includes the JSch library, which is Copyright
(c) 2002-2012 Atsuhiko Yamanaka, JCraft,Inc.
See "notices/LICENSE.jsch.txt" for the licence information of the
JSch library.

This product includes the Logback library, which is Copyright
(c) 1999-2012, QOS.ch. See "notices/LICENSE.logback.txt" for the
licence information of the Logback library.

This product includes the JaCoCo library, which is Copyright
(c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors. See
"notices/LICENSE.jacoco.txt" for the licence information of the
JaCoCo library.

This project includes the JUnit library.
See "notices/LICENSE.junit.txt" for the licence information of the
JUnit library.

This project includes the Mockito library, which is Copyright
(c) 2007 Mockito contributors. See "notices/LICENSE.mockito.txt"
for the licence information of the Mockito library.

This project includes the Java CoG Kit, which is Copyright (c) 1999-2006
University of Chicago. See "notices/LICENSE.cog-jglobus.txt" for the
licence information of the Java CoG Kit.

This project includes the Commons-logging library, which is Copyright (c)
Apache Software Foundation. See "notices/LICENSE.commons-logging.txt"
for the licence information of the Commons-logging library.

This project includes the log4j library, which is Copyright (c) Apache
Software Foundation. See "notices/LICENSE.log4j.txt" for the licence
information of the log4j library.

This project includes the Legion of the Bouncy Castle Java cryptography
APIs, which are Copyright (c) 2000-2013 The Legion Of The Bouncy Castle.
See "notices/LICENSE.bouncycastle.txt" for the licence information of this
library.

This project includes the pureTLS library, which is Copyright (c) Claymore
Systems, Inc. See "notices/LICENSE.puretls.txt" for the licence information
of the pureTLS library.

This project includes libraries produced by the Cryptix Project. See
"notices/LICENSE.cryptix.txt" for the licence information of these libraries.
