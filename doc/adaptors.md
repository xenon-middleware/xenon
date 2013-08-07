Octopus Adaptor Documentation
=============================

This document contains the adaptor documentation. This documentation is generated from the information provided by the adaptors themselves.

Octopus currently supports 4 adaptors: local, ssh, gridengine, slurm.

Adaptor: __local__
--------

The local adaptor implements all functionality with  standard java classes such as java.lang.Process and java.nio.file.Files.

This adaptor supports the following schemes: local, file

Supported properties: 

 - name: octopus.adaptors.local.queue.pollingDelay
 - type: INTEGER
 - levels: [OCTOPUS]
 - default: 1000
 - description: The polling delay for monitoring running jobs (in milliseconds).

 - name: octopus.adaptors.local.queue.multi.maxConcurrentJobs
 - type: INTEGER
 - levels: [OCTOPUS]
 - default: 4
 - description: The maximum number of concurrent jobs in the multiq..


Adaptor: __ssh__
--------

The SSH adaptor implements all functionality with remove ssh servers.

This adaptor supports the following schemes: ssh, sftp

Supported properties: 

 - name: octopus.adaptors.ssh.autoAddHostKey
 - type: BOOLEAN
 - levels: [SCHEDULER, FILESYSTEM]
 - default: true
 - description: Automatically add unknown host keys to known_hosts.

 - name: octopus.adaptors.ssh.strictHostKeyChecking
 - type: BOOLEAN
 - levels: [SCHEDULER, FILESYSTEM]
 - default: true
 - description: Enable strict host key checking.

 - name: octopus.adaptors.ssh.loadKnownHosts
 - type: BOOLEAN
 - levels: [OCTOPUS]
 - default: true
 - description: Load the standard known_hosts file.

 - name: octopus.adaptors.ssh.queue.pollingDelay
 - type: LONG
 - levels: [SCHEDULER]
 - default: 1000
 - description: The polling delay for monitoring running jobs (in milliseconds).

 - name: octopus.adaptors.ssh.queue.multi.maxConcurrentJobs
 - type: INTEGER
 - levels: [SCHEDULER]
 - default: 4
 - description: The maximum number of concurrent jobs in the multiq..

 - name: octopus.adaptors.ssh.gateway
 - type: STRING
 - levels: [SCHEDULER, FILESYSTEM]
 - default: null
 - description: The gateway machine used to create an SSH tunnel to the target.


Adaptor: __gridengine__
--------

The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

This adaptor supports the following schemes: ge, sge

Supported properties: 

 - name: octopus.adaptors.gridengine.ignore.version
 - type: BOOLEAN
 - levels: [SCHEDULER]
 - default: false
 - description: Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

 - name: octopus.adaptors.gridengine.accounting.grace.time
 - type: LONG
 - levels: [SCHEDULER]
 - default: 60000
 - description: Number of milliseconds a job is allowed to take going from the queue to the qacct output.

 - name: octopus.adaptors.gridengine.poll.delay
 - type: LONG
 - levels: [SCHEDULER]
 - default: 1000
 - description: Number of milliseconds between polling the status of a job.


Adaptor: __slurm__
--------

The Slurm Adaptor submits jobs to a Slurm scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

This adaptor supports the following schemes: slurm

Supported properties: 

 - name: octopus.adaptors.slurm.ignore.version
 - type: BOOLEAN
 - levels: [SCHEDULER]
 - default: false
 - description: Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

 - name: octopus.adaptors.slurm.poll.delay
 - type: LONG
 - levels: [SCHEDULER]
 - default: 1000
 - description: Number of milliseconds between polling the status of a job.


