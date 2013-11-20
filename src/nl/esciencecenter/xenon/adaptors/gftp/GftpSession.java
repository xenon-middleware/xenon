package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.PropertyTypeException;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.RelativePath;

import org.globus.ftp.FeatureList;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.io.streams.GridFTPInputStream;
import org.globus.io.streams.GridFTPOutputStream;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed GFTP Client Session to a single host+port. This class is thread safe as the Globus GridFTPClient can not be used
 * multi-threaded. This because the GridGTP client hold a state.
 * 
 * @author Piter T. de Boer
 */
public class GftpSession {

    private static final Logger logger = LoggerFactory.getLogger(GftpSession.class);

    public static class GftpSessionOptions {

        public boolean protocol_v1 = false;

        public boolean usePassiveMode = true;

        public boolean useBlindMode = false;

        public boolean explicitListHiddenFiles = true;

        public boolean enforceDCAU = false;

        public boolean optCheckClientState = true;

        public boolean autoConnectAfterException = true;

        @Override
        public String toString() {
            return "GftpSessionOptions:[protocol_v1=" + protocol_v1 + ", usePassiveMode=" + usePassiveMode + ", useBlindMode="
                    + useBlindMode + ", explicitListHiddenFiles=" + explicitListHiddenFiles + ", enforceDCAU=" + enforceDCAU
                    + "]";
        }

    }

    private GftpAdaptor adaptor = null;

    private GlobusProxyCredential credential = null;

    private FeatureList features = null;

    private GftpLocation location = null;

    private GridFTPClient client = null;

    private GftpSessionOptions options = new GftpSessionOptions();

    /**
     * Default to true, server will update actual use of DCAU when checking features after connecting.
     */
    private boolean useDataChannelAuthentication = true;

    private String entryPath;

    public GftpSession(GftpAdaptor gftpAdaptor, GftpLocation location, GlobusProxyCredential credential,
            XenonProperties properties) throws XenonException {

        this.location = location;
        this.adaptor = gftpAdaptor;
        this.credential = credential;
        updateProperties(properties);

    }

    private void updateProperties(XenonProperties properties) throws XenonException {
        if (properties == null) {
            return;
        }

        try {

            options.useBlindMode = properties.getBooleanProperty(GftpAdaptor.USE_BLIND_GFTP);
            options.usePassiveMode = properties.getBooleanProperty(GftpAdaptor.USE_PASSIVE_MODE);
            options.enforceDCAU = properties.getBooleanProperty(GftpAdaptor.ENFORCE_DATA_CHANNEL_AUTHENTICATION);
            options.protocol_v1 = properties.getBooleanProperty(GftpAdaptor.USE_GFTP_V1);

            logger.info("GridOptions= {}", options);

        } catch (UnknownPropertyException | PropertyTypeException | InvalidPropertyException e) {
            throw e; //new XenonException(GftpAdaptor.ADAPTOR_NAME,e.getMessage(),e);
        }

    }

    public void connect(boolean closePreviousClient) throws XenonException {

        if (client != null) {
            if (closePreviousClient == false) {
                return; // keep already connected Grid FTP Client. 
            } else {
                this.autoClose(true);
            }
        }

        this.client = this.createGFTPClient();

        this.entryPath = pwd();

        logger.info("connect(): connected to remote path (user home=) {}", entryPath);

        updateFeatureList();
    }

    protected void updateFeatureList() throws XenonException {
        // Check Grid FTP feature list: 
        try {
            this.features = client.getFeatureList();
            this.useDataChannelAuthentication = features.contains(FeatureList.DCAU);

            if (logger.isInfoEnabled()) {
                logger.info(" - Server Feature list:{}         = {}.", this, GftpUtil.toString(features));
                logger.info(" - usingDataChannelAuthentication = {}", useDataChannelAuthentication);
            }

            if ((this.options.enforceDCAU == true) && (useDataChannelAuthentication == false)) {
                autoClose(true);
                throw new XenonException(GftpAdaptor.ADAPTOR_NAME,
                        "Enforce Data Channel Authentication (forceDCAU) is enabled, but remote server does not support DCAU for server:"
                                + this);
            }

        } catch (ServerException | IOException e) {
            throw new XenonException(adaptor.getName(), e.getMessage(), e);
        }
    }

    public void disconnect() throws XenonException {
        autoClose(false);
    }

    public boolean isConnected() {
        return (this.client != null);
    }

    /**
     * Idempotent close() method, Can be called multiple times.
     * 
     * @param silent
     *            - if an exception is thrown during the close, this exception will be ignored.
     * @throws XenonException
     *             only if (silent==false) and an exception occurred during the close.
     */
    public void autoClose(boolean silent) throws XenonException {
        if (client != null) {
            try {
                client.close();
            } catch (ServerException | IOException e) {

                client = null;

                if (silent) {
                    // log exception, continue execution. 
                    logger.warn("Exception when closing previous Grid FTP Client:" + e.getMessage(), e);
                } else {
                    // not silent
                    throw new XenonException(adaptor.getName(), "Exception during close:" + e.getMessage(), e);
                }
            }
        }

        client = null;
    }

    public void resetConnection() throws XenonException {

        this.connect(true);
    }

    public String getHostname() {
        return this.location.getHostname();
    }

    public int getPort() {
        return this.location.getPort();
    }

    /**
     * Idempotent Close GridFTPClient. Does not throw exception
     */
    public void closeGFTPClient(GridFTPClient client) {
        try {
            client.close();
        } catch (Exception e) {
            logger.warn("Exception when closing GridFTPClient:" + client);
        }

    }

    // ========================================================================
    // Options 
    // ========================================================================

    public boolean useDataChannelAuthentication() {
        return useDataChannelAuthentication;
    }

    public boolean usePassiveMode() {
        return options.usePassiveMode;
    }

    public boolean useActiveMode() {
        return (options.usePassiveMode == false);
    }

    /**
     * <strong>Note:</strong>: GridFTP V1.x and SRM Support.<br>
     * Support for (OLD) SRM Grid FTP server which basically do not allow listing of remote filepaths at all. If useBlindMode() is
     * true it is assumed that all filepaths exists and dummy mslx entries will be created.
     * 
     * @return Wether to use Blind Mode GridFTP.
     */
    public boolean useBlindMode() {
        return options.useBlindMode;
    }

    /**
     * Whether to perform an extra list command to get hidden files. Some GridFTP servers don't list hidden files. Set this to
     * true to explicit list hidden files. This will result in some extra overhead since this might involve an extra list command.
     * 
     * @return - whether to explicit list hidden files.
     */
    public boolean getExplicitListHidden() {
        return options.explicitListHiddenFiles;
    }

    // ========================================================================
    // Actual GridFTP methods  
    // ========================================================================

    public String pwd() throws XenonException {
        assertConnected();
        try {
            synchronized (client) {
                this.updatePassiveMode(false);
                return client.getCurrentDir();
            }
        } catch (ServerException | IOException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "pwd() of server:" + this + "failed\n." + e.getMessage(), e);
        }
    }

    public void mkdir(String dirpath, boolean ignoreExisting) throws XenonException {
        logger.debug("mkdir(): {}", dirpath);

        if (exists(dirpath)) {
            if (ignoreExisting) {
                return;
            } else {
                throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "Path already exists:" + dirpath);
            }
        }

        assertConnected();
        synchronized (client) {
            // mkdir doesn't need data channel
            this.updatePassiveMode(false);

            try {
                client.makeDir(dirpath);
            } catch (ServerException | IOException e) {
                throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "mkdir() failed for path:" + dirpath + "\n" + e.getMessage(),
                        e);
            }
        }
    }

    public void rmfile(RelativePath path) throws XenonException {
        rmpath(path.getParent().getAbsolutePath(), path.getFileNameAsString(), false);
    }

    public void rmdir(RelativePath path) throws XenonException {
        rmpath(path.getParent().getAbsolutePath(), path.getFileNameAsString(), true);
    }

    /**
     * Delete single file or empty subdirectory from parent directory
     * 
     * @param parentdir
     *            - accessable parent directory
     * @param basename
     *            - file or sub directory name
     * @param isDirectory
     *            - whether to delte a file or sub directory.
     * @throws XenonException
     */
    public void rmpath(String parentdir, String basename, boolean isDirectory) throws XenonException {
        assertConnected();

        synchronized (client) {
            // changedir/delete doesn't need data channel:
            updatePassiveMode(false);

            // cd to directory. This also acts as an extra permissions check.
            try {
                client.changeDir(parentdir);

                if (isDirectory) {
                    client.deleteDir(basename);
                } else {
                    client.deleteFile(basename);
                }

            } catch (ServerException | IOException e) {
                throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "rmpath() failed for:" + parentdir + "/" + basename + "\n"
                        + e.getMessage(), e);
            }
        }
    }

    public void rename(String absolutePath, String otherPath) throws XenonException {
        logger.error("FIXME: rename(): {} -> {} ", absolutePath, otherPath);
    }

    public List<MlsxEntry> list(RelativePath dirpath) throws XenonException {
        return mlsd(dirpath);
    }

    protected GSSCredential getValidGSSCredential() throws XenonException {
        return this.credential.createGSSCredential();
    }

    protected Authorization getAuthorization() {
        return client.getAuthorization();
    }

    /**
     * Create New Globus GridFTPClient using this Sessions default configuration. Multiple Grid FTP Clients maybe created for a
     * single GftpSession, but a single GridFTPCLient may never be shared by multiple Threads !
     */
    protected GridFTPClient createGFTPClient() throws XenonException {
        // actual Grid FTP Client; 
        GridFTPClient newClient = null;
        int port = location.getPort();
        String hostname = location.getHostname();

        if (port <= 0) {
            port = GftpAdaptor.DEFAULT_PORT;
        }

        // --- todo: 
        // update firewall portrange for Globus:
        // setSystemProperty("org.globus.tcp.port.range",GftpConfig.getFirewallPortRangeString());

        try {
            GSSCredential cred = credential.createGSSCredential();

            if (cred == null) {
                logger.warn("Warning: NULL Credentials.");
            }

            newClient = new GridFTPClient(hostname, port);

            newClient.authenticate(cred);

            // Set default to (Binary) Image as opposed to "ASCII". 
            newClient.setType(Session.TYPE_IMAGE);

            // Note: This is disabled for old Grid FTP servers, for example some (grid) SRM Servers ! 
            boolean ldca = useDataChannelAuthentication();

            if (ldca == false) {
                newClient.setLocalNoDataChannelAuthentication();
            }

            logger.debug("GftpSession(): new gftp session:{}:{}.", hostname, port);

        } catch (UnknownHostException e) {
            throw new XenonException(adaptor.getName(), "Unknown hostname or server:" + e.getMessage(), e);
        } catch (ConnectException e) {
            throw new XenonException(adaptor.getName(), "Connection error when connecting to:" + this + ".\nReason="
                    + e.getMessage(), e);
        } catch (Exception e) {
            throw new XenonException(adaptor.getName(), e.getMessage(), e);

        }

        return newClient;
    }

    // ========================================================================
    // InputStream//OutputStream 
    // ========================================================================

    protected GridFTPInputStream createInputStream(String filepath) throws IOException, XenonException {

        logger.debug("createInputStream(): {}", filepath);

        // Create stand-alone GridFTPInputStream. 
        // Make sure to call close() on this stream ! 

        GridFTPInputStream inps;

        try {
            inps = new GridFTPInputStream(getValidGSSCredential(), getAuthorization(), this.getHostname(), this.getPort(),
                    filepath, usePassiveMode(), // always passive ?
                    Session.TYPE_IMAGE, useDataChannelAuthentication());

            // monitor(?): this.registerInputStream(inps); 
            return inps;
        } catch (Exception e) {
            logger.error("createInputStream(): {} => Exception={}", filepath, e);
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

    }

    protected OutputStream createOutputStream(String filePath, boolean append) throws XenonException {
        logger.info("createOutputStream(): (append={}):{} ", append, filePath);

        GridFTPOutputStream outps;

        try {
            // Create custom GridFTPOutputStream. 

            outps = new GridFTPOutputStream(getValidGSSCredential(), getAuthorization(), getHostname(), getPort(), filePath,
                    append,// do not append !
                    usePassiveMode(), // always passive ?
                    Session.TYPE_IMAGE, false);

            // Wrap ? 
            return outps;

        } catch (Exception e) {
            logger.error("createOutputStream(): {} => Exception={}", filePath, e);
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    // ========================================================================
    // MLST/MSLD commands 
    // ========================================================================

    public boolean exists(String path) throws XenonException {
        return (mlst(path) != null);
    }

    public MlsxEntry mlst(String pathStr) throws XenonException {
        return mlst(new RelativePath(pathStr));
    }

    public MlsxEntry mlst(RelativePath filepath) throws XenonException {
        logger.debug("mlst:{}", filepath);

        if (options.protocol_v1 == true) {
            return fakeMlst(filepath);
        }

        assertConnected();

        try {
            String pathStr = filepath.getAbsolutePath();

            MlsxEntry entry = null;

            synchronized (client) {
                // Assert correct GridFTP modus: 
                updatePassiveMode(false);
                if (this.client.exists(pathStr) == false) {
                    // return NULL if path can not be statted. 
                    return null;
                }

                // Update to passive for DATA channel mslt
                updatePassiveMode(true);
                entry = client.mlst(pathStr);

                return entry;
            }
        } catch (Exception e) {
            throw new XenonException(adaptor.getName(), "mlst(): Could not stat path: " + filepath + ". Exception="
                    + e.getMessage(), e);
        }
    }

    private void assertConnected() throws XenonException {
        if (client == null) {
            throw new XenonException(adaptor.getName(), "assertConnected(): Grid FTP Client is not connected!");
        }

        // check connection state here (debugging and keep-alive option).  
        if (options.optCheckClientState) {
            synchronized (client) {
                try {
                    String pwd = client.getCurrentDir();
                    logger.debug("assertConnected():pwd= {} ", pwd);
                } catch (ServerException | IOException e) {
                    if (options.autoConnectAfterException) {
                        logger.info("assertConnected(): reconnecting to remote server after exception: {} ", this);
                        resetConnection();
                    } else {
                        throw new XenonException(adaptor.getName(), "assertConnected(): Client in error state:" + e.getMessage(),
                                e);
                    }
                }
            }
        }

    }

    /**
     * V1.0 compatible 'mlst' method. Tries to get the FileInfo and converts it to a MlsxEntry object. Method is also needed for
     * some old SRM Servers.
     */
    private MlsxEntry fakeMlst(RelativePath filepath) throws XenonException {
        logger.debug("fakeMlst():{}", filepath);

        boolean isRoot = false;

        String pathStr = filepath.getAbsolutePath();
        String basename = filepath.getFileNameAsString();
        String dirname = filepath.getParent().getAbsolutePath();

        logger.debug("fakeMlst():dirname,basename='{}','{}'", dirname, basename);

        // ----
        // Grid FTP V1.0 patch:  Directory "/" might not be supported. 
        // root directory hack: cd to "/" and perform stat of ".";
        // ---- 

        if (pathStr.compareTo("/") == 0) {
            basename = ".";
            dirname = "/";
            isRoot = true;
        }

        try {
            // update mode for exists(). 
            updatePassiveMode(false);
            if (client.exists(pathStr) == false) {
                logger.debug("fakeMlst(): path doet not exist: {}", filepath);

                // Old Grid FTP server do not support statting "/", since the client is connected, return 'dummy' entry. 
                if (isRoot) {
                    return this.createDummyMslx("/", true);
                }
                // Either Blind mode  SRM or V1 GridFTP: 
                if (useBlindMode()) {
                    logger.info("BLINDMODE: returning dummy file object!");
                    // blind mode: Create Dummy MSLX entry 
                    return this.createDummyMslx(pathStr, false);
                } else {
                    // no stat info is null, assume path doesn't exist. 
                    return null;
                }
            }

            // -----------------------------------------------------------------
            // Old Grid FTP V1 patch, create private (V1) Grid FTP Client here!
            // -----------------------------------------------------------------

            GridFTPClient gftpv1;
            gftpv1 = this.createGFTPClient();
            gftpv1.setPassiveMode(usePassiveMode());
            gftpv1.changeDir(dirname);

            // --------------------
            // Try 1:  use list(): 
            // --------------------
            Vector<?> files = null;

            //try
            {
                files = gftpv1.list(basename);
            }
            // catch (Exception e)
            // {
            //    logger.warn("list(): default Grid FTP (V1) list() command failed for path: {}",basename); 
            // }

            // ----------------------------------
            // Try 2:  use command "ls -d .*" ): 
            // ----------------------------------

            if ((files == null) || (files.size() <= 0)) {
                gftpv1.setPassiveMode(usePassiveMode());

                // ---
                // Gftp (V1) PATCH:
                // Must use ls -d .* for hidden files listing  
                // ---
                if (basename.startsWith(".")) {
                    files = gftpv1.list("-d .*");
                } else {
                    files = gftpv1.list();
                }
                closeGFTPClient(gftpv1);
                gftpv1 = null;
            }

            // Check if actual file entry is in returned file list: 
            if ((files == null) || (files.size() <= 0)) {
                logger.info("Grid FTP (V1) 'ls -d *' command failed for path: {}", dirname);
                return null;
            }

            for (Object o : files) {
                FileInfo finfo = (FileInfo) o;
                // Found! 
                if (finfo.getName().compareTo(basename) == 0) {
                    return createMslx(dirname, basename, finfo);
                }
            }

            // Since 'exists' returned true, this is an error
            // might be a bug in old v1.0 servers:
            logger.error("fakeMlst(): GridFTP (V1) server says file exists, but 'ls -d' didn't return it for: {}", filepath);

            // don't know what happened here: return null and assume file doesn't exist. 
            return null;

        } catch (Exception e) {
            throw new XenonException(adaptor.getName(), e.getMessage(), e);
        }
    }

    /**
     * Create simple directory or file Mslx without any other file attributes. Used for Grid FTP V1.
     * 
     * @throws XenonException
     */
    private MlsxEntry createDummyMslx(String filepath, boolean isDir) throws XenonException {
        String typestr = MlsxEntry.TYPE_FILE;

        if (isDir) {
            typestr = MlsxEntry.TYPE_DIR;
        }

        String mstr = "mslx=" + "unix.owner=;" + "unix.group=;" + "type=" + typestr + ";" + "size=0;" + "unix.mode=;" + " "
                + filepath;

        try {
            MlsxEntry entry = null;
            entry = new MlsxEntry(mstr);
            // extra Attributes ? :
            // entry.set(UNIX_MODE_STRING,finfo.getModeAsString());
            // entry.set(UNIX_MODE,finfo.getModeAsString());
            return entry;

        } catch (FTPException e) {
            throw new XenonException(adaptor.getName(), "createDummyMslx(). Failed to create MSLX entry for:" + filepath, e);
        }

    }

    private static MlsxEntry createMslx(String dirpath, String basename, FileInfo finfo) throws XenonException {
        // Example Mslx. Note space before filepath:
        // mslx=unix.owner=dexter;unix.mode=0755;size=4096;perm=cfmpel;type=dir;unix.group=admin;
        // unique=fd06-1ee8001; modify=20070402124136; /var/scratch/dexter

        String typestr = MlsxEntry.TYPE_FILE;
        // make sure LAST part of name is returned !

        String fullpath;

        if (dirpath == null) {
            fullpath = basename;
        } else {
            fullpath = dirpath + "/" + basename;
        }

        if (finfo.isDirectory() == true) {
            typestr = MlsxEntry.TYPE_DIR;
        }

        if (basename.compareTo(".") == 0) {
            typestr = MlsxEntry.TYPE_CDIR;
        }

        if (basename.compareTo("..") == 0) {
            typestr = MlsxEntry.TYPE_PDIR;
        }

        String mstr = "mslx=" + "unix.owner=;" + "unix.group=;" + "type=" + typestr + ";" + "size=" + finfo.getSize() + ";"
                + "unix.mode=" + finfo.getModeAsString() + ";"
                // note single space after attributes to indicate filepath 
                + " " + fullpath;

        MlsxEntry entry = null;

        try {
            entry = new MlsxEntry(mstr);
            logger.debug("createMslx(): converted String:{} to MlsxEntry:{} ", mstr, entry);

            // extra Attributes ? :
            // entry.set(UNIX_MODE_STRING,finfo.getModeAsString());
            // entry.set(UNIX_MODE,finfo.getModeAsString());
            return entry;
        } catch (FTPException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "createDummyMslx(): Failed to create MSLX entry for:" + fullpath,
                    e);
        }
    }

    /**
     * Update and check status of Passive Mode connection. Some GridFTP command settting up a Data Channel either Passive Mode or
     * Active Mode <strong>has</strong> to be choosen. DataChannel methods are: msld, get, put and variants of the methods.
     * 
     * @param dataChannelCommand
     *            - if true then update Active/Passive mode for a DataChannel Command.
     * 
     * @throws XenonException
     */
    private void updatePassiveMode(boolean dataChannelCommand) throws XenonException {
        try {
            synchronized (client) {

                if (usePassiveMode() == false) {
                    // Always update active mode.  
                    client.setPassiveMode(false);
                } else {
                    // Explicit setting passive mode must be done before opening a Data Channel and may never
                    // be performed twice. 
                    if (dataChannelCommand) {
                        client.setPassiveMode(true);
                    }
                }
            }
        } catch (ServerException e) {
            logger.error("ServerException:{}", e.getMessage());
            throw new XenonException(adaptor.getName(), "updatePassiveMode(): Exception:" + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Exception:{}", e.getMessage());
            throw new XenonException(adaptor.getName(), "updatePassiveMode(): Exception:" + e.getMessage(), e);
        }
    }

    public List<MlsxEntry> mlsd(String pathStr) throws XenonException {
        return mlsd(new RelativePath(pathStr));
    }

    public List<MlsxEntry> mlsd(RelativePath dirpath) throws XenonException {
        logger.debug("msld(): {}", dirpath);

        // Old GridfTP server or SRM GridFTP location which doesn't support listing of directories. 
        if (useBlindMode() == true) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "mlsd(): Remote server doesn't suport listing of directories:"
                    + this);
        }

        if (options.protocol_v1 == true) {
            return fakeMlsd(dirpath);
        }

        String dirPathStr = dirpath.getAbsolutePath();

        try {
            if (client.exists(dirPathStr) == false) {
                throw new NoSuchPathException(GftpAdaptor.ADAPTOR_NAME, "Directory doesn't exists:" + dirpath);
            }

            Vector<?> listV = null;

            // ---
            // multi-threaded directory listing: 
            // Create custom client and no not block (synchronized) this session. 
            // ---

            if (useActiveMode()) {
                logger.error("FIXME: Active Mode not tested!");
            }

            {
                GridFTPClient myclient = this.createGFTPClient();
                // explicit enable active/passive mode. 
                myclient.setPassiveMode(usePassiveMode());
                listV = myclient.mlsd(dirPathStr);
                this.closeGFTPClient(myclient);
            }


            if (listV.size() < -0) {
                return new Vector<MlsxEntry>(0);
            } else if (listV.size() > 0) {

                if (listV.get(0) instanceof MlsxEntry) {
                    return (Vector<MlsxEntry>) listV;
                }

            }

            // Conversion needed or is a dynamic cast to Vector<MslxEntry> enough ? 
            ArrayList<MlsxEntry> mlsxs = new ArrayList<MlsxEntry>();

            for (Object el : listV) {
                if (el instanceof MlsxEntry) {
                    mlsxs.add((MlsxEntry) el);
                } else {
                    throw new XenonException(GftpAdaptor.ADAPTOR_NAME,
                            "Internal Error: Invalid Object type, object is not MlsxEntry but:" + el.getClass());
                }
            }

            return mlsxs;
            
        } catch (Exception e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

    }

    /**
     * GridFTP V1.0 compatible list command which mimics mlsd. Performs different backward compatible methods to list the remote
     * contents.
     */
    private List<MlsxEntry> fakeMlsd(RelativePath dirpath) throws XenonException {
        logger.debug("fakeMlsd(): {}", dirpath);

        GridFTPClient gftpv1 = null;

        String dirPathStr = dirpath.getAbsolutePath();

        try {
            Vector<?> list1 = null;
            Vector<?> list2 = null;

            // old server: create private v1 client. 
            gftpv1 = this.createGFTPClient();
            gftpv1.setPassiveMode(usePassiveMode());

            if (gftpv1.exists(dirPathStr) == false) {
                throw new NoSuchPathException(GftpAdaptor.ADAPTOR_NAME, "Directory doesn't exists:" + dirPathStr);
            }
            // cd into path: 
            gftpv1.changeDir(dirPathStr);
            list1 = gftpv1.list();

            // ---
            // GridFTP 1.0 PATCH
            // The normal list doesn't always return hidden files.
            // Add extra query which returns hidden files as well
            // (and only hidden files) if no .files are in list1 and merge results. 
            // ---
            // update PASS mode

            boolean listHidden = getExplicitListHidden();

            if (listHidden == true) {
                if (list1 != null) {
                    for (Object o : list1) {
                        FileInfo finf = (FileInfo) o;
                        String name = finf.getName();
                        // current list already returned hidden files, skip extra list hidden files query.
                        if ((name != null) && (GftpUtil.isXDir(name) == false) && (name.startsWith(".") == true)) {
                            listHidden = false;
                            break;
                        }
                    }
                }
            }

            if (listHidden) {
                logger.debug("fakeMlsd(): Listing hidden files: {}", dirpath);
                gftpv1.setPassiveMode(usePassiveMode());
                // list hidden files:
                list2 = gftpv1.list("-d .*");
            }

            if ((list1 == null) || (list1.size() == 0)) {
                if ((listHidden == false) || ((listHidden == true) && ((list2 == null) || (list2.size() == 0)))) {
                    return null;
                }
            }

            // Merge lists in O(N+M) using hash value of name (=Hash sort).
            Hashtable<String, MlsxEntry> entrys = new Hashtable<String, MlsxEntry>();

            for (Object o : list1) {
                FileInfo finf = (FileInfo) o;
                addHashedMslx(entrys, dirPathStr, finf);
            }

            if (list2 != null) {
                for (Object o : list2) {
                    FileInfo finf = (FileInfo) o;
                    addHashedMslx(entrys, dirPathStr, finf);
                }
            }

            // hashtable to list:
            ArrayList<MlsxEntry> entrysv = new ArrayList<MlsxEntry>();

            for (Enumeration<String> keys = entrys.keys(); keys.hasMoreElements();) {
                String key = (String) keys.nextElement();
                entrysv.add(entrys.get(key));
            }
            return entrysv;

        } catch (Exception e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        } finally {
            this.closeGFTPClient(gftpv1);
        }
    }

    private static void addHashedMslx(Hashtable<String, MlsxEntry> entrys, String dirpath, FileInfo finf) throws XenonException {
        String basename = finf.getName();
        // check ? 
        entrys.put(basename, createMslx(dirpath, basename, finf));
    }

    // ---
    // Miscellaneous methods 
    // ---

    public String toString() {
        return "GftpSession:[location:" + this.location + ",connected:" + this.isConnected() + "]";
    }

}
