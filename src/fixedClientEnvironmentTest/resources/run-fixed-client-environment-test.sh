#!/usr/bin/env bash

docker run \
--env MYUID=$UID \
--network host \
--volume $HOME/.gradle:/home/xenon/.gradle \
--volume $HOME/.m2:/home/xenon/.m2 \
--volume /var/run/docker.sock:/var/run/docker.sock \
--volume $PWD:/code \
--tty --interactive --rm \
--name=xenon-fixed-client \
nlesc/xenon-fixed-client \
./gradlew --no-daemon fixedClientEnvironmentTest "$@"
