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
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
mkdir /var/run/munge

ADD munge.key /etc/munge/munge.key
ADD service /etc/service
ADD slurm.conf /etc/slurm-llnl/slurm.conf

CMD ["/sbin/my_init"]
