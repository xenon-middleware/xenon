The integration tests of Xenon can be run against the docker images in this directory or live systems.

There is a docker image for each major version the specific Xenon adaptor supports.

The docker images are registered at the docker hub.

# Build

The build step is optional as the images are available on docker hub.

The docker images are built by running

```
./gradlew dockerBuild -Pdocker.uid=$UID
```

where `docker.uid` is the base user UID on the docker system. Building docker containers will send the build directory to the daemon. Because of this we can't use a shared set of keys between base images so they are copies. To build a subset of the images, specify a simple wildcard in the `docker.filter` property:

```
./gradlew dockerBuild -Pdocker.uid=$UID -Pdocker.filter='*ftp'
```

# Push

The build images can be pushed to [NLeSC docker hub](https://hub.docker.com/u/nlesc/) with
```
docker login
./gradlew dockerPush
```

You must have push rights in the `nlesc` organization. Again, use a wildcard with `docker.filter` to only push a subset of the images.

# Run integration tests

Run the integration tests against the docker containers using docker compose. Tests suite is run from a docker container so it can connect to linked containers and has ssh/globus keys. The docker container is run with your own UID so test results are also owned by you.

```
./gradlew dockerIntegrationTest -Pdocker.uid=$UID
```

To filter the tests using Gradle's filtering mechanism, use the `docker.tests` property:

```
./gradlew dockerIntegrationTest -Pdocker.uid=$UID -Pdocker.tests='*ftp*'
```

