# Used https://github.com/gawbul/docker-sge as inspiration/source
#
# Build with
#
#     docker build -t nlesc/xenon-gridengine .
#
# Run with
#
#     docker run -d --name=xenon-gridengine nlesc/xenon-gridengine
#
FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN echo "Package: xserver-xorg*\nPin: release *\nPin-Priority: -1" >> /etc/apt/preferences && \
echo "Package: unity*\nPin: release *\nPin-Priority: -1" >> /etc/apt/preferences && \
echo "Package: gnome*\nPin: release *\nPin-Priority: -1" >> /etc/apt/preferences && \
echo "gridengine-master shared/gridenginemaster string $HOSTNAME" | debconf-set-selections && \
echo "postfix postfix/main_mailer_type  string 'No configuration'" | debconf-set-selections && \
apt-get update && \
DEBIAN_FRONTEND=noninteractive apt-get install -y gridengine-master gridengine-exec gridengine-client inotify-tools && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# expose ports
EXPOSE 6444
EXPOSE 6445
EXPOSE 6446

# add init scripts for master and exec deamons
RUN mkdir -p /etc/my_init.d
ADD etc/my_init.d/gridengine.sh /etc/my_init.d/gridengine.sh

# Add script to watch for exec container registrations
RUN mkdir -p /etc/service/sgeexecwatcher
ADD sync_exec_hosts.sh /usr/bin/sync_exec_hosts.sh
ADD etc/service/sgeexecwatcher/run.sh /etc/service/sgeexecwatcher/run

# config
ADD etc/gridengine /etc/gridengine/files
WORKDIR /etc/gridengine/files
RUN mkdir host_groups exec_hosts && \
/bin/echo -e "group_name @allhosts\nhostlist $HOSTNAME" > host_groups/allhosts && \
# Ignore domains
perl -pi -e 's/false/true/' /var/lib/gridengine/default/common/bootstrap && \
/etc/my_init.d/gridengine.sh && \
# submit host
qconf -as $HOSTNAME && \
# hosts for queues
/bin/echo -e "group_name @allhosts\nhostlist $HOSTNAME" > host_groups/allhosts && \
qconf -Ahgrp host_groups/allhosts && \
qconf -Ap parallel_environments/bi && \
qconf -Ap parallel_environments/fillup && \
qconf -Ap parallel_environments/round && \
qconf -Ap parallel_environments/smp && \
qconf -Aq queues/default && \
qconf -Aq queues/slow && \
qconf -Msconf scheduler && \
killall sge_qmaster sge_execd

CMD ["/sbin/my_init"]

# Shares for gridengine-exec containers
VOLUME ["/var/lib/gridengine", "/etc/gridengine/files", "/home"]
