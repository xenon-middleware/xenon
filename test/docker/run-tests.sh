#!/bin/sh

if [ $MYUID -ne $(id xenon -u) ]
then
echo "Changing uid of xenon user to MYUID=$MYUID"
usermod -u $MYUID xenon
chown -R $MYUID /home/xenon
else
echo "Uid of xenon user is already same as MYUID=$MYUID, not changing uid"
fi

setuser xenon ant -lib /usr/share/java test