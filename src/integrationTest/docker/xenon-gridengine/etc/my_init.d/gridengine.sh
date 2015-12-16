#!/bin/sh

# get stored SGE hostname
export SGE_HOST=$(cat /var/lib/gridengine/default/common/act_qmaster)

# replace SGE_HOST text in files
grep -Rl "$SGE_HOST" /var/lib/gridengine | xargs sed -i "s/$SGE_HOST/$HOSTNAME/g"

# SGE can not start in the foreground so use the rc scripts
/etc/init.d/gridengine-master restart

# master isn't immediately running so wait a bit, before adjusting config
sleep 1
sync_exec_hosts.sh
qconf -ds $SGE_HOST
qconf -as $HOSTNAME

/etc/init.d/gridengine-exec restart
