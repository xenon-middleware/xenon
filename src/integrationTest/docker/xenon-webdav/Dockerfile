# Docker container with webdav server, to run the xenon webdav adaptor integration tests against.
#
# Build with:
#
#     docker build -t nlesc/xenon-webdav .
#
# Run with:
#
#     docker run -d --name=xenon-webdav -h xenon-webdav nlesc/xenon-webdav
#
# Get containers ip with
#
#     XENON_WEBDAV_LOCATION=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" xenon-webdav)
#
# Login by
#
#     lwebdav webdav://xenon:javagat@$XENON_WEBDAV_LOCATION
#     # or for anonymous access
#     lwebdav webdav://$XENON_WEBDAV_LOCATION
#
# Run xenon test with
#
# test.webdav.location=$XENON_WEBDAV_LOCATION
# test.webdav.user=xenon
# test.webdav.password=javagat
FROM nlesc/xenon-alpine-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apk add --update apache2-webdav && rm -rf /var/cache/apk/* || /bin/true
ADD httpd.conf /etc/apache2/httpd.conf

EXPOSE 80
CMD ["/usr/sbin/httpd", "-X"]