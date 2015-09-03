The integration tests of Xenon can be run against the docker images in this directory or live systems.

There is a docker image for each major version the specific Xenon adaptor supports.

The docker images are registered at the docker hub.

# Build

The build step is optional as the images are available on docker hub.

The docker images can be build by running

```
./build.sh
```

Building docker containers will send the build directory to the deamon.
Because of this we can't use a shared set of keys between base images so they are copies.

# Push

The build images can be pushed to docker hub at https://hub.docker.com/u/nlesc/ with
```
docker login
push.sh
```

You must have push rights in the nlesc organization.

# Run integration tests

Run the integration tests against the docker containers using docker compose.
Tests suite is run from a docker container so it can connect to linked containers and has ssh/globus keys.
The docker container is run with your own UID so test results are also owned by you.

```
cd src/integrationTest/docker
cp xenon.test.properties.docker ../../../xenon.test.properties
docker-compose run -e MYUID=$UID --rm xenon-test
docker-compose kill && docker-compose rm -f
```

To run filter tests use
```
docker-compose run -e MYUID=$UID --rm xenon-test ./gradlew integrationTest --tests *gftp*
```
