#
# docker build -t nlesc/xenon-slurm:2.6.5 .
#
FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y slurm-llnl && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
create-munge-key && \
mkdir /etc/service/munged /etc/service/slurmd /etc/service/slurmctld && \
mkdir /var/run/munge

ADD munged.sh /etc/service/munged/run
ADD slurmctld.sh /etc/service/slurmctld/run
ADD slurmd.sh /etc/service/slurmd/run
RUN chmod +x /etc/service/*/run

ADD slurm.conf /etc/slurm-llnl/slurm.conf

CMD ["/sbin/my_init"]

