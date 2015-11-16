# Build with:
#
# docker build -t nlesc/xenon-slurm:2.6.5 .
#
# Run with:
#
# docker run -d --name=xenon-slurm nlesc/xenon-slurm
#
# Test with:
#
# ssh xenon@<ip of xenon-slurm container>
#

FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y slurm-llnl && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ADD munge.key /etc/munge/munge.key
ADD service /etc/service
# Key was generated with openssl genrsa -out slurm.key 1024 && openssl rsa -in slurm.key -pubout -out slurm.cert
ADD etc /etc/slurm-llnl

RUN mkdir /var/run/munge && \
mkdir -p /var/spool/slurmctld/state && \
mkdir -p /var/spool/slurmd.node-0 && \
mkdir -p /var/spool/slurmd.node-1 && \
mkdir -p /var/spool/slurmd.node-2 && \
mkdir -p /var/spool/slurmd.node-3 && \
mkdir -p /var/spool/slurmd.node-4 && \
chmod 600 /etc/munge/munge.key && \
chown root.root /var/lib/munge /etc/munge && \
chown slurm /var/spool/slurmctld/state && \
chown slurm /etc/slurm-llnl/slurm.key

CMD ["/sbin/my_init"]
