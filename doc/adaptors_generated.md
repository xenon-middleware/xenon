Appendix A: Adaptor Documentation
---------------------------------

This section contains the adaptor documentation which is generated from the information provided by the adaptors themselves.

Xenon currently supports 4 adaptors: local, ssh, gridengine, slurm.

Adaptor: local
--------

The local adaptor implements all functionality with  standard java classes such as java.lang.Process and java.nio.file.Files.

#### Supported schemes: ####
local, file

#### Supported locations: ####
(null), (empty string), /

#### Supported properties: ####


__`xenon.adaptors.local.queue.pollingDelay`__

The polling delay for monitoring running jobs (in milliseconds).

- Expected type: INTEGER

- Default value: 1000

- Valid for: [XENON]


__`xenon.adaptors.local.queue.multi.maxConcurrentJobs`__

The maximum number of concurrent jobs in the multiq..

- Expected type: INTEGER

- Default value: 4

- Valid for: [XENON]



Adaptor: ssh
--------

The SSH adaptor implements all functionality with remove ssh servers.

#### Supported schemes: ####
ssh, sftp

#### Supported locations: ####
[user@]host[:port]

#### Supported properties: ####


__`xenon.adaptors.ssh.autoAddHostKey`__

Automatically add unknown host keys to known_hosts.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [SCHEDULER, FILESYSTEM]


__`xenon.adaptors.ssh.strictHostKeyChecking`__

Enable strict host key checking.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [SCHEDULER, FILESYSTEM]


__`xenon.adaptors.ssh.loadKnownHosts`__

Load the standard known_hosts file.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [XENON]


__`xenon.adaptors.ssh.queue.pollingDelay`__

The polling delay for monitoring running jobs (in milliseconds).

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]


__`xenon.adaptors.ssh.queue.multi.maxConcurrentJobs`__

The maximum number of concurrent jobs in the multiq..

- Expected type: INTEGER

- Default value: 4

- Valid for: [SCHEDULER]


__`xenon.adaptors.ssh.gateway`__

The gateway machine used to create an SSH tunnel to the target.

- Expected type: STRING

- Default value: null

- Valid for: [SCHEDULER, FILESYSTEM]



Adaptor: gridengine
--------

The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

#### Supported schemes: ####
ge, sge

#### Supported locations: ####
(locations supported by local), (locations supported by ssh)

#### Supported properties: ####


__`xenon.adaptors.gridengine.ignore.version`__

Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


__`xenon.adaptors.gridengine.accounting.grace.time`__

Number of milliseconds a job is allowed to take going from the queue to the qacct output.

- Expected type: LONG

- Default value: 60000

- Valid for: [SCHEDULER]


__`xenon.adaptors.gridengine.poll.delay`__

Number of milliseconds between polling the status of a job.

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]



Adaptor: slurm
--------

The Slurm Adaptor submits jobs to a Slurm scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

#### Supported schemes: ####
slurm

#### Supported locations: ####
(locations supported by local), (locations supported by ssh)

#### Supported properties: ####


__`xenon.adaptors.slurm.ignore.version`__

Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


__`xenon.adaptors.slurm.disable.accounting.usage`__

Do not used accounting info of slurm, even when available. Mostly for testing purposes

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


__`xenon.adaptors.slurm.poll.delay`__

Number of milliseconds between polling the status of a job.

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]



