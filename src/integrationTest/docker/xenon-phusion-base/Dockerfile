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
ADD insecure-ssh-keys /home/xenon/.ssh
RUN chmod 600 /home/xenon/.ssh/*

# Create symlinks
ADD create_symlinks /tmp/
RUN /tmp/create_symlinks && rm -f /tmp/create_symlinks && chown -R xenon.xenon .

# Upgrade OS
RUN apt-get update && apt-get upgrade -y -o Dpkg::Options::="--force-confold" && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

CMD ["/sbin/my_init"]
