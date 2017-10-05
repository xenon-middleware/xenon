1. Make the GridFTP adaptor stable
1. Write documentation on how to start writing a new adaptor
1. Add _JavaDoc for Xenon developers_ covering the private/internal methods of the adaptors
    - file adaptors: local, ssh, ftp, sftp, WebDAV
    - job adaptors: local, ssh, SLURM, Torque, GridEngine
1. Make it easier to inspect at runtime which adaptors are available and what properties they support.
1. Add more adaptors, for example for:
    - [S3](https://aws.amazon.com/s3/)
    - SWIFT
    - [Hadoop](https://en.wikipedia.org/wiki/Apache_Hadoop) HDFS, YARN
    - Azure-Batch (corporate [site](https://azure.microsoft.com/en-us/services/batch/), [docs](https://docs.microsoft.com/en-us/azure/batch/))
    - [Amazon-Batch](https://aws.amazon.com/batch/)
1. Add interface to clouds (AWS, Azure, ...)


Planned extensions include: 

- Swift
- HDFS (almost done)
- YARN
- GridFTP
- glite
- Azure-Batch
- Amazon-Batch
