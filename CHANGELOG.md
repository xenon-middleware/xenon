Xenon 1.2.1
-----------

This is release 1.2.1 of Xenon. 

Notable changes compared to v1.2.0:
-----------------------------------

- fixed nasty inconsistency in adaptor implementations of waiting for jobs to start or finish.

Notable changes between v1.2.0 and v1.1.0:
------------------------------------------

- added support for WebDAV file access.
- added OSX testing in Travis
- added support for Slurm version 15.08.6
- fixed several bugs related to Windows local file system semantics
- many small bugfixes, additional tests, etc. 


Notable changes between v1.1.0 and v1.0.0:
------------------------------------------
 
- added support for FTP file access.
- added support for Torque resource manager.
- added support for Slurm versions 2.6.0, 14.03.0 and 14.11.9-Bull.1.0.
- added option to specify resources in sge adaptor.
- added support for SSH agent and agent proxies
- added a -lot- of unit and integration tests
- javadoc is java 8 compliant.
- the adaptor documentation is now part of the javadoc.
- Xenon releases are now available in jCenter.
- switched from and ant to gradle based build system, this is reflected in the directory structure 
- split unit and integration tests
- added docker support for integration tests
- now using travis-ci for continous integration 
- now using PMD and codacy for code quality
- now using codecov for unit and integration test coverage
- moved examples and tutorial to a separate repository https://github.com/NLeSC/Xenon-examples


What's missing:
---------------
	
The GridFTP adaptor is not considered stable yet. It is not part of this release.

There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.

It should be made easier to inspect at runtime which adaptors are available and what properties they support.

We can always use more adaptors, e.g, for S3, SWIFT, HDFS, YARN, Azure-Batch, etc. These are planned for 1.3 or later.

We can always use more interfaces, e.g. for clouds. This is planned for 2.0.



