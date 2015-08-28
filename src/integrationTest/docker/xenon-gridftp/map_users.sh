#!/bin/sh

for u in `ls /etc/ca-files/users`
do
    /usr/bin/map_user.sh $u
done
