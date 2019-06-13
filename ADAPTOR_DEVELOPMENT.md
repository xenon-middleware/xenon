# Adding an adaptor

> This documentation is out of date, for now use https://github.com/xenon-middleware/xenon-adaptors-cloud as an example.

To add an adaptor the following steps must be performed:
* source code
* dependencies (optional)
* unit tests
* integration tests
* Docker based server for adaptor to test against in integration tests
* registration in Xenon engine
* registration in build system

For a new file adaptor use `webdav` as an example. 
For a new job adaptor use `slurm` as an example. 

Adding an adaptor can be completed by adding/changing files in the following locations:
1. Source code in `src/main/java/nl/esciencecenter/xenon/adaptors/<adaptor name>`.
2. Specify dependencies of adaptor in `build.gradle`.
3. Unit tests in `src/test/java/nl/esciencecenter/xenon/adaptors/<adaptor name>`.
4. Register adaptor in `src/main/java/nl/esciencecenter/xenon/engine/XenonEngine.java:loadAdaptors()`
5. Integration tests
  1. Create a Dockerfile in `src/integrationTest/docker/xenon-<adaptor-name>` for a server of the adaptor to test against
  2. Register the Docker image in `src/integrationTest/docker/docker-compose.yml` and  `gradle/docker.gradle`
  3. Add the Docker container credentials/location/configuration to `src/integrationTest/docker/xenon.test.properties.docker`
  4. Create an integration test in `src/integrationTest/java/esciencecenter/xenon/adaptors/<adaptor name>/`
