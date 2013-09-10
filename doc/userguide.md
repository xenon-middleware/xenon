![logo](images/NLeSC_Octopus_logo.png "Octopus Logo")

Octopus
=======

Copyright 2013 The Netherlands eScience Center

Author: Jason Maassen (<J.Maassen@esciencecenter.nl>)

Version: Userguide v1.0, Octopus v1.0

Last modified: 10 September 2013


Copyrights & Disclaimers
------------------------

Octopus is copyrighted by the Netherlands eScience Center and releases under 
the Apache License, Version 2.0.

See the "LICENSE" and "NOTICE" files in the octopus distribution for more 
information. 

For more information on the Netherlands eScience Center see:

<http://www.esciencecenter.nl> 

The octopus project web site can be found at:

<https://github.com/NLeSC/octopus>.


Third party libraries
---------------------

This product includes the SLF4J library, which is Copyright (c) 2004-2013
QOS.ch See "notices/LICENSE.slf4j.txt" for the licence information of
the SLF4J library.

This product includes the JSch library, which is Copyright (c) 2002-2012
Atsuhiko Yamanaka, JCraft,Inc. See "notices/LICENSE.jsch.txt" for the
licence information of the JSch library.

This product includes the Logback library, which is Copyright (c)
1999-2012, QOS.ch. See "notices/LICENSE.logback.txt" for the licence
information of the Logback library.

This product includes the JaCoCo library, which is Copyright
(c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors. See
"notices/LICENSE.jacoco.txt" for the licence information of the JaCoCo
library.

This project includes the JUnit library. See "notices/LICENSE.junit.txt"
for the licence information of the JUnit library.

This project includes the Mockito library, which is Copyright (c) 2007
Mockito contributors. See "notices/LICENSE.mockito.txt" for the licence
information of the Mockito library.


What is it?
-----------

Octopus is a middleware abstraction library. It provides a simple Java 
programming interface to various pieces of software that can be used to 
access distributed compute and storage resources. 


Why Octopus?
------------

Octopus is developed by the Netherlands eScience Center as a support library 
for our projects. Several projects develop end-user applications that require 
access to distributed compute and storage resources. Octopus provides a simple 
API to access those resources, allowing those applications to be developed more 
rapidly. The experience gained during the development of these end-user 
applications is used to improve the Octopus API and implementation. 


Installation
------------

The installation procedure and dependencies of the octopus library can be found 
in the file "INSTALL.md" in the octopus distribution. 


Design
------

Octopus is designed with extensibility in mind. It uses a modular and layer design
as shown in the figure below:

![Octopus design](images/octopus-design.png "Octopus design.")
	
Octopus consists of three layers, an *interface layer*, an *engine layer* and an 
*adaptor layer*. 

The *interface layer* is used by the application using octopus. It contains several 
specialized interfaces:

- Octopus: this is the main entry point used to retrieve the other interfaces. 
- Files: contains functionality related to files, e.g., creation, deletion, 
  copying, reading, writing, obtaining directory listings, etc. 
- Jobs: contains functionality related to job submission, e.g., submitting, 
  polling status, cancelling, etc. 
- Credentials: contains functionality related to credentials. Credentials 
  (such as a username password combination) are often needed to gain access to 
  files or to submit jobs. 

The modular design of octopus allows us to add additional interfaces in later 
versions, e.g., a Clouds interface to manage virtual machines, or a Networks 
interface to manage bandwidth-on-demand networks. 

The *adaptor layer* contains the adaptors for the each of the middlewares that 
octopus supports. An *adaptor* offers a middleware specific implementation 
for the functionality offered by one of the interfaces in octopus.
 
For example, an adaptor may provide an *sftp* specific implementation of the 
functions in the octopus *file interface* (such as *copy* or *delete*) by 
translating each of these functions to *sftp* specific code and commands.

For each interface in octopus there may be multiple adaptors translating 
its functionality to different middlewares. To distinguises between these 
adaptors octopus uses the *scheme* they support, such as "sftp", "http" 
or "ssh". There can be only one adaptor for each scheme. 

The *engine layer* of octopus contains the "glue" that connects each interface 
to the adaptors that implement its functionality. When a function of the 
interface layer is invoked, the call will be forwarded to the engine layer. 
It is then the responsibility of the engine layer to forward this call to the 
right adaptor. 

To perform this selection, the engine layer matches the *scheme* of the object 
on which the operation needs to be performed, to the *schemes* supported by 
each of the adaptors. When the schemes match, the adaptor is selected. 


Interfaces and datatypes
------------------------

This section will briefly explain each of the interfaces and related datatypes.
Detailed information about Octopus can be found in the online Javadoc at: 

<http://nlesc.github.io/octopus/javadoc/>

### Package Structure ##

The octopus API uses the following package structure:

- `nl.esciencecenter.octopus` Entry point into octopus.
- `nl.esciencecenter.octopus.credentials` Credential interface.
- `nl.esciencecenter.octopus.files`  Files interface.
- `nl.esciencecenter.octopus.jobs`  Jobs interface.
- `nl.esciencecenter.octopus.exeptions`  Exceptions used in octopus.
- `nl.esciencecenter.octopus.util`  Various utilty classes.

We will now briefly describe the most important classes and interfaces of these packages.

### Octopus factory and interface ###

The [`nl.esciencecenter.octopus`][1] package contains the entry point into the octopus library: 
[__OctopusFactory__][2]

    public class OctopusFactory {
       public static Octopus newOctopus(Map<String,String> properties) 
       public static void endOctopus(Octopus octopus) 
       public static void endAll()
    }

The __newOctopus__ method can be used to create a new octopus instance, while the 
__endOctopus__ method can be used to release the octopus instance once it is no longer needed.
It is important to end the octopus when it is no longer needed, as this allows it to release 
any resources it has obtained. 

When creating an octopus using __newOctopus__, the _properties_ parameter can be used to 
configure the octopus instance. If no configuration is necessary, `null` can be used.
Properties consist of a set of key-value pairs. In octopus all keys __must__ start with 
"octopus.". To configure the adaptors, properties of the form 
"octopus.adaptors.(name).(property)" can be used, where "(name)" is the name of the 
adaptor (for example "local" or "ssh") and "(property)" is the name of the property to be 
configured. Note that this name can be futher qualified, for example 
"octopus.adaptors.local.a.b.c". The available properties can be found in the documentation 
of the individual adaptors (see Appendix A). 

A call to __newOctopus__ will return an [__Octopus__][3]:

    public interface Octopus {
        Files files()
        Jobs jobs()
        Credentials credentials()
        Map<String,String> getProperties()
        AdaptorStatus getAdaptorStatus(String adaptorName)
        AdaptorStatus[] getAdaptorStatuses()
    }

The __files__, __jobs__ and __credentials__ methods in this interface can be used to retrieve 
various interfaces that the octopus library offers. They will be described in more detail below. 

The __getProperties__ method can be used to retrieve the properties used when the octopus was 
created. Most objects created by octopus contain such a __getProperties__ method. For brevity, 
we will not explain these further.

The __getAdaptorStatus__ method can be used to retrieve information about the adaptors. This 
information is returned in an [__AdaptorStatus__][4]:

    public interface AdaptorStatus {
        String getName()
        String getDescription()
        String[] getSupportedSchemes()
        OctopusPropertyDescription[] getSupportedProperties()
        Map<String, String> getAdaptorSpecificInformation()
    }
    
An __AdaptorStatus__ contains __getName__ to retrieve the name of an adaptor,  __getDescription__ to 
get a human readable description of what functionality it has to offer and __getSupportedSchemes__
to retrieve a list of the schemes it supports.

The __getSupportedProperties__ method can be used to retrieve a list of configuration options the adaptor 
supports. Each returned [__OctopusPropertyDescription__][5] gives a full description of a single property, 
including its name (of the form "octopus.adaptors.(name).(property)"), the expected type of its value, 
a human readable description of its purpose, etc. More information on the supported properties can be 
found in Appendix A.

Finally, __getAdaptorSpecificInformation__ can be used to retrieve status information from the adaptor. 
Each key contains a property of the form described above. 

### Credentials interface ###

The [`nl.esciencecenter.octopus.credentials`][6] package contains the [__Credentials__][7] interface of 
octopus:

    public interface Credentials {
        Credential newCertificateCredential(String scheme, String keyfile, String certfile, String username, 
            char [] password, Map<String,String> properties) 

        Credential newPasswordCredential(String scheme, String username, char [] password, Map<String,String> properties)
        Credential getDefaultCredential(String scheme)
        void close(Credential credential)
    }

The __Credentials__ interface contains various methods for creating credentials, based 
on certificates or passwords. For each method, the desired _scheme_ needs to be 
provided as a parameter (for example, "ssh" or "sftp"). This allows octopus to forward the 
call to the correct adaptor. Note that some types of credentials may not be supported by
all adaptors. An exception will be thrown when an unsupported __new**Credential__ methods is invoked. 

Additional configuration can also be provides using the _properties_ parameter, which use 
the same form as described in the _Octopus factory and interface_ section above. If no 
additional configuration is needed, `null` can be used. The __getDefaultCredential__
method returns the default credential for the given scheme. All adaptors are guarenteed to 
support this method. 

All __new**Credential__ methods return a [__Credential__][13] that contains the following 
methods: 

    public interface Credential {
       String getAdaptorName()
       Map<String,String> getProperties()
    }

The __getAdaptorName__ method can be used to retrieve the name of the adaptor that created 
the credential. Many adaptor specific objects returned by octopus contain this method. For 
brevity we will not explain this further.

When a __Credential__ is no longer used, it __must__ be closed using __close__. This releases any 
resources held by the __Credential__. The __isOpen__ method can be used to check if a __Credential__ 
is open or closed. 

### Files interface ###

The [`nl.esciencecenter.octopus.files`][8] package contains the [__Files__][9] interface of 
octopus. For readability we will split the explanation of __Files__ into several parts:

    public interface Files {
       FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String,String> properties)
       void close(FileSystem filesystem) 
       boolean isOpen(FileSystem filesystem)
       // ... more follows
    }

The __Files__ interface contains several method for creating and closing a [__FileSystem__][10]. 
A __FileSystem__ provides an abstraction for a (possibly remote) file system. 

To create a __FileSystem__ the __newFileSystem__ method can be used. As before, the desired __scheme__ 
must be provided as a parameter. In addition, the _location_ parameter provides information on the 
location of the file system using an adaptor specific string. For local file systems, the location 
must contain the root of the file system to access, such as "/" on Linux or "C:" on Windows. For remote 
file systems, the location typically contains the host name of the machine to connect to. The exact 
format of accepted location strings can be found in the adaptor documentation. 

The following are all valid combinations of file system schemes and locations:

    "file","/"                        connect to the local file system on Linux
    "file","C:"                       connect to the local C: drive on Windows  
    "sftp","example.com"              connect to example.com using sftp 
    "sftp","test@example.com:44"      connect to example.com using sftp on port 44 with "test" as user name.

The __newFileSystem__ method also has a _credential_ parameter to provide the credential needed to access the file 
system. If this parameter is set to `null` the default credentials will be used for the scheme. The _properties_
parameter can be used to provide additional configuration properties. Again, `null` can be used if no additional 
configuration is required. The returned __FileSystem__ contains the following methods:

    public interface FileSystem {
        String getScheme()
        String getLocation()
        Path getEntryPath()
    }

The __getScheme__ and __getLocation__ methods returns the scheme and location strings used to create the __FileSystem__. 
The __getEntryPath__ method returns the _path at which the file system was entered_. For example, when accessing a file 
system using "sftp" it is customary (but not manditory) to enter the file system at the users' home directory. Therefore, 
the entry path of the __FileSystem__ will be similar to "/home/(username)". For local file systems the entry path is 
typically set to the root of the file system (such as "/" or "C:").

When a __FileSystem__ is no longer used, it __must__ be closed using __close__. This releases any resources 
held by the __FileSystem__. The __isOpen__ method can be used to check if a __FileSystem__ is open or closed. 
Once a __FileSystem__ is created, it can be used to access files: 

    public interface Files {
       Path newPath(FileSystem filesystem, RelativePath location) 
       void createFile(Path path)
       void createDirectories(Path dir)
       void createDirectory(Path dir)
       boolean exists(Path path)
       void delete(Path path)
       FileAttributes getAttributes(Path path)
       // ... more follows
    }

The __newPath__ method can be used to create a new [__Path__][11]. An __Path__ represents a path
on a specific __FileSystem__. This path does not necessarily exists. To create an __Path__, both 
the target __FileSystem__ and a [__RelativePath__][12] are needed. A __RelativePath__ contains a 
sequence of strings separated using a special _separator_ character, which is used to identify a 
location on a file system (for example "/tmp/dir"). __RelativePath__ contains many utility methods 
for manipulating these string sequences. The details can be found in the Javadoc of 
[__RelativePath__][12].

__Files__ contains several methods to create and delete files and directories. When creating files and 
directories octopus checks if the target already exists. If so, an exception will be thrown. Similary, 
an exception is thrown when attempting to delete non-existing file or a directory that is not empty. 
The __exists__ method can be used to check if a path exists.

Using the __getAttributes__ method the attributes of a file can be retrieved. The returned
[__FileAttributes__][14] contains information on the type of file (regular file, directory, link, etc), 
it size, creation time, access rights, etc. 

To list directories, the following methods are available:

    public interface Files {
       DirectoryStream<Path> newDirectoryStream(Path dir)
       DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir)  
       // ... more follows
    }

Both __newDirectoryStream__ and __newAttributesDirectoryStream__ return a [__DirectoryStream__][15]
which can be used to iterate over the contents of a directory. For the latter, the __FileAttributes__ 
for each of the files are also included. alternatively, these methods are also available with an extra 
_filter_ parameter, which can be used to filter the stream in advance.

To read or write files, the following methods are available:

    public interface Files {
       InputStream newInputStream(Path path)
       OutputStream newOutputStream(Path path, OpenOption... options)
    }

Using these methods, an __InputStream__ can be created to read a file, and an __OutputStream__ can be 
created to write a file. The __newOutputStream__ method requires a _options_ parameter to specify how 
the file should be opened for writing (for example, should the data be append or should the file be 
truncated first). These options are describe in more detail in the Javadoc.

To copy files, the following methods are available:

    public interface Files {
       Copy copy(Path source, Path target, CopyOption... options)
       CopyStatus getCopyStatus(Copy copy)
       CopyStatus cancelCopy(Copy copy)
    }

The __copy__ method supports various copy operations such as a regular copy, a resume or an append. 
The _options_ parameter can be used to specify the desired operation. 
Normally, __copy__ performs its operation _synchronously_, that is, the call blocks until the copy 
is completed. However, _asynchronous_ operations are also supported by providing the option 
[__CopyOption.ASYNCHRONOUS__][17]. In that case a [__Copy__][16] object is returned that can be used 
to retrieve the status of the copy (using __getCopyStatus__) or cancel it (using __cancelCopy__).
The details of the available copy operations can be found in the Javadoc of [__CopyOption__][17].

### Jobs interface ###

The [`nl.esciencecenter.octopus.job`][18] package contains the [__Jobs__][19] interface of octopus.
For readability we will split the explanation of __Jobs__ into several parts:

    public interface Jobs {
        Scheduler newScheduler(String scheme, String location, Credential credential, Map<String,String> properties)
        void close(Scheduler scheduler)
        boolean isOpen(Scheduler scheduler)
        // ... more follows
    }

The __Jobs__ interface contains the __newScheduler__ method that can be used to create a [__Scheduler__][20]. 
A __Scheduler__ provides an abstraction for a (possibly remote) scheduler that can be used to run jobs. 
The __newScheduler__ method has __scheme__ and __location__ parameters that specify how to access the 
scheduler. As with __newFileSystem__ the __location__ is adaptor specific. To access the local scheduler, 
passing `null` or an empty string is sufficient. To access remote schedulers, the location typically contains 
the host name of the machine to connect to. The exact format of accepted location strings can be found in the 
adaptor documentation.

The following are valid examples of scheduler schemes and locations:

    "local",""                     the local scheduler 
    "ssh","example.com"            connect to a remote scheduler at example.com using SSH
    "slurm",""                     connect to a local slurm scheduler
    "slurm","test@example.com:44"  connect to a remote slurm scheduler at example.com via SSH on port 44 with "test" as user name.

When a __Scheduler__ is no longer used, is __must__ be closed using the __close__ method. The __isOpen__ 
method can be use to check if a __Scheduler__ is open or closed. A __Scheduler__ contains the following:

    public interface Scheduler {
        String[] getQueueNames()
        boolean isOnline()
        boolean supportsInteractive()
        boolean supportsBatch()
    }

Each __Scheduler__ contains one or more queues to which jobs can be submitted. Each queue has a name that 
is unique to the __Scheduler__. The __getQueueNames__ method can be used to retrieve all queue names. 

The __isOnline__ method can be used to determine if the __Scheduler__ is an _online scheduler_ or an 
_offline scheduler_. Online schedulers need to remain active for their jobs to run. Closing an online 
scheduler will kill all jobs that were submitted to it. Offline schedulers do not need to remains active 
for their jobs to run. A submitted job will typically be handed over to some external server that will 
manage the job for the rest of its lifetime.

The __supportsInteractive__ and __supportsBatch__ method can be use to check if the __Scheduler__ supports
interactive and/or batch jobs. Interactive jobs are jobs where the user gets direct control over the standard 
streams of the job (the _stdin_, _stdout_ and _stderr_ streams). The user __must__ retrieve these streams 
using the __getStreams__ method in __Jobs__ and then provide input and output, or close the streams. Failing
to do so may cause the job to block indefinately. Batch jobs are jobs where the standard streams are redirected 
from and to files. The location of these files must be set before the job is started, as will be explained 
below.

Once a __Scheduler__ is created, __Jobs__ contains several methods to retrieve information about the 
__Scheduler__:

    public interface Jobs {
        String getDefaultQueueName(Scheduler scheduler)
        QueueStatus getQueueStatus(Scheduler scheduler, String queueName)
        QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames).
        Job[] getJobs(Scheduler scheduler, String... queueNames)
        // ... more follows
    }

The __getQueueStatuses__ method can be used to retrieve information about a queue. If no queue names 
are provided as a parameter, information on all queues in the scheduler will be returned. Using the 
__getDefaultQueueName__ the default queue can be retrieved for the __Scheduler__. The __getJobs__ method 
can be used to retrieve information on all jobs in a queue. Note that this may also include jobs
from other users.

To submit and manage jobs, the __Jobs__ interface contains the following methods:

    public interface Jobs {
        Job submitJob(Scheduler scheduler, JobDescription description)
        Streams getStreams(Job job)
        JobStatus getJobStatus(Job job)
        JobStatus[] getJobStatuses(Job... jobs)
        JobStatus waitUntilRunning(Job job, long timeout)
        JobStatus waitUntilDone(Job job, long timeout)
        JobStatus cancelJob(Job job)
    }    

The __submitJob__ method can be used to submit a job to a __Scheduler__. A [__JobDescription__][21] must 
be provided as parameter. A __JobDescription__ contains all necessary information on how to start the job, 
for example, the location of the executable, any command line arguments that are required, the working 
directory, if the job is an interactive of batch job, the location of the files for stream redirection 
(in case of a batch job), etc. See the Javadoc for details of the __JobDescription__.

Once a job is submitted, a [__Job__][22] object is returned that can be used with __getJobStatus__ to 
retrieve the status of the job, and with __cancelJob__ to cancel it. This __Job__ contains the following:

    public interface Job {
        JobDescription getJobDescription()
        Scheduler getScheduler()
        String getIdentifier()
        boolean isInteractive()
        boolean isOnline()
    } 

Besides methods for retrieveing the __JobDescription__ and __Scheduler__ that created it, each __Job__ also 
contains the __isInteractive__ method to determine if the __Job__ is interactive, and the __isOnline__ 
method to determine if the job is running on an _online scheduler_ (explained above).
 
After submitting a job, __waitUntilRunning__ can be used to wait until a job is no longer waiting in the 
queue and __waitUntilDone__ can be used to wait until the job has finished.  

For all methods returning a [__JobStatus__][23], the following rule applies: after a job has finished, the 
status is only guarenteed to be returned _once_. Any subsequent calls to a method that returns a 
__JobStatus__ _may_ throw an exception stating that the job does not exist. Some adaptors may return 
a result however.  

### Exceptions ###

The [`nl.esciencecenter.octopus.exceptions`][24] package contains the exceptions that may be thrown by 
octopus. See the Javadoc for the available exceptions.

### Utilities classes ###

The [`nl.esciencecenter.octopus.util`][25] package contains various utility classes. The main entry points 
are __Utils__, __Sandbox__ and __JavaJobDescription__.

In [__Utils__][42] various utility methods can be found that make it easier to use Octopus. Many methods provide 
simple shortcuts to often used code constructs. Some examples are shown below:

    public class Utils {
        // Create a new local Scheduler.
        public static Scheduler getLocalScheduler(Jobs jobs)             

        // Create a new Scheduler without Credentials or properties.
        public static Scheduler newScheduler(Jobs jobs, String scheme)   

        // Create a Path that represents the home directory of the current user.
        public static Path getLocalHome(Files files)                     

        // Create a Path that represents the current working directory.
        public static Path getLocalCWD(Files files)                      

        // Convert a String containing a local path into a Path.
        public static Path fromLocalPath(Files files, String path)       

        // Retrieve all local file systems.
        public static FileSystem [] getLocalFileSystems(Files files)     
 
        //  Are we running on a Linux machine ?
        public static boolean isLinux()   

        // Are we running on a Windows machine ?
        public static boolean isWindows()                     

        // Are we running on a OSX machine ?
        public static boolean isOSX()                                   
    }

In addition many methods are provided for reading data from files or streams to various output targets,
writing data to files or streams from various input sources, recursive copying, recursive deletion, etc.
See the Javadoc of [__Utils__][42] for details.

A [__Sandbox__][43] is a utility class that makes is it easier to create a (possibly remote) temporary 
directory and transfer files to and from this directory. A Sandbox is often used in when submitting 
jobs that require input files and / or produce output files. Sandbox contains the following methods:

    public class Sandbox {
       Sandbox(Files files, Path root, String sandboxName)
       void addUploadFile(Path src, String dest)
       void addDownloadFile(String src, Path dest)
       void upload(CopyOption... options)
       void download(CopyOption... options)
       void delete()
    }

Creating a Sandbox requires an octopus __Files__ interface and a __root__ directory. The Sandbox will 
then create a temporary directory __sandboxName__ in __root__. If __sandboxName__ is `null`, a random 
name will be generated. Using __addUploadFile__ files can be added to the upload queue. These files will 
be transferred to the Sandbox directory when __upload__ is invoked. Similarly, using __addDownloadFile__,
files can be added to the download queue. They will be downloaded from the Sandbox directory when 
__download__ is invoked. Finally, __delete__ can be used to delete the Sandbox directory.

A [__JavaJobDescription__][44] is a utility class that makes is it easier to create a __JobDescription__ 
for running a Java application. In addition to the command line arguments used by the application, Java 
applications typically require a number of _special_ command line argument for the Java Virtual Machine 
(JVM), such as a _class path_, _system properties_, and _JVM options_. 

The JavaJobDescription class extends the regular JobDescription with support for these additional 
arguments. When a Job a submitted to an octopus Scheduler that uses a JavaJobDescription, the various 
types of command line arguments will be merged automatically into a single arguments list. See the 
Javadoc of [__JavaJobDescription__][44] for details.

Examples
--------

Examples of how to use octopus can be found in the [examples][26] directory. We will list the examples here 
in order of increasing complexity, and with a short description of each example.

### Initializing Octopus ###

Creating an __Octopus__ and retrieving the __Credentials__, __Files__ and __Jobs__ interfaces: 
[CreatingOctopus.java][27]

Creating an __Octopus__ with configuration properties: 
[CreatingOctopusWithProperties.java][28]

### Creating Credentials ###

Creating a password and default __Credential__:
[CreatingCredential.java][29]

### File Access ###

Creating a local __FileSystem__:
[CreateLocalFileSystem.java][30]

Checking if a local file exists:
[LocalFileExists.java][32]

Creating a __FileSystem__ based on a URI. 
[CreateFileSystem.java][31]

Checking if a (possibly remote) file exists:
[FileExists.java][32]

Listing a directory:
[DirectoryListing.java][33]

Listing the attributes of a file:
[ShowFileAttributes.java][45]

Copying a file:
[CopyFile.java][34]

### Job Submission ###

Creating a __Scheduler__ and retrieving the status of its queues:
[ListQueueStatus.java][36]

Creating a __Scheduler__ and retrieving the jobs:
[ListJobs.java][37]

Listing the status of a Job:
[ListJobStatus.java][38]

Submitting a batch job without output: 
[SubmitSimpleBatchJob.java][39]

Submitting a batch job with output: 
[SubmitBatchJobWithOutput.java][40]

Submitting an interactive job with output: 
[SubmitInteractiveJobWithOutput.java][41]

[1]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/package-summary.html
[2]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/OctopusFactory.html
[3]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/Octopus.html
[4]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/AdaptorStatus.html
[5]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/OctopusPropertyDescription.html
[6]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/credentials/package-summary.html 
[7]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/credentials/Credentials.html
[8]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/package-summary.html
[9]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/Files.html 
[10]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/FileSystem.html
[11]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/Path.html
[12]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/RelativePath.html
[13]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/credentials/Credential.html
[14]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/FileAttributes.html
[15]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/DirectoryStream.html
[16]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/Copy.html
[17]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/files/CopyOption.html
[18]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/jobs/package-summary.html
[19]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/jobs/Jobs.html
[20]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/jobs/Scheduler.html
[21]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/jobs/JobDescription.html
[22]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/jobs/Job.html
[23]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/jobs/JobStatus.html
[24]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/exceptions/package-summary.html
[25]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/utils/package-summary.html
[26]: https://github.com/NLeSC/octopus/tree/develop/examples/src/nl/esciencecenter/octopus/examples
[27]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/CreatingOctopus.java
[28]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/CreatingOctopusWithProperties.java
[29]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/credentials/CreatingCredential.java
[30]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/CreateLocalFileSystem.java
[31]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/CreateFileSystem.java
[32]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/LocalFileExists.java  
[33]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/FileExists.java
[34]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/DirectoryListing.java
[35]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/CopyFile.java 
[36]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/jobs/ListQueueStatus.java
[37]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/jobs/ListJobs.java
[38]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/jobs/ListJobStatus.java
[39]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/jobs/SubmitSimpleBatchJob.java
[40]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/jobs/SubmitBatchJobWithOutput.java
[41]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/jobs/SubmitInteractiveJobWithOutput.java
[42]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/utils/Utils.html
[43]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/utils/Sandbox.html
[44]: http://nlesc.github.io/octopus/javadoc/nl/esciencecenter/octopus/utils/JavaJobDescription.html
[45]: https://github.com/NLeSC/octopus/blob/develop/examples/src/nl/esciencecenter/octopus/examples/files/ShowFileAttributes.java

Appendix A: Adaptor Documentation
---------------------------------

This section contains the adaptor documentation which is generated from the information provided by the adaptors themselves.

Octopus currently supports 4 adaptors: local, ssh, gridengine, slurm.

Adaptor: local
--------

The local adaptor implements all functionality with  standard java classes such as java.lang.Process and java.nio.file.Files.

#### Supported schemes: ####
local, file

#### Supported locations: ####
(null), (empty string), /

#### Supported properties: ####


__`octopus.adaptors.local.queue.pollingDelay`__

The polling delay for monitoring running jobs (in milliseconds).

- Expected type: INTEGER

- Default value: 1000

- Valid for: [OCTOPUS]


__`octopus.adaptors.local.queue.multi.maxConcurrentJobs`__

The maximum number of concurrent jobs in the multiq..

- Expected type: INTEGER

- Default value: 4

- Valid for: [OCTOPUS]



Adaptor: ssh
--------

The SSH adaptor implements all functionality with remove ssh servers.

#### Supported schemes: ####
ssh, sftp

#### Supported locations: ####
[user@]host[:port]

#### Supported properties: ####


__`octopus.adaptors.ssh.autoAddHostKey`__

Automatically add unknown host keys to known_hosts.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [FILESYSTEM, SCHEDULER]


__`octopus.adaptors.ssh.strictHostKeyChecking`__

Enable strict host key checking.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [FILESYSTEM, SCHEDULER]


__`octopus.adaptors.ssh.loadKnownHosts`__

Load the standard known_hosts file.

- Expected type: BOOLEAN

- Default value: true

- Valid for: [OCTOPUS]


__`octopus.adaptors.ssh.queue.pollingDelay`__

The polling delay for monitoring running jobs (in milliseconds).

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]


__`octopus.adaptors.ssh.queue.multi.maxConcurrentJobs`__

The maximum number of concurrent jobs in the multiq..

- Expected type: INTEGER

- Default value: 4

- Valid for: [SCHEDULER]


__`octopus.adaptors.ssh.gateway`__

The gateway machine used to create an SSH tunnel to the target.

- Expected type: STRING

- Default value: null

- Valid for: [FILESYSTEM, SCHEDULER]



Adaptor: gridengine
--------

The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.

#### Supported schemes: ####
ge, sge

#### Supported locations: ####
(locations supported by local), (locations supported by ssh)

#### Supported properties: ####


__`octopus.adaptors.gridengine.ignore.version`__

Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


__`octopus.adaptors.gridengine.accounting.grace.time`__

Number of milliseconds a job is allowed to take going from the queue to the qacct output.

- Expected type: LONG

- Default value: 60000

- Valid for: [SCHEDULER]


__`octopus.adaptors.gridengine.poll.delay`__

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


__`octopus.adaptors.slurm.ignore.version`__

Skip version check is skipped when connecting to remote machines. WARNING: it is not recommended to use this setting in production environments!

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


__`octopus.adaptors.slurm.disable.accounting.usage`__

Do not used accounting info of slurm, even when available. Mostly for testing purposes

- Expected type: BOOLEAN

- Default value: false

- Valid for: [SCHEDULER]


__`octopus.adaptors.slurm.poll.delay`__

Number of milliseconds between polling the status of a job.

- Expected type: LONG

- Default value: 1000

- Valid for: [SCHEDULER]



