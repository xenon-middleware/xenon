package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by atze on 3-7-17.
 */
public class HDFSFileAdaptor extends FileAdaptor{

    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSFileAdaptor.class);

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Adaptor for the Apache Hadoop file system";

    /** The locations supported by this adaptor */
    private static final String [] ADAPTOR_LOCATIONS = new String [] { "hdfs://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "hdfs.";

    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[0];

    public HDFSFileAdaptor() {
        super("hdfs", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }


    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        Configuration conf = new Configuration(false);
        conf.set("fs.defaultFS", location);
        // TODO: use authentication
        if(!(credential instanceof DefaultCredential)){
            throw new XenonException("hdfs", "Currently only default credentials supported on HDFS");
        }
        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);
        try {
            org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.get(conf);
            return new HDFSFileSystem(getNewUniqueID(),location, fs,xp);
        } catch(IOException e){
            throw new XenonException("hdfs", "Failed to create HDFS connection: " + e.getMessage());
        }

    }
}