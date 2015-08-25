#
# docker build -t nlesc/xenon-phusion-base .
#
#
FROM phusion/baseimage:0.9.17
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN rm -f /etc/service/sshd/down
RUN /etc/my_init.d/00_regen_ssh_host_keys.sh

# Create a test user
RUN /usr/sbin/useradd -p $(openssl passwd javagat) -d /home/xenon -m --shell /bin/bash xenon
WORKDIR /home/xenon
ADD insecure-ssh-keys .ssh
RUN chmod go-r .ssh/id_rsa

# Create symlinks
RUN mkdir -p xenon_test/links && \
echo "Hello World" > xenon_test/links/file0 && \
touch xenon_test/links/file1 && \
ln -s /home/xenon/xenon_test/links/file0 /home/xenon/xenon_test/links/link0 && \
ln -s /home/xenon/xenon_test/links/file1 /home/xenon/xenon_test/links/link1 && \
ln -s /home/xenon/xenon_test/links/file2 /home/xenon/xenon_test/links/link2 && \
ln -s /home/xenon/xenon_test/links/link0 /home/xenon/xenon_test/links/link3 && \
ln -s /home/xenon/xenon_test/links/link2 /home/xenon/xenon_test/links/link4 && \
ln -s /home/xenon/xenon_test/links/link6 /home/xenon/xenon_test/links/link5 && \
ln -s /home/xenon/xenon_test/links/link5 /home/xenon/xenon_test/links/link6
RUN chown -R xenon.xenon .

# Upgrade OS
RUN apt-get update && apt-get upgrade -y -o Dpkg::Options::="--force-confold" && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

CMD ["/sbin/my_init"]