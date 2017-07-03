package nl.esciencecenter.xenon.adaptors.file.jclouds;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.InvalidCredentialException;
import nl.esciencecenter.xenon.adaptors.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.ftp.FtpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.s3.S3Properties;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidPathException;
import org.jclouds.ContextBuilder;
import org.jclouds.aws.s3.AWSS3ApiMetadata;
import org.jclouds.s3.S3ApiMetadata;
import org.jclouds.blobstore.BlobStoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by atze on 29-6-17.
 */
public class JCloudsFileAdaptor extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileAdaptor.class);

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The JClouds adaptor uses Apache JClouds to talk to s3 and others";

    /** The locations supported by this adaptor */
    private static final String [] ADAPTOR_LOCATIONS = new String [] { "s3://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "s3.";

    protected static final XenonPropertyDescription [] VALID_PROPERTIES = new XenonPropertyDescription[0];

    public JCloudsFileAdaptor() {
        super("jclouds", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES, false);
    }


    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        int split = location.lastIndexOf("/");
        if(split < 0){
            throw new InvalidLocationException("s3","No bucket found in url: " + location);
        }

        String server = location.substring(0,split);
        String bucket = location.substring(split + 1);

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        if (!(credential instanceof PasswordCredential)){
            throw new InvalidCredentialException("jclouds", "No secret key given for jclouds connection.");
        }
        PasswordCredential pwUser = (PasswordCredential) credential;
        if(properties == null) { properties = new HashMap<>(); }
        System.err.println("server : " + server);
        BlobStoreContext context = ContextBuilder.newBuilder("s3").endpoint(server).
                credentials(pwUser.getUsername(), new String(pwUser.getPassword())).buildView(BlobStoreContext.class);
        return new JCloudsFileSytem(getNewUniqueID(),"jclouds", server, context,  bucket,xp);
    }
}
