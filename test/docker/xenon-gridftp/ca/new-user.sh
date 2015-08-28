#!/bin/sh

mkdir /var/lib/globus/users
cd /var/lib/globus/users
mkdir $1
cd $1
grid-cert-request -cn $1 -nopassphrase -dir .
grid-ca-sign -passin pass:$CA_PASSPHRASE -days $CA_DAYS -in usercert_request.pem -out usercert.pem
# Convert to PKCS#1 format so jglobus can read key
openssl rsa -in userkey.pem -out userkey.rsa.pem
chmod go-r userkey.rsa.pem
ssh-keygen -p -P "" -N $2 -f userkey.rsa.pem
chown -R $MYUID /var/lib/globus /etc/grid-security
