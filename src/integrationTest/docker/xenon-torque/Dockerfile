# Docker container with ssh server, to run the xenon torque adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-torque .
#
# Run with:
#
#

FROM agaveapi/torque
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

# Create a test user
RUN /usr/sbin/useradd -p $(openssl passwd javagat) -d /home/xenon -m --shell /bin/bash xenon
WORKDIR /home/xenon
ADD insecure-ssh-keys .ssh
RUN chmod 600 .ssh/id_rsa

# Create symlinks
ADD create_symlinks /tmp/
RUN /tmp/create_symlinks && rm -f /tmp/create_symlinks && chown -R xenon.xenon .

RUN chown -R xenon.xenon .
