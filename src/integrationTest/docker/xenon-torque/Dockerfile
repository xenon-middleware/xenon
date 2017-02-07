# Docker container with ssh server, to run the xenon torque adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-torque .
#
# Run with:
#
#     docker run -d -h xenon-torque -p 10022:22 --privileged nlesc/xenon-torque
#
#     ssh -p 10022 xenon@localhost
#     # password: javagat
#     qstat -q
#
# Note: Torque does not like hostname that start with number, so hostname is required
#

FROM agaveapi/torque
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

# Create a test user
RUN /usr/sbin/useradd -p $(openssl passwd javagat) -d /home/xenon -m --shell /bin/bash xenon
WORKDIR /home/xenon
ADD insecure-ssh-keys .ssh
RUN chmod 600 .ssh/*

# Create symlinks
ADD create_symlinks /tmp/
RUN /tmp/create_symlinks && rm -f /tmp/create_symlinks && chown -R xenon.xenon .

RUN chown -R xenon.xenon .

ADD docker_entrypoint.sh /docker_entrypoint.sh
