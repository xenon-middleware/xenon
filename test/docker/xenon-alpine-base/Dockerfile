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
ADD insecure-ssh-keys .ssh

# Create symlinks
RUN mkdir -p xenon_test/links && \
echo "Hello World" > xenon_test/links/file0 && \
touch xenon_test/links/file1 && \
ln -s /home/xenon/xenon_test/links/file0 xenon_test/links/link0 && \
ln -s /home/xenon/xenon_test/links/file1 xenon_test/links/link1 && \
ln -s /home/xenon/xenon_test/links/file2 xenon_test/links/link2 && \
ln -s /home/xenon/xenon_test/links/link0 xenon_test/links/link3 && \
ln -s /home/xenon/xenon_test/links/link2 xenon_test/links/link4 && \
ln -s /home/xenon/xenon_test/links/link6 xenon_test/links/link5 && \
ln -s /home/xenon/xenon_test/links/link5 xenon_test/links/link6 && \
chown -R xenon.xenon .