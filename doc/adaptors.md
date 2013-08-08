Octopus Adaptor Documentation
=============================

This document contains the adaptor documentation. This documentation is generated from the information provided by the adaptors themselves.

Octopus currently supports 4 adaptors: local, ssh, gridengine, slurm.

Adaptor: local
--------

The local adaptor implements all functionality with  standard java classes such as java.lang.Process and java.nio.file.Files.

### Supported schemes: ###
local, file

### Supported properties: ###


### `octopus.adaptors.local.queue.pollingDelay` ###

The polling delay for monitoring running jobs (in milliseconds).

- Expected type: INTEGER

- Default value: 1000

- Valid for: [OCTOPUS]


### `octopus.adaptors.local.queue.multi.maxConcurrentJobs` ###

The maximum number of concurrent jobs in the multiq..

- Expected type: INTEGER

- Default value: 4

- Valid for: [OCTOPUS]



Adaptor: ssh
--------

The SSH adaptor implements all functionality with remove ssh servers.

### Supported schemes: ###
ssh, sftp

### Supported properties: ###


### `octopus.adaptors.ssh.autoAddHostKey` ###

Automatically add unknown host keys to known_hosts.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [FILESYSTEM, SCHEDULER]


### `octopus.adaptors.ssh.strictHostKeyChecking` ###

Enable strict host key checking.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [FILESYSTEM, SCHEDULER]


### `octopus.adaptors.ssh.loadKnownHosts` ###

Load the standard known_hosts file.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [OCTOPUS]


### `octopus.adaptors.ssh.queue.pollingDelay` ###

The polling delay for monitoring running jobs (in milliseconds).

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]


### `octopus.adaptors.ssh.queue.multi.maxConcurrentJobs` ###

The maximum number of concurrent jobs in the multiq..

- Expected type: INTEGER

- Default value: 4

- Valid for: [SCHEDULER]


### `octopus.adaptors.ssh.gateway` ###

The gateway machine used to create an SSH tunnel to the target.

- Expected type: STRING

- Default value: null

- Valid for: [FILESYSTEM, SCHEDULER]



Adaptor: gridengine
--------

The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

### Supported schemes: ###
ge, sge

### Supported properties: ###


### `octopus.adaptors.gridengine.ignore.version` ###

Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


### `octopus.adaptors.gridengine.accounting.grace.time` ###

Number of milliseconds a job is allowed to take going from the queue to the qacct output.

- Expected type: LONG

- Default value: 60000

- Valid for: [SCHEDULER]


### `octopus.adaptors.gridengine.poll.delay` ###

Number of milliseconds between polling the status of a job.

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]



Adaptor: slurm
--------

The Slurm Adaptor submits jobs to a Slurm scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

### Supported schemes: ###
slurm

### Supported properties: ###


### `octopus.adaptors.slurm.ignore.version` ###

Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


### `octopus.adaptors.slurm.disable.accounting.usage` ###

Do not used accounting info of slurm, even when available. Mostly for testing purposes

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


### `octopus.adaptors.slurm.poll.delay` ###

Number of milliseconds between polling the status of a job.

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]



