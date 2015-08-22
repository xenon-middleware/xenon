FROM phusion/baseimage:0.9.17
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y globus-simple-ca globus-gsi-cert-utils-progs fakeroot dpkg-dev cdbs openssh-client && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# grid-ca-create will be run as non-root, edit script so it doesnt die on `id -un`
RUN perl -pi -e 's/\$\(id -un\)/nobody/' /usr/bin/grid-ca-create

ENV CA_SUBJECT CN=eScience Center Simple CA, O=eScienceCenter
ENV CA_EMAIL nobody@example.com
ENV CA_DAYS 3650
ENV CA_PASSPHRASE mycainsecurepassword
ENV CA_HOST1 gridftp1.xenontest.nlesc.nl
ENV CA_HOST2 gridftp2.xenontest.nlesc.nl
ENV CA_USER1 xenon

ADD new-ca.sh /usr/bin/
ADD new-host.sh /usr/bin/
ADD new-user.sh /usr/bin/
ADD make-all.sh /usr/bin/

VOLUME /var/lib/globus

WORKDIR /var/lib/globus

CMD /usr/bin/make-all.sh
