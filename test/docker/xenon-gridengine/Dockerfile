#
# docker build -t nlesc/xenon-gridengine:2.6.5 .
#
FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y gridengine-master gridengine-exec gridengine-client && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# TODO pre-fill auto config

# TODO add init scripts for master and exec deamons

CMD ["/sbin/my_init"]

