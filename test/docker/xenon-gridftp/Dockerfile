# Docker container with gridftp server, to run the xenon gridftp adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-gridftp .
#
# Run with:
#
#

FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y libglobus-ftp-client2 \
 libglobus-gridftp-server-control0 globus-gridftp-server-progs \
 globus-simple-ca globus-gsi-cert-utils-progs fakeroot dpkg-dev cdbs openssh-client \
&& \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
mkdir /etc/ca-files

ADD files /etc/ca-files
RUN dpkg -i /etc/ca-files/globus-simple-ca-*_all.deb
# TODO create user mapping

# TODO init script which setups host certificates and start gridftp server

PORT 2811

CMD ["/sbin/my_init"]
