# Docker container with ftp server, to run the xenon ftp adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-ftp .
#
# Run with:
#
#     docker run -d --name=xenon-ftp -h xenon-ftp nlesc/xenon-ftp
#
# Get containers ip with
#
#     XENON_FTP_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-ftp)
#
# Login by
#
#     lftp ftp://xenon:javagat@$XENON_FTP_LOCATION
#     # or for anonymous access
#     lftp ftp://$XENON_FTP_LOCATION
#
# Run xenon test with
#
# test.ftp.location=$XENON_FTP_LOCATION
# test.ftp.user=xenon
# test.ftp.password=javagat

FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y vsftpd && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
mkdir /etc/service/vsftpd && echo "#!/bin/sh" > /etc/service/vsftpd/run && \
echo "/usr/sbin/vsftpd /etc/vsftpd.conf" >> /etc/service/vsftpd/run && \
chmod +x /etc/service/vsftpd/run && mkdir -p /var/run/vsftpd/empty

ADD vsftpd.conf /etc/vsftpd.conf

EXPOSE 21
