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
RUN chmod go-r .ssh/id_rsa

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
ln -s /home/xenon/xenon_test/links/link5 xenon_test/links/link6

RUN chown -R xenon.xenon .
