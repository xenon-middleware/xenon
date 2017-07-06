# Run unit tests

The unit tests can be run using:
```bash
./gradlew test
```

# Run integration tests

The integration tests of Xenon can be run against the docker images in this directory or live systems.
 
## with Docker

There is a docker image for each major version the specific Xenon adaptor supports.

The docker images are registered at the docker hub.

Run the integration tests within and against the docker containers using docker compose. 
Tests suite is run from a docker container so it can connect to linked containers and has ssh/globus keys. 
The docker container is run with your own UID so test results are also owned by you.

```bash
./gradlew dockerIntegrationTest -Pdocker.uid=$UID
```

To filter the tests using Gradle's filtering mechanism, use the `docker.tests` property:

```bash
./gradlew dockerIntegrationTest -Pdocker.uid=$UID -Pdocker.tests='*ftp*'
```

The `docker.tests` property uses the same syntax as `--tests` described at https://docs.gradle.org/3.3/userguide/java_plugin.html#test_filtering

## against live systems

To run the Xenon integration tests a configuration file called `./xenon.test.properties` is required. 
You can use `src/integrationTest/resources/xenon.test.properties.examples` as a template.

The integration tests can be run with
```bash
./gradlew integrationTest
```

To run only integration test of local adaptor under Windows, this will not require Docker or remote systems:
```
echo '' > xenon.test.properties
mkdir build\integrationTest & cd build\integrationTest & src\integrationTest\resources\scripts\create_symlinks.bat & cd ..\..
./gradlew.bat check integrationTest -x prepareIntegrationTest -x testPropertiesFileExists --tests=*adaptors.local*
```

See https://docs.gradle.org/3.3/userguide/java_plugin.html#test_filtering how to filter tests with `--tests`.

# fixed client environment tests

Run tests which expect the client environment (like credentials and ssh agent) to be in a fixed state.

Task will startup multiple docker containers: the servers with filesystems/schedulers to test against and a container (xenon-test) which has the current code mounted and runs the tests.


Use script to start a Docker container which will run `./gradlew fixedClientEnvironmentTest` inside of it and start any Docker containers to tests against.
```bash
./src/fixedClientEnvironmentTest/resources/run-fixed-client-environment-test.sh
```

# Live tests

Run tests against a (remote) system like a cluster with Slurm or an sftp server. 

It is the user's responsibility to manage the live system. For example, if you want to test whether certain files 
exist, you need to create them yourself. If you want to run the tests against the live system, you can run the `src/liveTest/resources/scripts/create_symlinks` script.

To run tests you need the pass the `Scheduler.create` or `FileSystem.create` method arguments as command line arguments.
```bash
./gradlew liveTest -Dscheduler=slurm -Dlocation=das5.vu.nl -Dcredential=default DpropertiesFile=slurm-das5.props
```

To ignore test
```bash
./gradlew liveTest
```