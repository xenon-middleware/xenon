#!/bin/sh

mkdir /var/lib/globus/hosts
cd /var/lib/globus/hosts
mkdir $1 && cd $1
grid-cert-request -host $1 -nopassphrase -dir .
grid-ca-sign -passin pass:$CA_PASSPHRASE -days $CA_DAYS -in hostcert_request.pem -out hostcert.pem
chown -R $MYUID /var/lib/globus /etc/grid-security
