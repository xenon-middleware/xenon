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
 globus-gss-assist-progs && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
mkdir /etc/ca-files

# install ca certificates
ADD files /etc/ca-files
RUN dpkg -i /etc/ca-files/globus-simple-ca-*_all.deb

# create user mapping
ADD map_users.sh /usr/bin/
ADD map_user.sh /usr/bin/
RUN /usr/bin/map_users.sh

# init script which setups host certificates and starts gridftp server
RUN mkdir -p /etc/service/gridftp
ADD gridftp.sh /etc/service/gridftp/run

EXPOSE 2811

CMD ["/sbin/my_init"]
