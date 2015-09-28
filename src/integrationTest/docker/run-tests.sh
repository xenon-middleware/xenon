#!/bin/sh

if [ "x$MYUID" -ne "x$(id xenon -u)" ]
then
echo "Changing uid of xenon user to MYUID=$MYUID"
usermod -u $MYUID xenon
chown -R $MYUID /home/xenon
else
echo "Uid of xenon user is already same as MYUID=$MYUID, not changing uid"
fi

# ssh in prepareIntegrationTest in build.gradle adds ecdsa key which it cant read
ssh-keyscan -t rsa xenon-ssh >> /home/xenon/.ssh/known_hosts
chown xenon.xenon /home/xenon/.ssh/known_hosts

setuser xenon "$@"
