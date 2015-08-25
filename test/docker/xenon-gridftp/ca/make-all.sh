#!/bin/sh

/usr/bin/new-ca.sh
/usr/bin/new-host.sh $CA_HOST1
/usr/bin/new-host.sh $CA_HOST2
/usr/bin/new-user.sh $CA_USER1 $CA_USER1_PASSPHRASE
