#!/usr/bin/env bash

docker run -d --name=xenon-ssh -h xenon-ssh nlesc/xenon-ssh
XENON_SSH_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-ssh)

docker run -d --name=xenon-ftp -h xenon-ftp nlesc/xenon-ftp
XENON_FTP_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-ftp)

docker run -d --name=xenon-webdav -h xenon-webdav nlesc/xenon-webdav
XENON_WEBDAV_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-webdav)

docker run -d --name=xenon-slurm -h xenon-slurm nlesc/xenon-slurm
XENON_SLURM_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-slurm)

docker run -d --name=xenon-gridengine -h xenon-gridengine nlesc/xenon-gridengine
XENON_GRIDENGINE_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-gridengine)

docker run -d --name=xenon-torque -h xenon-torque --privileged nlesc/xenon-torque
XENON_TORQUE_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-torque)
