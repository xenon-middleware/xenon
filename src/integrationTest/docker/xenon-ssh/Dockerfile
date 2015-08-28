# Docker container with ssh server, to run the xenon ssh adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-ssh .
#
# Run with:
#
#     docker run -d --name=xenon-ssh -h xenon-ssh nlesc/xenon-ssh
#
# Get containers ip with
#
#     XENON_SSH_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-ssh)
#
# Login by
#
#     ssh-keyscan -t rsa -H $XENON_SSH_LOCATION >> ~/.ssh/known_hosts
#     # with keys
#     ssh -i ../xenon-alpine-base/insecure-ssh-keys/id_rsa.pub xenon@$XENON_SSH_LOCATION
#     # with password javagat
#     ssh xenon@$XENON_SSH_LOCATION
#
# Run xenon test with
#
# test.ssh.location=$XENON_SSH_LOCATION
# test.ssh.user=xenon
# test.ssh.password=javagat
FROM nlesc/xenon-alpine-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apk update && /sbin/setup-sshd -c openssh && rm -rf /var/cache/apk/* || /bin/true && \
ssh-keygen -N "" -t rsa -f /etc/ssh/ssh_host_rsa_key && \
ssh-keygen -N "" -t dsa -f /etc/ssh/ssh_host_dsa_key && \
ssh-keygen -N "" -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key && \
ssh-keygen -N "" -t ed25519 -f /etc/ssh/ssh_host_ed25519_key && \
echo "StrictHostKeyChecking no" >> /etc/ssh/ssh_config && \
echo "HashKnownHosts no" >> /etc/ssh/ssh_config

EXPOSE 22
CMD ["/usr/sbin/sshd", "-De"]
