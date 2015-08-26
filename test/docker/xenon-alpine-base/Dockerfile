# Docker container with ftp server, to run the xenon ftp adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-alpine-base .
#
FROM alpine:3.2
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

# Create a test user
RUN /usr/sbin/adduser -D xenon && echo xenon:javagat |chpasswd
WORKDIR /home/xenon
ADD insecure-ssh-keys /home/xenon/.ssh
RUN chmod go-r /home/xenon/.ssh/id_rsa

# xenon prepare test runs a bash based test script
RUN apk add --update bash && rm -rf /var/cache/apk/* || /bin/true

# Create symlinks
ADD create_symlinks /tmp/
RUN /tmp/create_symlinks && rm -f /tmp/create_symlinks && chown -R xenon.xenon .
