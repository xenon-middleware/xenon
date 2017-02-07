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
# Login with username `xenon` and password `javagat`.
# Test with following commands:
#
#     # for basic authenticated read access
#     cadaver http://$XENON_WEBDAV_LOCATION/~xenon
#     # or for basic authenticated write access
#     cadaver http://$XENON_WEBDAV_LOCATION/~xenon/uploads
#     # or for anonymous write access
#     cadaver http://$XENON_WEBDAV_LOCATION/uploads
#     # or for anonymous read access
#     cadaver http://$XENON_WEBDAV_LOCATION/downloads
#
# Run xenon test with
#
# test.webdav.location=$XENON_WEBDAV_LOCATION
# test.webdav.user=xenon
# test.webdav.password=javagat
FROM nlesc/xenon-alpine-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apk add --update apache2-webdav apache2-utils && rm -rf /var/cache/apk/* && \
mkdir /var/www/var && chown apache.apache /var/www/var && \
mkdir /home/xenon/uploads && chown apache.apache /home/xenon/uploads && \
mkdir /var/www/uploads && cp -r /home/xenon/xenon_test /var/www/uploads && chown apache.apache /var/www/uploads && \
mkdir /var/www/localhost/htdocs/downloads && cp -r /home/xenon/xenon_test /var/www/localhost/htdocs/downloads/ && \
htpasswd -bc /var/www/users.htpasswd xenon javagat
ADD httpd.conf /etc/apache2/httpd.conf

EXPOSE 80
CMD ["/usr/sbin/httpd", "-DFOREGROUND"]
