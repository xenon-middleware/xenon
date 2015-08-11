# Docker container with ssh server, to run the xenon ssh adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-ssh .
#
# Run with:
#
#     docker run -d --name=xenon-ssh -u $USER nlesc/xenon-ssh
#
# Get containers ip with
#
#     XENON_SSH_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-ssh)
#
# Login by
#
#     ssh-keyscan -t rsa -H $XENON_SSH_LOCATION >> ~/.ssh/known_hosts
#     # with keys
#     ssh -i insecure-ssh-keys/id_rsa.pub xenon@$XENON_SSH_LOCATION
#     # with password javagat
#     ssh xenon@$XENON_SSH_LOCATION
#
# Run xenon test with
#
# test.ssh.location=$XENON_SSH_LOCATION
# test.ssh.user=xenon
# test.ssh.password=javagat

FROM phusion/baseimage
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN rm -f /etc/service/sshd/down
RUN /etc/my_init.d/00_regen_ssh_host_keys.sh

# Create a test user
RUN /usr/sbin/useradd -p $(openssl passwd javagat) -d /home/xenon -m --shell /bin/bash xenon
WORKDIR /home/xenon
ADD insecure-ssh-keys .ssh

# Create symlinks
RUN mkdir -p xenon_test/links && \
echo "Hello World" > xenon_test/links/file0 && \
touch xenon_test/links/file1 && \
ln -s xenon_test/links/file0 xenon_test/links/link0 && \
ln -s xenon_test/links/file1 xenon_test/links/link1 && \
ln -s xenon_test/links/file2 xenon_test/links/link2 && \
ln -s xenon_test/links/link0 xenon_test/links/link3 && \
ln -s xenon_test/links/link2 xenon_test/links/link4 && \
ln -s xenon_test/links/link6 xenon_test/links/link5 && \
ln -s xenon_test/links/link5 xenon_test/links/link6
RUN chown -R xenon.xenon .

CMD ["/sbin/my_init"]