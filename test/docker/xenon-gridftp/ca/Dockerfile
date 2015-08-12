FROM phusion/baseimage:0.9.17
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y globus-simple-ca globus-gsi-cert-utils-progs fakeroot dpkg-dev cdbs openssh-client && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# grid-ca-create will be run as non-root, edit script so it doesnt die on `id -un`
RUN perl -pi -e 's/\$\(id -un\)/nobody/' /usr/bin/grid-ca-create

WORKDIR /var/lib/globus

CMD /bin/bash