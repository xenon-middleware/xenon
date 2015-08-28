#!/bin/sh

if [ -f /etc/ca-files/hosts/$(hostname -f)/hostcert.pem ] ; then
    ln -f -s /etc/ca-files/hosts/$(hostname -f)/hostcert.pem /etc/grid-security
    ln -f -s /etc/ca-files/hosts/$(hostname -f)/hostkey.pem /etc/grid-security
else
    logger -s "No certificates found for host, starting grid ftp server without"
fi

/usr/sbin/globus-gridftp-server -no-detach -c /etc/gridftp.conf -banner "Welcome to Xenon test gridftp server"
