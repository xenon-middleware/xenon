#!/bin/bash
echo "+-------+"
echo "| clean |"
echo "+-------+"
./gradlew clean

echo "+--------------------------+"
echo "| checking license headers |"
echo "+--------------------------+"
./gradlew license

echo "+---------------------+"
echo "| checking code style |"
echo "+---------------------+"
./gradlew spotlessJavaCheck

echo "+------------------+"
echo "| checking javadoc |"
echo "+------------------+"
./gradlew javadoc

echo "+------------------------------+"
echo "| checking development javadoc |"
echo "+------------------------------+"
./gradlew javadocDevel

echo "+------------+"
echo "| unit tests |"
echo "+------------+"
./gradlew test

echo "+--------------------+"
echo "| fixed client tests |"
echo "+--------------------+"
./src/fixedClientEnvironmentTest/resources/run-fixed-client-environment-test.sh

echo "+-------------------+"
echo "| integration tests |"
echo "+-------------------+"
./gradlew integrationTest

echo "+-----------------------------+"
echo "| generate merged test report |"
echo "+-----------------------------+"
./gradlew jacocoMergedTestReport

