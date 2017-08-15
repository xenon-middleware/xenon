The Xenon library has different test suites:

* unit tests
* integration tests - Run the integration tests against docker containers
* fixed client environment tests - Run the integration tests within and against docker containers
* live tests - Runs the integration tests against live systems

# Run unit tests

The unit tests can be run using:
```bash
./gradlew test
```

# Run integration tests against Docker containers

Requirements:
* [docker](https://docs.docker.com/engine/installation/), v1.13 or greater
* [docker-compose](https://docs.docker.com/compose/), v1.10 or greater

The integration tests of Xenon is run against the Docker images.
 
The schedulers and file systems supported by Xenon all have Docker images on [Docker Hub](https://hub.docker.com/r/nlesc/).
See [https://github.com/NLeSC/xenon-docker-images](https://github.com/NLeSC/xenon-docker-images) for the source code of the images. The integration tests will start the Docker containers they need using [Docker compose junit rules](https://github.com/palantir/docker-compose-rule).

The integration tests against Docker containers can be run with:

```bash
./gradlew integrationTest
```

To filter the tests use [Gradle's filtering mechanism](https://docs.gradle.org/3.3/userguide/java_plugin.html#test_filtering).
For example to only run the tests with `ftp` in their package name or class name use:

```bash
./gradlew integrationTest --tests '*ftp*'
```

The tests can be slow, to see which test is running set the `CI` environment variable.
```
# To print test started/passed/etc. event to stdout
CI=1 ./gradlew integrationTest
```

# Run fixed client environment tests

Requirements:
* [docker](https://docs.docker.com/engine/installation/), v1.13 or greater
* [docker-compose](https://docs.docker.com/compose/), v1.10 or greater
* docker server running on localhost, which excludes MacOS and Windows as they run the Docker server in a VM.

Run tests which expect the client environment (like credentials and ssh agent) to be in a fixed state.

The [nlesc/xenon-fixed-client](https://hub.docker.com/r/nlesc/xenon-fixed-client/) Docker image is used as the fixed state client environment.

The fixed client environment tests can be run with:
```bash
# change to root directory of Xenon repository
./src/fixedClientEnvironmentTest/resources/run-fixed-client-environment-test.sh
```

The script will startup the [nlesc/xenon-fixed-client](https://hub.docker.com/r/nlesc/xenon-fixed-client/) Docker container which:
* has the current code mounted
* can communicate with Docker server, so tests can start filesystem/scheduler containers
* can connect to host local ports, so tests can connect to filesystem/scheduler containers
* has the gradle cache mounted, so it does not need to download all plugins and dependencies again
* runs as the current user, so it writes test results and coverage in the Gradle build directory as the current user
* runs the tests by running `./gradlew fixedClientEnvironmentTest` command

# Run live tests

Run tests against a (remote) system like a cluster with Slurm or an sftp server. 

It is the user's responsibility to manage the live system. For example, if you want to test whether certain files 
exist, you need to create them yourself. If you want to run the tests against the live system, you can run the `src/liveTest/resources/scripts/create_symlinks` script.

Supported arguments (`-D<name>=<value>`):
* `xenon.scheduler`, name of scheduler
* `xenon.filesystem`, name of file system
* `xenon.scheduler.location`, location of scheduler
* `xenon.filesystem.location`, location of filesystem
* `xenon.username`, username for location
* `xenon.password`, password for username
* `xenon.certfile`, path to certificate file
* `xenon.passphrase`, passphrase for certificate file
* `xenon.basedir`, path at location where `create_symlinks` script was run, will get combined with filesystem.getEntryPath() to form absolute path

Run examples
```bash
# slurm on das5 with default credentials
./gradlew liveTest -Dxenon.scheduler=slurm -Dxenon.scheduler.location=das5.vu.nl
# slurm on das5 with username/password
./gradlew liveTest -Dxenon.scheduler=slurm -Dxenon.scheduler.location=das5.vu.nl -Dxenon.username=username -Dxenon.password=password
# slurm on das5 with certificate file
./gradlew liveTest -Dxenon.scheduler=slurm -Dxenon.scheduler.location=das5.vu.nl -Dxenon.username=username -Dxenon.certfile=pathtocertfile [ -Dxenon.passphrase=passphrase ]
# slurm on das5 with default credentials and a custom property
./gradlew liveTest -Dxenon.scheduler=slurm -Dxenon.scheduler.location=das5.vu.nl -Dxenon.adaptors.slurm.strictHostKeyChecking=false
# sftp on localhost:10022
/gradlew liveTest -Dxenon.filesystem=sftp -Dxenon.filesystem.location=localhost:10022  -Dxenon.username=xenon -Dxenon.password=javagat -Dxenon.adaptors.file.sftp.strictHostKeyChecking=false -Dxenon.adaptors.file.sftp.loadKnownHosts=false
# local filesystem and scheduler
./gradlew liveTest -Dxenon.scheduler=local -Dxenon.filesystem=file -Dxenon.filesystem.location=/ -Dxenon.username=$USERNAME -Dxenon.basedir=$PWD
```
