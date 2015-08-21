#!/bin/sh

printenv | grep ^CA_

/usr/bin/grid-ca-create -noint -subject "$CA_SUBJECT" \
-email $CA_EMAIL -days $CA_DAYS -pass $CA_PASSPHRASE -nobuild
echo 1 | grid-default-ca
echo 1 | grid-ca-package -d -r
chown -R $MYUID /var/lib/globus /etc/grid-security
