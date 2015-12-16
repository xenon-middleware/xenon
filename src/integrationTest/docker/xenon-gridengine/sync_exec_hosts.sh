#!/bin/sh

# Wait for exec host file to be written completely
sleep 1

# master also runs a execd
EXEC_HOSTS=$HOSTNAME
# Add exec hosts to /etc/hosts
for exec_host in `ls /etc/gridengine/files/exec_hosts/`
do
    echo $(cat /etc/gridengine/files/exec_hosts/$exec_host) $exec_host >> /etc/hosts
    EXEC_HOSTS="$EXEC_HOSTS $exec_host"
done
# Add exec hosts to allhosts host group
/bin/echo -e "group_name @allhosts\nhostlist $EXEC_HOSTS" > /etc/gridengine/files/host_groups/allhosts

# Wait for exec host file to be resolvable
sleep 1

qconf -Mhgrp /etc/gridengine/files/host_groups/allhosts
