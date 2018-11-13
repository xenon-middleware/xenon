**Run your distributed computing tasks on a variety of systems through  abstraction with Xenon**

At the Netherlands eScience Center, we come across many projects that make use of remote resources, both for storage and for compute. 

<!-- 
examples?
-->

A typical distributed computing application looks like this: First, you need to get your data and your software to the remote resource. Protocols such as SSH and SFTP are typical ways for users to get the files where they need to be. Second, you need to ask the remote system to execute your program. Because virtually all remote systems are shared between many users, a _scheduler_ such as Slurm, PBS, SGE, or Torque is required to do the resource allocation. While the program is being executed, it may need access to a file system, for instance via S3 or WebDAV, in order to read its input and for writing its output.

When developing applications that use remote resources, dealing with all these different protocols and schedulers can be quite a nuisance. As a result, people usually choose to implement only the necessary code for one protocol and for one scheduler. That way, they can avoid cluttering their code with many (potentially nested) conditionals such as 
```
if (scheduler == 'slurm') {
    # slurm code
    if (protocol == 'sftp') {
        # sftp code 
    }
    ...other protocols...
}
...other schedulers...
```

While this simplifies matters for the developer, it comes at the cost of reduced flexibility with respect to the infrastructure that the distributed application can make use of. Such reduced flexibility can become a problem when you want to migrate to a different remote system, for example because it is cheaper, or it has better availability, or because you want to use (large) data that is stored there. 

To avoid this reduced flexibility, we developed a library called Xenon that hides the specifics of the remote system behind a unified interface. That is, Xenon is an _abstraction layer_. With Xenon, a developer only has to implement one thing (for example a ``copy`` or a ``run`` operation) on the application side, and Xenon takes care of making the translation to whichever scheduler and file system access protocol is available on the remote system.

The easiest way to start using Xenon is via its command line interface. In our experience, the command line interface is sufficient to accomplish most distributed computing projects. 

Assuming you have a working [Conda](https://conda.io/docs/) distribution (if not, go [here](https://conda.io/docs/user-guide/install/index.html#)), install the ``xenon-cli`` package from the ``nlesc`` channel, as follows:

```bash
conda install --channel nlesc xenon-cli
```

Let's say we have access to two systems, one using Slurm as a scheduler, and one using SGE. We have a username/password combination for each one, and of course each system has its own location. For brevity, let's store those data in two Bash variables, as follows:

```
CREDENTIALS_SLURM_SYSTEM='--location localhost:10022 --username xenon --password javagat'
CREDENTIALS_SGE_SYSTEM='--location localhost:10022 --username xenon --password javagat'
```

For illustration purposes, let's emulate doing some actual work by ``sleep``ing for 60 seconds. For the Slurm cluster, that would amount to the following command:

```
xenon scheduler slurm $CREDENTIALS_SLURM_SYSTEM submit bash sleep 60
```

Migrating this task to the SGE cluster is a simple as replacing ``slurm`` by ``sge``, as follows:
```
xenon scheduler sge $CREDENTIALS_SGE_SYSTEM submit bash sleep 60
```

As you can see, migrating from one system to another becomes trivial thanks to the abstraction that Xenon provides.

Xenon is written in Java and has a gRPC extension, which means that you can use it from a range of languages such as C++, C#, Dart, Go, Java, Node.js, Objective-C, PHP, Python, and Ruby. [This tutorial](https://xenonrse2017.readthedocs.io/en/latest/) shows ``xenon-cli`` examples side by side with their Java and Python equivalents.

<!-- 
call to action
 -->
