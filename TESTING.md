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