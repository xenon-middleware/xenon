Xenon 1.1.0
------------------

This is release 1.1.0 of Xenon. 

Notable changes compared to v1.0.0:
------------------------------------------------------
 
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

Bugfixes:
--------------

Many bugfixes in adaptors and tests.

What's missing:
-----------------------

The GridFTP and WebDAV adaptors are not considered stable yet. They are not part of this release.

There is no adaptor writing documentation at the moment, nor is the Javadoc complete for the internals methods of the adaptor implementations.

We can always use more adaptors.

We can always use more interfaces, e.g. for bandwidth-on-demand or clouds.


