# Used https://github.com/gawbul/docker-sge as inspiration/source
#
# Build with
#
#     docker build -t nlesc/xenon-gridengine-exec .
#
# Run with
#
#     docker run -d --name=xenon-gridengine nlesc/xenon-gridengine
#     docker run -d --link=xenon-gridengine --volumes-from=xenon-gridengine nlesc/xenon-gridengine-exec
#
# Test it
#
# 1. Login to xenon-gridengine
# 2. Run `qstat -f`, there should be multiple hosts
# 3. Run `qsub`, job should run over multiple hosts
FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN echo "Package: xserver-xorg*\nPin: release *\nPin-Priority: -1" >> /etc/apt/preferences && \
echo "Package: unity*\nPin: release *\nPin-Priority: -1" >> /etc/apt/preferences && \
echo "Package: gnome*\nPin: release *\nPin-Priority: -1" >> /etc/apt/preferences && \
echo "gridengine-master shared/gridenginemaster string $HOSTNAME" | debconf-set-selections && \
echo "postfix postfix/main_mailer_type  string 'No configuration'" | debconf-set-selections && \
apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y gridengine-exec && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# expose ports
EXPOSE 6445

# add init scripts for exec deamon
RUN mkdir -p /etc/my_init.d
ADD gridengine.sh /etc/my_init.d/gridengine.sh

CMD ["/sbin/my_init"]
