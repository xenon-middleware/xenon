package nl.esciencecenter.xenon.adaptors.filesystems.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;

import nl.esciencecenter.xenon.adaptors.XenonProperties;

import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.jclouds.JCloudsFileSytem;
import nl.esciencecenter.xenon.adaptors.filesystems.s3.S3FileSystem;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by atze on 29-6-17.
 */
public class S3FileAdaptor extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3FileAdaptor.class);

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The JClouds adaptor uses Apache JClouds to talk to s3 and others";

    /** The locations supported by this adaptor */
    private static final String [] ADAPTOR_LOCATIONS = new String [] { "s3://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "s3.";

    protected static final XenonPropertyDescription [] VALID_PROPERTIES = new XenonPropertyDescription[0];

    public S3FileAdaptor() {

        super("s3", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES, false, false, false);
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
            throw new InvalidCredentialException("s3", "No secret key given for s3 connection.");
        }
        PasswordCredential pwUser = (PasswordCredential) credential;
        if(properties == null) { properties = new HashMap<>(); }
        System.err.println("server : " + server);
        BlobStoreContext context = ContextBuilder.newBuilder("s3").endpoint(server).
                credentials(pwUser.getUsername(), new String(pwUser.getPassword())).buildView(BlobStoreContext.class);
        return new JCloudsFileSytem(getNewUniqueID(),"s3", server, context,  bucket,xp);
    }

}