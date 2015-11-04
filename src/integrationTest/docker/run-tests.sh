#!/bin/sh

# Testing if UID was empty (not allowed)
if [ "$MYUID" = "" ]; then
	echo "\$MYUID is not set"
	exit 1
elif [ "$MYUID" = "$(id xenon -u)" ]; then
	echo "Uid of xenon user is already same as MYUID=$MYUID, not changing uid"
else
	echo "Changing uid of xenon user to MYUID=$MYUID"
	usermod -u $MYUID xenon
	chown -R $MYUID /home/xenon
fi

# ssh in prepareIntegrationTest in build.gradle adds ecdsa key which it cant read
# fill known hosts so prepareIntegrationTest
ssh-keyscan -t rsa xenon-ssh >> /home/xenon/.ssh/known_hosts
chown xenon.xenon /home/xenon/.ssh/known_hosts

if [ "$BOOT_DELAY" != "" ]; then
    echo 'Waiting' $BOOT_DELAY 'seconds for services to boot-up...'
    sleep $BOOT_DELAY
    echo 'Grid engine should have 2 exec hosts:'
    setuser xenon ssh-keyscan -t rsa xenon-gridengine >> /home/xenon/.ssh/known_hosts
    chown xenon.xenon /home/xenon/.ssh/known_hosts
    setuser xenon ssh xenon-gridengine qhost
fi

setuser xenon "$@"
