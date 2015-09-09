#!/usr/bin/env bash

docker push nlesc/xenon-alpine-base
docker push nlesc/xenon-ssh
docker push nlesc/xenon-webdav
docker push nlesc/xenon-phusion-base
docker push nlesc/xenon-ftp
docker push nlesc/xenon-slurm
docker push nlesc/xenon-gridengine
docker push nlesc/xenon-gridengine-exec
docker push nlesc/xenon-torque

docker push nlesc/xenon-gridftp-ca

docker push nlesc/xenon-gridftp

docker push nlesc/xenon-test
