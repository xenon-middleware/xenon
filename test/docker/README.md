The integration tests of Xenon can be run against the docker images in this directory or live systems.

There is a docker image for each major version the specific Xenon adaptor supports.

The docker images are registered at the docker hub.

# Build

The docker images can be build by running

    ./build.sh

Building docker containers will send the build directory to the deamon.
Because of this we can't use a shared set of keys between base images so they are copies.

# Run integration tests

To run the integration tests against the docker containers. They must be started and a config file must be supplied.

   ./test/docker/start.sh
   ant integration -Dxenon.test.config=test/docker/xenon.test.properties.docker
   ./test/docker/stop.sh

