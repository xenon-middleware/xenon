#!/bin/bash
echo "+-------+"
echo "| clean |"
echo "+-------+"
./gradlew clean

echo "+--------------------------+"
echo "| checking license headers |"
echo "+--------------------------+"
./gradlew license || { echo 'licence check failed' ; exit 1; }

echo "+---------------------+"
echo "| checking code style |"
echo "+---------------------+"
./gradlew spotlessJavaCheck || { echo 'code style check failed' ; exit 1; }


echo "+------------------+"
echo "| checking javadoc |"
echo "+------------------+"
./gradlew javadoc || { echo 'generating user javadoc failed' ; exit 1; }

echo "+------------------------------+"
echo "| checking development javadoc |"
echo "+------------------------------+"
./gradlew javadocDevel || { echo 'generating development javadoc failed' ; exit 1; }

echo "+------------+"
echo "| unit tests |"
echo "+------------+"
./gradlew test || { echo 'unit test failed' ; exit 1; }

echo "+--------------------+"
echo "| fixed client tests |"
echo "+--------------------+"
./src/fixedClientEnvironmentTest/resources/run-fixed-client-environment-test.sh || { echo 'fixed client test failed' ; exit 1; }

echo "+-------------------+"
echo "| integration tests |"
echo "+-------------------+"
./gradlew integrationTest || { echo 'integration test failed' ; exit 1; }

echo "+-----------------------------+"
echo "| generate merged test report |"
echo "+-----------------------------+"
./gradlew jacocoMergedTestReport || { echo 'generating coverage report failed' ; exit 1; }

