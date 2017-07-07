#!/usr/bin/env bash

# TODO rename nlesc/xenon-test to nlesc/xenon-fixed-client, see https://github.com/NLeSC/xenon-docker-images/issues/7

docker run \
-e MYUID=$UID \
--network host \
--name=xenon-client \
-ti --rm \
-v $HOME/.gradle:/home/xenon/.gradle \
-v $PWD:/code \
-v /var/run/docker.sock:/var/run/docker.sock \
nlesc/xenon-test \
./gradlew --no-daemon fixedClientEnvironmentTest "$@"