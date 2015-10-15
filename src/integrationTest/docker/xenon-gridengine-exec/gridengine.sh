#!/bin/sh

# wait for sge master come online, otherwise execd is not registered at qmaster
sleep 10

# Remove master linked hostname and optional alias, to fix host resolving for sge
export QMAST=$(cat /var/lib/gridengine/default/common/act_qmaster)
# /etc/hosts is a busy file so cant be moved, use update mode (+<) to rewrite file
perl -e 'open(FH, "+</etc/hosts");@ARRAY=<FH>;for $i (@ARRAY) { $i =~ s/^(.*)\s+.*?\s+$ENV{"QMAST"}.*$/$1 $ENV{"QMAST"}/;}; seek(FH,0,0);print FH @ARRAY;truncate(FH,tell(FH));close(FH)'

# Register exec host at master
# the master will notice the file change and update the configuration
MYIP=$(ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' | cut -d: -f2 | awk '{ print $1}')
echo $MYIP > /etc/gridengine/files/exec_hosts/$HOSTNAME

/etc/init.d/gridengine-exec restart
