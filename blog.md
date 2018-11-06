**Run your remote programs more easily through abstraction with Xenon**

At the Netherlands eScience Center, we come across many projects that make use of remote resources, both for storage and for compute. 

A typical distributed computing application looks like this: First, you need to get your data and your software to the remote resource. SSH and SFTP are typical ways for users to get the files where they need to be. Second, you need to ask the remote system to execute your program. Because virtually all remote systems are shared between many users, a _scheduler_ such as Slurm, PBS, SGE, or Torque is required to do the resource allocation. While the program is being executed, it may need access to for example an S3 bucket or an Hadoop File System, for reading its input and for writing its output.

- Within a given project, you may have to update your code if the service provider decides to decommission say, Slurm 14, in favor of Slurm 17.
- Within a given project, you may want to move to a different service provider if they can offer for example better pricing or better performance.
- From a Research Software Engineer's perspective, switching between projects that overlap in time, but which use different schedulers, can be difficult. Trying to remember the intricacies of how one version is different from another is difficult and makes it easy to introduce bugs.
- Easily switch between local development and remote development.

(Can we show how this was easy for us thanks to Xenon?)

What technology is available around Xenon?

<img src="https://raw.githubusercontent.com/NLeSC/Xenon/master/docs/images/readme-xenon-api.svg.png" width="800px">

- gRPC 
- pyXenon
- xenon-cli

(call to action: get started with the RSE2017 virtual machine? Could use an update particularly re pyXenon)