package nl.esciencecenter.xenon.adaptors.filesystems.gridftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.util.ConfigUtil;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class GridFTPFileAdaptor extends FileAdaptor {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "gridftp";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 2811;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Adaptor for the GridFTP file system";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "gsiftp://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "gridftp.";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, XenonPropertyDescription.Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

    public GridFTPFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public boolean supportsThirdPartyCopy() {
        return true;
    }

    public static GSSCredential getDefaultCredential() throws InvalidCredentialException {

        System.out.println("Proxy Location " + ConfigUtil.discoverProxyLocation());
        return loadProxyCredential(new File(ConfigUtil.discoverProxyLocation()));
    }

    public static GSSCredential loadCredential(String file) throws InvalidCredentialException {
        return null;
    }

    public static GSSCredential loadProxyCredential(File proxyFile) throws InvalidCredentialException {

        byte[] proxyBytes = new byte[(int) proxyFile.length()];

        try (FileInputStream in = new FileInputStream(proxyFile)) {
            in.read(proxyBytes);
        } catch (IOException e) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Failed to open credential proxy file: " + proxyFile, e);
        }

        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();

        GSSCredential credential;
        try {
            credential = manager.createCredential(proxyBytes, ExtendedGSSCredential.IMPEXP_OPAQUE, GSSCredential.DEFAULT_LIFETIME, null,
                    GSSCredential.INITIATE_AND_ACCEPT);
        } catch (GSSException e) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Failed to instantiate credential from file " + proxyFile, e);
        }

        return credential;

    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        GridFTPClient client = null;

        URI uri;

        try {
            uri = new URI(location);
        } catch (Exception e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String cwd = uri.getPath();

        if (!"gsiftp".equals(scheme)) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Scheme not supported: " + scheme);
        }

        if (port == -1) {
            port = DEFAULT_PORT;
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        try {
            client = new GridFTPClient(host, port);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to connect to " + location, e);
        }

        GSSCredential cred = null;

        if (credential instanceof DefaultCredential) {
            cred = getDefaultCredential();
        } else if (credential instanceof CertificateCredential) {
            cred = loadCredential(((CertificateCredential) credential).getCertificateFile());
        } else {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Credential type not supported");
        }

        try {
            client.authenticate(cred);
        } catch (ServerException e) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Failed to authenticate to server", e);
        } catch (IOException ioe) {
            throw new XenonException(ADAPTOR_NAME, "Error during authentication with server", ioe);
        }

        try {
            client.setPassiveMode(true);
        } catch (ClientException | ServerException | IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to configure gridftp client", e);
        }

        if (cwd != null) {
            try {
                client.changeDir(cwd);
            } catch (ServerException | IOException e) {
                try {
                    client.close();
                } catch (Exception e2) {
                    // ignored, as this may be the result of the same connection error.
                }
                throw new InvalidLocationException(ADAPTOR_NAME, "Failed to access working directory: " + cwd);
            }
        }

        return new GridFTPFileSystem(getNewUniqueID(), ADAPTOR_NAME, location, credential, new Path(cwd), (int) bufferSize, xp);
    }

}
