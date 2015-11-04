#!/usr/bin/env bash

class_path=../lib/*:$(ls Xenon-*-examples.jar)

java -cp $class_path nl.esciencecenter.xenon.examples.CreatingXenon

java -cp $class_path nl.esciencecenter.xenon.examples.CreatingXenonWithProperties

java -cp $class_path nl.esciencecenter.xenon.examples.files.CreateLocalFileSystem

java -cp $class_path nl.esciencecenter.xenon.examples.files.CreateFileSystem file:///

java -cp $class_path nl.esciencecenter.xenon.examples.files.CreateFileSystem ssh://$USER@localhost

java -cp $class_path nl.esciencecenter.xenon.examples.files.DirectoryListing file:///etc

java -cp $class_path nl.esciencecenter.xenon.examples.files.DirectoryListing ssh://$USER@localhost/etc

java -cp $class_path nl.esciencecenter.xenon.examples.files.LocalFileExists $PWD/README.md

java -cp $class_path nl.esciencecenter.xenon.examples.files.FileExists file://$PWD/README.md

java -cp $class_path nl.esciencecenter.xenon.examples.files.FileExists ssh://$USER@localhost/$PWD/README.md

java -cp $class_path nl.esciencecenter.xenon.examples.files.ShowFileAttributes file://$PWD/README.md

java -cp $class_path nl.esciencecenter.xenon.examples.files.ShowFileAttributes ssh://$USER@localhost/$PWD/README.md

java -cp $class_path nl.esciencecenter.xenon.examples.files.CopyFile file://$PWD/README.md file:///tmp/Copy.Of.README.md
ls /tmp/Copy.Of.README.md
rm /tmp/Copy.Of.README.md

# start a scheduler, so jobs can be submitted. Local and ssh scheduler can't be used because they run in-memory
docker run -d --name slurm_test -p 2234:22 nlesc/xenon-slurm

java -cp $class_path nl.esciencecenter.xenon.examples.jobs.ListJobs slurm://xenon:javagat@localhost:2234

docker rm -f slurm_test