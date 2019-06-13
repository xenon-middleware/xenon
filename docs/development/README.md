# Xenon development documentation

This documentation is only intended for developers working on Xenon itself. 

For more information, examples and a tutorial on how to use the Xenon library see <https://github.com/NLeSC/Xenon-examples>.

The javadoc aimed at users of the Xenon library is available at <http://nlesc.github.io/Xenon/versions/1.1.0/javadoc>.

The javadoc aimed at developers of the Xenon library is available at <http://nlesc.github.io/Xenon/versions/1.1.0/javadoc-devel>.

- [Integration tests](#integration-tests)
	- [Using live systems](#using-live-systems)
	- [Using docker containers](#using-docker-containers)
		- [Filtering tests](#filtering-tests)
- [Release a new version](#release-a-new-version)

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
* Docker, https://www.docker.com/, tested with version 1.9.1
  * Without sudo
* Docker compose, https://docs.docker.com/compose/, tested with version 1.5.0

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

1. Update version in `gradle/common.gradle`, `CHANGLOG.md` and `README.md` files
2. Update site with `./gradlew publishSite`
3. Update git master branch
4. Create a distribution with `./gradlew build`
5. Create release on https://github.com/NLeSC/Xenon/releases
6. Upload artifacts to bintray/jcenter with `BINTRAY_USER=<my bintray username> BINTRAY_KEY=<my bintray API key> ./gradlew publish`
7. Make version public on bintray
8. Update related repros such as Xenon-examples, pyXenon, xenon-cli, etc.
9. Celebrate

Website is hosted on Github pages.
