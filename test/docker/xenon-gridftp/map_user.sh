#!/bin/sh

cd /etc/ca-files/users/$1
MYDN=$(openssl x509 -noout -in usercert.pem -subject |awk '{print $2}')
grid-mapfile-add-entry -dn "$MYDN" -ln $1
