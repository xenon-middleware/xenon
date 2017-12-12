package nl.esciencecenter.xenon.adaptors.filesystems.gridftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.MlsxEntry;
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
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;

public class GridFTPFileAdaptor extends FileAdaptor {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "gridftp";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 2811;

    /** The maximum buffer size used in file transfer */
    protected static final long MAX_BUFFER_SIZE = 128 * 1024 * 1024;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Adaptor for the GridFTP file system";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "gsiftp://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "gridftp.";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, XenonPropertyDescription.Type.SIZE, "128K", "The buffer size to use when copying files (in bytes).") };

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

    // Close a GridFTPClient after some fatal exception occurred. Most of the time this close will fail as we have already lost the connection to the server,
    // but we try anyway. We ignore the any exception thrown here, as there will always be some other (more informative) exception already on the way to the
    // user.
    private void forceClose(GridFTPClient client) {
        try {
            client.close();
        } catch (Exception e) {
            // intentionally ignored.
        }
    }

    private void authenticate(GridFTPClient client, GSSCredential cred) throws XenonException {
        try {
            client.authenticate(cred);
        } catch (ServerException e) {
            forceClose(client);
            throw new InvalidCredentialException(ADAPTOR_NAME, "Failed to authenticate to server", e);
        } catch (IOException ioe) {
            forceClose(client);
            throw new XenonException(ADAPTOR_NAME, "Error during authentication with server", ioe);
        }
    }

    // private Path setWorkingDirectory(GridFTPClient client, String wd) throws XenonException {
    //
    // if (wd == null || "".equals(wd)) {
    // try {
    // return new Path(client.getCurrentDir());
    // } catch (Exception e) {
    // forceClose(client);
    // throw new XenonException(ADAPTOR_NAME, "Failed to retrieve current working directory", e);
    // }
    // }
    //
    // try {
    // MlsxEntry e = client.mlst(wd);
    //
    // if (!MlsxEntry.TYPE_DIR.equals(e.get(MlsxEntry.TYPE))) {
    // forceClose(client);
    // throw new NoSuchPathException(ADAPTOR_NAME, "Working directory does not exist: " + wd);
    // }
    //
    // client.changeDir(wd);
    // } catch (ServerException | IOException e) {
    // forceClose(client);
    // throw new XenonException(ADAPTOR_NAME, "Failed to set working directory to: " + wd, e);
    // }
    //
    // return new Path(wd);
    // }

    protected GridFTPClient connectClient(String location, Credential credential, Map<String, String> properties) throws XenonException {

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
        // String cwd = uri.getPath();

        if (!"gsiftp".equals(scheme)) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Scheme not supported: " + scheme);
        }

        if (port == -1) {
            port = DEFAULT_PORT;
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

        authenticate(client, cred);

        try {
            client.setPassiveMode(true);
            // client.setType(Session.TYPE_IMAGE);
            // client.setLocalTCPBufferSize((int) bufferSize);
            // client.setTCPBufferSize((int) bufferSize);
        } catch (ClientException | ServerException | IOException e) {
            forceClose(client);
            throw new XenonException(ADAPTOR_NAME, "Failed to configure gridftp client", e);
        }

        return client;
    }

    private String getCurrentWorkingDirectory(GridFTPClient client, String location) throws XenonException {

        try {
            String pathFromURI = new URI(location).getPath();

            if (pathFromURI == null || pathFromURI.isEmpty()) {
                return client.getCurrentDir();
            }

            try {
                MlsxEntry e = client.mlst(pathFromURI);

                if (!MlsxEntry.TYPE_DIR.equals(e.get(MlsxEntry.TYPE))) {
                    forceClose(client);
                    throw new NoSuchPathException(ADAPTOR_NAME, "Specified working directory does not exist: " + pathFromURI);
                }

                client.changeDir(pathFromURI);
            } catch (ServerException | IOException e) {
                throw new XenonException(ADAPTOR_NAME, "Failed to set working directory to: " + pathFromURI, e);
            }

            return pathFromURI;

        } catch (URISyntaxException e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        } catch (ServerException | IOException e) {
            throw new XenonException(getName(), "Could not set current working directory", e);
        }
    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {

        if (location == null || location.isEmpty()) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Location may not be empty");
        }

        if (credential == null) {
            throw new InvalidCredentialException(getName(), "Credential may not be null.");
        }

        if (!(credential instanceof CertificateCredential || credential instanceof DefaultCredential)) {
            throw new InvalidCredentialException(getName(), "Credential type not supported.");
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        GridFTPClient client = connectClient(location, credential, properties);

        String cwd = null;

        try {
            cwd = getCurrentWorkingDirectory(client, location);
        } catch (Exception e) {
            try {
                client.close();
            } catch (Exception ex) {
                // ignored
            }
            throw e;
        }

        return new GridFTPFileSystem(getNewUniqueID(), ADAPTOR_NAME, location, credential, client, new Path(cwd), (int) bufferSize, this, xp);
    }
}
