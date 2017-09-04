
Xenon
=====

[![Build Status](https://travis-ci.org/NLeSC/Xenon.svg?branch=develop)](https://travis-ci.org/NLeSC/Xenon)
[![Build status Windows](https://ci.appveyor.com/api/projects/status/h4l4wn158db23kuf?svg=true)](https://ci.appveyor.com/project/NLeSC/xenon)
[![codecov.io](https://codecov.io/github/NLeSC/Xenon/coverage.svg?branch=master)](https://codecov.io/github/NLeSC/Xenon?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/457da68977d1406c9ea93d340720d340)](https://www.codacy.com/app/NLeSC/Xenon)
[![SonarQube Coverage](https://sonarqube.com/api/badges/measure?key=nlesc%3AXenon&metric=coverage)](https://sonarqube.com/component_measures/domain/Coverage?id=nlesc%3AXenon)
[![GitHub license](https://img.shields.io/badge/license-Apache--2.0%20-blue.svg)](https://github.com/NLeSC/Xenon/blob/master/LICENSE)
[![Download](https://jitpack.io/v/NLeSC/Xenon.svg)](https://jitpack.io/#NLeSC/Xenon)
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

Adding Xenon as a dependency to your project
--------------------------------------------

Follow the instructions from [jitpack.io](https://jitpack.io/#NLeSC/Xenon/2.0.0-rc2) to include Xenon as a 
dependency for Gradle, Maven, SBT, or Leiningen projects, e.g. Gradle:

```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

and 

```gradle
	dependencies {
	        compile 'com.github.NLeSC:Xenon:2.0.0-rc2'
	}

```

Or for a Maven project,

```maven
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

and


```maven
	<dependency>
	    <groupId>com.github.NLeSC</groupId>
	    <artifactId>Xenon</artifactId>
	    <version>2.0.0-rc2</version>
	</dependency>
```


Simple examples
---------------

Here are some examples of basic operations you can perform with Xenon: 

#### Copying a file from a local filesystem to a remote filesystem

```java
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.CopyMode;
import nl.esciencecenter.xenon.filesystems.CopyStatus;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class CopyFileLocalToSftpAbsolutePaths {

    public static void main(String[] args) throws Exception {

        // Use the file system adaptors to create file system representations; the remote file system
        // requires credentials, so we need to create those too.
        //
        // Assume the remote system is actually just a Docker container (e.g.
        // https://hub.docker.com/r/nlesc/xenon-ssh/), accessible via
        // port 10022 on localhost
        String location = "localhost:10022";
        String username = "xenon";
        char[] password = "javagat".toCharArray();
        PasswordCredential credential = new PasswordCredential(username, password);
        FileSystem localFileSystem = FileSystem.create("file");
        FileSystem remoteFileSystem = FileSystem.create("sftp", location, credential);

        // create Paths for the source and destination files, using absolute paths
        Path sourceFile = new Path("/etc/passwd");
        Path destFile = new Path("/tmp/password");

        // create the destination file only if the destination path doesn't exist yet
        CopyMode mode = CopyMode.CREATE;
        boolean recursive = false;

        // perform the copy and wait 1000 ms for the successful or otherwise
        // completion of the operation
        String copyId = localFileSystem.copy(sourceFile, remoteFileSystem, destFile, mode, recursive);
        long timeoutMilliSecs = 1000;
        CopyStatus copyStatus = localFileSystem.waitUntilDone(copyId, timeoutMilliSecs);

        // throw any exceptions
        XenonException copyException = copyStatus.getException();
        if (copyException != null) {
          throw copyException;
        }
    }
}
```

#### Submitting a job

The following code performs a wordcount of a file residing on a remote machine: 

```java 
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class SlurmSubmitWordCountJob {

    public static void main(String[] args) throws Exception {

        // Assume the remote system is actually just a Docker container (e.g.
        // https://hub.docker.com/r/nlesc/xenon-slurm/), accessible to user 'xenon' via
        // port 10022 on localhost, using password 'javagat'
        String location = "localhost:10022";
        String username = "xenon";
        char[] password = "javagat".toCharArray();
        PasswordCredential credential = new PasswordCredential(username, password);

        // create the SLURM scheduler representation
        Scheduler scheduler = Scheduler.create("slurm", location, credential);

        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/wc");
        description.setArguments("-l", "/etc/passwd");
        description.setStdout("/tmp/wc.stdout.txt");

        // submit the job
        String jobId = scheduler.submitBatchJob(description);

        long WAIT_INDEFINITELY = 0;
        JobStatus jobStatus = scheduler.waitUntilDone(jobId, WAIT_INDEFINITELY);

        // print any exceptions
        Exception jobException = jobStatus.getException();
        if (jobException != null)  {
          throw jobException;
        }

    }
}
```

The output of the job will be written to ``/tmp/wc.stdout.txt`` file in the ``nlesc/slurm:17`` Docker container.

Supported middleware
--------------------

Xenon currently supports the following file access mechanisms:

- ``file`` (local file manipulation)
- ``ftp``
- ``sftp``
- ``webdav``
- ``s3``

Xenon currently supports the following job submission mechanisms:

- ``local`` (interactive jobs only)
- ``ssh`` (interactive jobs only)
- ``gridengine`` (batch jobs only)
- ``slurm`` (interactive and batch jobs)
- ``torque`` (batch jobs only)  

Planned extensions include: 

- Swift
- HDFS (almost done)
- YARN
- GridFTP
- glite
- Azure-Batch
- Amazon-Batch

Documentation
-------------

Xenon's JavaDoc is available online at <https://jitpack.io/com/github/NLeSC/Xenon/master-SNAPSHOT/javadoc/index.html>.

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
