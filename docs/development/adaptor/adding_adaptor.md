# Adding an adaptor

To add an adaptor the following steps must be performed:
* source code
* dependencies (optional)
* unit tests
* integration tests
* Docker based server for adaptor to test against in integration tests
* registration in Xenon engine
* registration in build system

For a new file adaptor use `webdav` as an example. 
For a new job adaptor use `slurm` as an example. 

Adding an adaptor can be completed by adding/changing files in the following locations:
1. Source code in `src/main/java/nl/esciencecenter/xenon/adaptors/<adaptor name>`.
2. Specify dependencies of adaptor in `build.gradle`.
3. Unit tests in `src/test/java/nl/esciencecenter/xenon/adaptors/<adaptor name>`.
4. Register adaptor in `src/main/java/nl/esciencecenter/xenon/engine/XenonEngine.java:loadAdaptors()`
5. Integration tests
  1. Create a Dockerfile in `src/integrationTest/docker/xenon-<adaptor-name>` for a server of the adaptor to test against
  2. Register the Docker image in `src/integrationTest/docker/docker-compose.yml` and  `gradle/docker.gradle`
  3. Add the Docker container credentials/location/configuration to `src/integrationTest/docker/xenon.test.properties.docker`
  4. Create an integration test in `src/integrationTest/java/esciencecenter/xenon/adaptors/<adaptor name>/`

# Example: 

## Creating the gridftp adaptor. 

1. Create the `nl/esciencecenter/xenon/adaptors/gridftp` package.
2. In this package, create two classes, one that extends `nl/esciencecenter/xenon/filesystems/FileAdaptor.java` (in this case we call it `GridFTPFileAdaptor.java`) and one that extends
   `nl.esciencecenter.filesystems.FileSystem.java` (in this case we call it `GridFTPFileSystem.java`). Next provide an implemention for both.

### The initial GridFTPFileAdaptor implementation

The class `GridFTPFileAdaptor` serves as a factory class to create new instances of `GridFTPFileSystem`. In addition, is is used to provide information to the user on how to configure and 
use this filesystem. There are two methods that must be implemented, a public constructor and `createFileSystem`, and several optional ones. 

The no-arguments public constructor looks like this:

    public GridFTPFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }    

This constructor's only duty is to call the constructor of super class (that is `FileSystem`) to initialize several fields that are used to provide human readable information about this adaptor to the user. 
It is good practise to declare several `final static` variables in the adaptor class that contain this information, and then simply pass them on (as shown above). For the gridftp adaptor these fields are: 

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "gridftp";

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Adaptor for the GridFTP file system";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "hostname[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "gridftp.";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, XenonPropertyDescription.Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

The name of the adaptor name should be unique, as it is used by the user to instantiate the adaptor. Both the adaptor description and locations are meant to be human readable. They describe what the adaptor does, 
and what format location string the user should provide when creating a filesystem. The valid properties contains various configuation setting for the adaptor, including a default value and expected data type, 
and a human readable description of the property. Note that it is customary to use a well defined namespace for the property names, it this case `nl.esciencecenter.xenon.filesystems.gridftp.*`.

Next to the descriptions passed to the constructor, there are several methods the `GridFTPFileAdaptor` _may_ override to describe the features it offers to the user. These method can be found in the 
`nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription` class: 

     /**
     * Does this adaptor support third party copy ?
     *
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.
     *
     * @return if this adaptor supports third party copy.
     */
    boolean supportsThirdPartyCopy();

    /**
     * Can this adaptor read symbolic links ?
     *
     * @return if this adaptor can read symbolic links.
     */
    boolean canReadSymboliclinks();

    /**
     * Can this adaptor create symbolic links ?
     *
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.
     *
     * @return if this adaptor can create symbolic links.
     */
    boolean canCreateSymboliclinks();

    /**
     * Is this adaptor connectionless ?
     * 
     * A connectionless adaptor to not retain a connection to a resources between operations. Instead a new connection is created for each operation that is
     * performed. In contrast, connected adaptors typically perform a connection setup when they are created and reuse this connection for each operation.
     * 
     * @return if this adaptor is connectionless.
     */
    boolean isConnectionless();

    /**
     * Does this adaptor support reading of posix style permissions?
     * 
     * @return if this adaptor support reading of posix style permissions.
     */
    boolean supportsReadingPosixPermissions();

    /**
     * Does this adaptor support setting of posix style permissions?
     * 
     * @return if this adaptor supports setting of posix style permissions.
     */
    boolean supportsSettingPosixPermissions();

    /**
     * Does this adaptor support renaming of files ?
     * 
     * @return if this adaptor supports renaming of files.
     */

    boolean supportsRename();

    /**
     * Can this adaptor append data to existing files ?
     * 
     * @return if this adaptor can append data to existing files.
     */
    boolean canAppend();

    /**
     * When writing to a file, does this adaptor need to know the size of the data beforehand ?
     * 
     * @return if this adaptor needs to know the size of the date written to a file beforehand.
     */
    boolean needsSizeBeforehand(); 

Often, the default values returned by these methods are sufficient. When they are not, an adaptor can override the necesary method. The default values are:  

    supportsThirdPartyCopy() - false
    canReadSymboliclinks()  - true
    canCreateSymboliclinks() - false
    isConnectionless() - false
    supportsReadingPosixPermissions() - false
    supportsSettingPosixPermissions() - false
    supportsRename() - true
    canAppend() - true
    needsSizeBeforehand() - false

In our case the gridftp protocol supports third party data tranfers, so we add the following method to the `GridFTPFileAdaptor`:

    @Override
    public boolean supportsThirdPartyCopy() {
        return true;
    }

### Setting up the FileSystem 

Next, we need to implement `createFileSystem` in the `GridFTPFileAdaptor` class. This method is called whenever the uses creates a new filesystem of this type. For example:

   FileSystem.create("gridftp", "myserver.edu", credential, properties);

Xenon will use the "gridftp" name to lookup the GridFTPFileAdaptor, and invoke the `createFileSystem` method using the provide location, credentials, and properties. It looks like this:

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        // create a filesystem here!  
        // ..  
        return filesystem;
    }


The `FileSystem` implementation that is returned represents an actual filesystem or data store somewhere, usually on some remote server. Often, a network connection is required to communicate 
with this server using some protocol, and some form of authentication is required to set up this connection. Creating such the connection and performing the authentication 
is the task of `createFileSystem`. Once the connection is established, all relevant objects (data, network connections, etc) are be wrapped in a `GridFTPFileSystem` implementation (described below)
and returned to the user.


Since we not want to implement the gridftp protocol ourselves, we use the JGlobus library to provide us with the necessary implementation. To use this library, we add it to our dependencies in 
the 'build.gradle' file in the Xenon root directory. This file will contain an `dependency` block containing references to libraries needed for the various adaptors:

    dependencies {

        // JClouds (for S3 filesystem)
        compile group: 'org.apache.jclouds.api', name: 's3', version: '2.0.2'
        compile group: 'org.apache.jclouds.provider', name: 'aws-s3', version: '2.0.2'

        // Hadoop (for HDFS filesystem)
        compile group: 'org.apache.hadoop', name: 'hadoop-common', version: '2.8.1'
        compile group: 'org.apache.hadoop', name: 'hadoop-hdfs', version: '2.8.1'
  
        // Sardine (for WebDAV filesystem)
        compile group: 'commons-net', name: 'commons-net', version: '3.3'
        compile group: 'com.github.lookfirst', name: 'sardine', version: '5.7'

        ... more ...
    }

We can add our dependecy to this block:

    // JGlobus (for gridftp)
    compile group: 'org.jglobus', name: 'gridftp', version: '2.1.0'

Be sure to include the specific version of the desired library. Otherwise, the latest version will be used, which may break your code unexpectedly.




### The GridFTPFileSystem implementation

The `GridFTPFileSystem` class extends `nl.esciencecenter.filesystems.FileSystem` and should look similar to this:

    public class GridFTPFileSystem extends FileSystem {

        public GridFTPFileSystem(String uniqueID, String adaptor, String location, Credential credential, Path workDirectory, int bufferSize, XenonProperties properties) {
            super(uniqueID, adaptor, location, credential, workDirectory, bufferSize, properties);
            // TODO Auto-generated constructor stub
        }
  
        @Override
        public boolean isOpen() throws XenonException {
	    // TODO
        }
 
        @Override
        public void rename(Path source, Path target) throws XenonException {
            // TODO
        }

        @Override
        public void createDirectory(Path dir) throws XenonException {
            // TODO
        } 

        // .. lots of method to implement here!
    }
  


 The methods in the `FileSystem` class represent the operation the user 
can perform on the filesystem and its files, such as listing the content, creating and deleting files, write to and reading from files, etc.

   
