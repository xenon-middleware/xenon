#!/usr/bin/env bash

# Cant share files between Dockerfile context so need to copy files
# copy insecure ssh keys to image dirs
chmod go-r insecure-ssh-keys/id_rsa
cp -r insecure-ssh-keys xenon-alpine-base/
cp -r insecure-ssh-keys xenon-phusion-base/
cp -r insecure-ssh-keys xenon-torque/
# copy create_symlinks script to image dirs
cp ../scripts/create_symlinks xenon-alpine-base/
cp ../scripts/create_symlinks xenon-phusion-base/
cp ../scripts/create_symlinks xenon-torque/

docker build -t nlesc/xenon-alpine-base xenon-alpine-base
docker build -t nlesc/xenon-ssh xenon-ssh
docker build -t nlesc/xenon-webdav xenon-webdav
docker build -t nlesc/xenon-phusion-base xenon-phusion-base
docker build -t nlesc/xenon-ftp xenon-ftp
docker build -t nlesc/xenon-slurm xenon-slurm
docker build -t nlesc/xenon-gridengine xenon-gridengine
docker build -t nlesc/xenon-torque xenon-torque

if [ -e xenon-gridftp/files ] ; then
  echo "Certificate already exist, not recreating certificates"
else
  docker build -t nlesc/xenon-gridftp-ca xenon-gridftp/ca
  docker run -t --rm=true -e MYUID=$UID -v $PWD/xenon-gridftp/files:/var/lib/globus nlesc/xenon-gridftp-ca
fi

docker build -t nlesc/xenon-gridftp xenon-gridftp
