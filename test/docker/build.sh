#!/usr/bin/env bash

docker build -t nlesc/xenon-alpine-base xenon-alpine-base
docker build -t nlesc/xenon-ssh xenon-ssh
docker build -t nlesc/xenon-ftp xenon-ftp
docker build -t nlesc/xenon-webdav xenon-webdav
docker build -t nlesc/xenon-phusion-base xenon-phusion-base
docker build -t nlesc/xenon-slurm xenon-slurm
docker build -t nlesc/xenon-gridengine xenon-gridengine
docker build -t nlesc/xenon-torque xenon-torque

 docker build -t nlesc/xenon-gridftp-ca ca
 docker run -t --rm=true -e MYUID=$UID -v $PWD/files:/var/lib/globus nlesc/xenon-gridftp-ca
 docker build -t nlesc/xenon-gridftp .
