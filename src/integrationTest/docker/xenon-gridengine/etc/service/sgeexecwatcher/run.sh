#!/bin/sh
while inotifywait -r -e create -e delete -e modify /etc/gridengine/files/exec_hosts; do
    sync_exec_hosts.sh
done
