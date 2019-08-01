#!/usr/bin/env bash
#
# Copyright 2013 Netherlands eScience Center
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

MYDOCKERGID=`cut -d: -f3 < <(getent group docker)`

docker run \
--env MYUID=$UID \
--env DOCKERGID=$MYDOCKERGID \
--network host \
--volume $HOME/.gradle:/home/xenon/.gradle \
--volume $HOME/.m2:/home/xenon/.m2 \
--volume /var/run/docker.sock:/var/run/docker.sock \
--volume $PWD:/code \
--tty --interactive --rm \
--name=xenon-fixed-client \
xenonmiddleware/fixed-client \
./gradlew --no-daemon fixedClientEnvironmentTest "$@"
