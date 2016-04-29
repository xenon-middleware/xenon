#!/bin/bash

set -e

# Set the ulimits for this container. Must be run with the --privileged option
ulimit -l unlimited
ulimit -s unlimited

# Configure torque with current hostname
hostname > /var/spool/torque/server_name
echo $(hostname) np=1 > /var/spool/torque/server_priv/nodes
echo '$pdbsserver' $(hostname) > /var/spool/torque/mom_priv/config

# Run whatever the user wants to
exec "$@"
