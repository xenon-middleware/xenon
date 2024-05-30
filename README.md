
# Xenon

[![Unit tests linux](https://github.com/xenon-middleware/xenon/actions/workflows/unitTests.yml/badge.svg)](https://github.com/xenon-middleware/xenon/actions/workflows/unitTests.yml)
[![Unit tests windows](https://github.com/xenon-middleware/xenon/actions/workflows/tests-windows.yml/badge.svg)](https://github.com/xenon-middleware/xenon/actions/workflows/tests-windows.yml)
[![Unit tests macos](https://github.com/xenon-middleware/xenon/actions/workflows/tests-macos.yml/badge.svg)](https://github.com/xenon-middleware/xenon/actions/workflows/tests-macos.yml)
[![Integration tests](https://github.com/xenon-middleware/xenon/actions/workflows/integrationTests.yml/badge.svg)](https://github.com/xenon-middleware/xenon/actions/workflows/integrationTests.yml)
[![Fixed client tests](https://github.com/xenon-middleware/xenon/actions/workflows/fixedClientTests.yml/badge.svg)](https://github.com/xenon-middleware/xenon/actions/workflows/fixedClientTests.yml)
[![codecov](https://codecov.io/gh/xenon-middleware/xenon/branch/master/graph/badge.svg)](https://codecov.io/gh/xenon-middleware/xenon)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=xenon-middleware_xenon&metric=alert_status)](https://sonarcloud.io/dashboard?id=xenon-middleware_xenon)
[![cffvalidation](https://github.com/xenon-middleware/xenon/actions/workflows/cffconvert.yml/badge.svg)](https://github.com/xenon-middleware/xenon/actions/workflows/cffconvert.yml)
[![GitHub license](https://img.shields.io/badge/license-Apache--2.0%20-blue.svg)](https://github.com/xenon-middleware/xenon/blob/master/LICENSE)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.597993.svg)](https://doi.org/10.5281/zenodo.597993)
[![Research Software Directory](https://img.shields.io/badge/rsd-xenon-00a3e3.svg)](https://www.research-software.nl/software/xenon)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/3692/badge)](https://bestpractices.coreinfrastructure.org/projects/3692)
[![fair-software.eu](https://img.shields.io/badge/fair--software.eu-%E2%97%8F%20%20%E2%97%8F%20%20%E2%97%8F%20%20%E2%97%8F%20%20%E2%97%8F-green)](https://fair-software.eu)

Copyright 2013-2023 The Netherlands eScience Center

## What problem does Xenon solve?

Many applications use remote storage and compute resources. To do so, they need
to include code to interact with the scheduling systems and file transfer
protocols used on those remote machines.

Unfortunately, many different scheduler systems and file transfer protocols
exist, often with completely different programming interfaces. This makes it
hard for applications to switch to a different system or support multiple
remote systems simultaneously.

Xenon solves this problem by providing a single programming interface to many
different types of remote resources, allowing applications to switch without
changing a single line of code.

![Xenon abstraction](https://raw.githubusercontent.com/xenon-middleware/xenon/master/docs/images/readme-xenon-abstraction.svg.png "Xenon abstraction")

## How does Xenon work?

Xenon is an abstraction layer that sits between your application and the (usually remote)
resource it uses. Xenon is written in Java, but is also accessible from other
languages (e.g. Python) through its gRPC interface and via the command line.

![Xenon API](xhttps://raw.githubusercontent.com/xenon-middleware/xenon/blob/master/docs/images/readme-xenon-api.svg.png?raw=true "Xenon API")

### Overview of the Xenon ecosystem of tools

| component | repository |
|---|---|
| Xenon library | https://github.com/xenon-middleware/Xenon |
| Xenon cloud adaptors like s3 | https://github.com/xenon-middleware/xenon-adaptors-cloud |
| Xenon grid adaptors like gridftp| https://github.com/xenon-middleware/xenon-adaptors-grid |
| Xenon hadoop adaptors like hdfs | https://github.com/xenon-middleware/xenon-adaptors-hadoop |
| gRPC extension for Xenon | https://github.com/xenon-middleware/xenon-grpc |
| command line interface to Xenon | https://github.com/xenon-middleware/xenon-cli |
| Python API for Xenon | https://github.com/xenon-middleware/pyxenon |
| Docker images | https://github.com/xenon-middleware/xenon-docker-images |

## Supported middleware

Xenon currently supports the following file access mechanisms:

- ``file`` (local file manipulation)
- ``ftp``
- ``sftp``
- ``webdav``
- ``s3``
- ``hdfs``

Xenon currently supports the following job submission mechanisms:

- ``local``
- ``ssh``
- ``at``
- ``gridengine``
- ``slurm``
- ``torque``  

See the [roadmap](/ROADMAP.md) for the planned extensions.

## Adding Xenon as a dependency to your project

Follow the instructions from [bintray.com](https://bintray.com/nlesc/xenon/xenon) to include Xenon as a 
dependency for Gradle, Maven, SBT, or Leiningen projects, e.g. Gradle:

```gradle
	allprojects {
		repositories {
			...
			jcenter()
		}
	}
```

and 

```gradle
	dependencies {
	        compile group: 'nl.esciencecenter.xenon', name: 'xenon', version: '3.2.0'
	}

```

This will give the core adaptors to get cloud, grid and hadoop adaptors add the following dependencies:
```gradle
    compile group: 'nl.esciencecenter.xenon.adaptors', name: 'xenon-adaptors-cloud', version: '3.0.2'
    compile group: 'nl.esciencecenter.xenon.adaptors', name: 'xenon-adaptors-grid', version: '3.0.0'
    compile group: 'nl.esciencecenter.xenon.adaptors', name: 'xenon-adaptors-hadoop', version: '3.0.0'
```

## Simple examples

Here are some examples of basic operations you can perform with Xenon: 

### Copying a file from a local filesystem to a remote filesystem

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
        // https://hub.docker.com/r/xenonmiddleware/ssh/), accessible via
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

### Submitting a job

The following code performs a wordcount of a file residing on a remote machine: 

```java 
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class SlurmSubmitWordCountJob {

    public static void main(String[] args) throws Exception {

        // Assume the remote system is actually just a Docker container (e.g.
        // https://hub.docker.com/r/xenonmiddleware/slurm/), accessible to user 'xenon' via
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

The output of the job will be written to ``/tmp/wc.stdout.txt`` file in the ``xenonmiddleware/slurm`` Docker container.

For more examples, see the tutorial at [Read The Docs](http://xenonrse2017.readthedocs.io/).

## Documentation

Xenon's JavaDoc is available online at <http://xenon-middleware.github.io/xenon/>.

## Documentation for maintainers

For developers of Xenon itself 

* see [RELEASE.md](RELEASE.md) how to perform a release.
* see [ADAPTOR_DEVELOPMENT.md](ADAPTOR_DEVELOPMENT.md) how to write a new adaptor.

## Legal

The Xenon library is copyrighted by the Netherlands eScience Center and released
under the Apache License, Version 2.0. A copy of the license may be obtained
from [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).

Xenon uses several third-party libraries that have their own (permissive, open 
source) licenses. See the file [legal/README.md](legal/README.md) for an overview.

