- [Xenon development](#xenon-development)
- [Offline build](#offline-build)
	- [Update offline jars](#update-offline-jars)
- [Integration tests](#integration-tests)
	- [Using live systems](#using-live-systems)
	- [Using docker containers](#using-docker-containers)
		- [Filtering tests](#filtering-tests)
- [Release a new version](#release-a-new-version)

# Xenon development

Xenon uses the Gradle build automation tool.
Use `./gradlew tasks` to list all available tasks.

# Offline build

Prerequisites:

* JDK
* Gradle, http://gradle.org/

The dist can be built offline with
```
gradle -b build-offline.gradle --offline build
```
Replace `build` to run another gradle task offline.

Offline limitations/workarounds:

* Requires that gradle is installed
* No test coverage, as coverage report generation requires download
* Publishing to Bintray must be done manually

## Update offline jars

To work offline all the jars required by Xenon are available in the `lib/` folder.

When dependencies are added to the `build.gradle` file the jar files of the dependencies must be added to the `lib/` folder.
This can be done with the following command:

```
./gradlew downloadDependencies
```

PS. Do not forget to commit the downloaded jar files.

# Integration tests

## Using live systems

To test against a live system (eg. HPC cluster) a configuration file must be supplied.
An example configuration file can be found at `src/integrationTest/resources/xenon.test.properties.examples`.
Create a configuration file in the root of the Xenon repository called `xenon.test.properties` and configure it for the live systems you want to test against.

The integration tests can be run with:

```
../gradlew integrationTest
```

## Using docker containers

Prerequisites:

* Hardware/OS
  * 2 Cores (required by some schedulers)
  * Xenon repo not residing on a Virtualbox share
  * Minimal 2Gb RAM
* Docker, https://www.docker.com/
  * Without sudo
* Docker compose, https://docs.docker.com/compose/

Integrations tests will be run inside a docker image against several docker containers containing servers for all the Xenon adaptors.

Using Docker we have control credentials/hostnames/locations in the test environment.

The docker integrations tests can be run with:

```
./gradlew dockerIntegrationTest -Pdocker.uid=$UID
```

The $UID is the current user identifier. It is used so the output of the integration tests are owned by the current user.

See `src/integrationTest/README.md` for more information about the used docker containers.

### Filtering tests

The default `--tests` argument does not work for the docker integration tests instead use:
```
./gradlew dockerIntegrationTest -Pdocker.uid=$UID -Pdocker.tests='*slurm*'
```

# Release a new version

1. Update git master branch
2. Create a distribution with `./gradlew build`
3. Create release on https://github.com/NLeSC/Xenon/releases
4. Upload artifacts to bintray/jcenter
5. Update website with new Javadoc
6. Celebrate
