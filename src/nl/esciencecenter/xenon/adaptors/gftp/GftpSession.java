/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.globus.ftp.ChecksumAlgorithm;
import org.globus.ftp.DataChannelAuthentication;
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
 * GftpSession manages a (thread-safe) Grid FTP Session for a single server (host:port).
 * <p>
 * This class is thread-safe as opposed to the Globus GridFTPClient which can not be used multi-threaded. This because the
 * GridFTPClient client holds a state. For multi-threaded access to the same GFTP server it is recommended to use multiple
 * GftpSession objects as GFTP Servers are optimized for parallel access.<br>
 * This class can be used multi-threaded.
 * 
 * @author Piter T. de Boer
 */
public class GftpSession {

    private static final Logger logger = LoggerFactory.getLogger(GftpSession.class);

    public static class GftpSessionOptions {

        public boolean protocol_v1 = false;

        /** Use Passive GFTP Mode. Default is true. */
        public boolean usePassiveMode = true;

        /** Either GFTP V1 or SRM Grid FTP Server. */
        public boolean useBlindMode = false;

        /** Explicit list hidden files on Grid FTP V1 servers. */
        public boolean gftp1ListHiddenFiles = true;

        /** Always use DataChannel Authentication or throw an exception if not supported by the remote GridFTP server. */
        public boolean enforceDCAU = false;

        /** Perform extra client state test to ensure valid connections. */
        public boolean optCheckClientState = true;

        /** Reconnect client after exceptions to ensure continues access. */
        public boolean autoConnectAfterException = true;

        @Override
        public String toString() {
            return "GftpSessionOptions:[protocol_v1=" + protocol_v1 + ", usePassiveMode=" + usePassiveMode + ", useBlindMode="
                    + useBlindMode + ", gfpt1ListHiddenFiles=" + gftp1ListHiddenFiles + ", enforceDCAU=" + enforceDCAU + "]";
        }
    }

    // ---
    // Instance 
    // --- 

    private GftpAdaptor adaptor = null;

    private GlobusProxyCredential credential = null;

    private FeatureList features = null;

    private GftpLocation location = null;

    private GridFTPClient client = null;

    private Object clientMutex = new Object();

    private GftpSessionOptions options = new GftpSessionOptions();

    /**
     * Default to true, server will update actual use of DCAU when checking features after connecting. If
     * GftpSessionOptions.enforceDCAU==true, Exceptions will be thrown when this is not supported.
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

        synchronized (clientMutex) {
            this.client = this.createGFTPClient();
            this.entryPath = pwd();
        }

        logger.info("connect(): connected to remote path (user home=) {}", entryPath);

        updateFeatureList();
    }

    protected void updateFeatureList() throws XenonException {

        try {

            synchronized (clientMutex) {
                // Check Grid FTP feature list:
                this.features = client.getFeatureList();
                this.useDataChannelAuthentication = features.contains(FeatureList.DCAU);
            }

            //this.useMLST = features.contains("mslt");
            //this.useMLSD = features.contains("msld");

            if (logger.isInfoEnabled()) {
                logger.info(" - Server Feature list:{}       = {}.", this, GftpUtil.toString(features));
                logger.info(" - useDataChannelAuthentication = {}", useDataChannelAuthentication);
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
     * Idempotent close() method, can be called multiple times.
     * 
     * @param silent
     *            - if an Exception is thrown during the close, the Exception will be ignored.
     * @throws XenonException
     *             only if (silent==false) and an exception occurred during the close.
     */
    public void autoClose(boolean silent) throws XenonException {

        synchronized (clientMutex) {
            if (client != null) {
                try {
                    client.close();
                } catch (ServerException | IOException e) {
                    client = null;
                    if (silent) {
                        // silent mode: log exception, continue execution. 
                        logger.warn("Exception when closing previous Grid FTP Client:" + e.getMessage(), e);
                    } else {
                        // not silent
                        throw new XenonException(adaptor.getName(), "Exception during close:" + e.getMessage(), e);
                    }
                }
            }

            client = null;
        }
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
     * Idempotent Close GridFTPClient. Does not throw exceptions.
     */
    public void closeGFTPClient(GridFTPClient gftpClient) {
        try {
            gftpClient.close();
        } catch (Exception e) {
            logger.warn("Exception when closing GridFTPClient:" + gftpClient);
        }

    }

    /**
     * Check whether connection is still valid and (optionally) reconnect to ensure valid Grid FTP connection.<br>
     * If the connection is already closed by one of the close() methods this method will always result in an Exception.
     * 
     * @throws XenonException
     *             if connection is not valid anymore and option autoConnectAfterException==false.
     */
    private void assertConnected() throws XenonException {

        if (client == null) {
            throw new XenonException(adaptor.getName(), "assertConnected(): Grid FTP Client is not connected!");
        }

        // Check connection state here (for debugging and keep-alive option).  
        if (options.optCheckClientState) {

            //  
            try {

                synchronized (clientMutex) {
                    String pwd = client.getCurrentDir();
                    logger.debug("assertConnected():pwd= {} ", pwd);
                }
            } catch (ServerException | IOException e) {

                if (options.autoConnectAfterException) {
                    logger.info("assertConnected(): reconnecting to remote server after exception: {} ", this);
                    resetConnection();
                } else {
                    throw new XenonException(adaptor.getName(), "assertConnected(): Client in error state:" + e.getMessage(), e);
                }
            }
        }
    }

    // ========================================================================
    // Options 
    // ========================================================================

    public boolean useDataChannelAuthentication() {
        return useDataChannelAuthentication;
    }

    /**
     * @return whether this client should use Passive Mode transfers, which means the remote server has to be set to Passive Mode
     *         for each data channel transfer.
     */
    public boolean usePassiveMode() {
        return options.usePassiveMode;
    }

    /**
     * @return whether this client should use Active transfer, which means the remote server has to be set to Active mode for each
     *         data channel
     */
    public boolean useActiveMode() {
        return (options.usePassiveMode == false);
    }

    /**
     * Set transfer mode of Server to Passive. This is the default behavior of the GridFTP Client. <br>
     * This means that per data transfer the remote server will create a datachannel port and the local client will connect to
     * that data channel. The remote datachannel port must be accessible by the client.
     */
    public void setPassiveMode() {
        options.usePassiveMode = true;
    }

    /**
     * Set transfer mode of the Server to Active. <br>
     * This means the remote server will be the active party and connect back to the local client. The local (client) port must be
     * accessible by the remote server.
     */
    public void setActiveMode() {
        options.usePassiveMode = false;
    }

    /**
     * <strong>Note:</strong>: GridFTP V1.x and SRM Support.<br>
     * Support for (OLD) SRM Grid FTP server which basically do not allow listing of remote filepaths at all. If useBlindMode() is
     * true it is assumed that all filepaths exists and dummy mslx entries will be created.
     * 
     * @return whether to use Blind Mode GridFTP.
     */
    public boolean useBlindMode() {
        return options.useBlindMode;
    }

    /**
     * Whether to perform an extra list command to get hidden files. Some GridFTP servers don't list hidden files. Set this to
     * true to explicit list hidden files. This will result in some extra overhead since this might involve an extra list command.
     * 
     * @return whether to explicit list hidden files.
     */
    public boolean getExplicitListHidden() {
        return options.gftp1ListHiddenFiles;
    }

    // ========================================================================
    // Actual GridFTP methods  
    // ========================================================================

    /**
     * Create New Globus GridFTPClient using this Sessions configuration. Multiple GridFTPClients may be created for a single
     * GftpSession, but a single GridFTPCLient may never be shared by multiple threads !<br>
     * Create Multiple <code>GftpSession</code> instances to ensure save parallel access to a remote Grid FTP server.
     */
    protected GridFTPClient createGFTPClient() throws XenonException {
        // actual Grid FTP Client; 
        GridFTPClient newClient = null;
        int port = location.getPort();
        String hostname = location.getHostname();

        if (port <= 0) {
            port = GftpAdaptor.DEFAULT_PORT;
        }

        if (credential == null) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "No (Globus Proxy) Credentials supplied, please supply them");
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
                logger.warn("GftpSession(): Explicitly disabling dataChannelAuthentication for: {}", this);
                newClient.setLocalNoDataChannelAuthentication();
            }

            logger.debug("GftpSession(): new gftp session for host:{}:{}.", hostname, port);

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

    /**
     * @return current working directory.
     * @throws XenonException
     */
    public String pwd() throws XenonException {

        try {
            synchronized (clientMutex) {

                assertConnected();
                updatePassiveMode(false);
                String pwd = client.getCurrentDir();
                logger.debug("pwd() of {} = {}", this, pwd);
                return pwd;

            }
        } catch (ServerException | IOException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "pwd() of server:" + this + "failed\n." + e.getMessage(), e);
        }
    }

    /**
     * Create directory.
     * 
     * @param dirpath
     *            - directory to create on the remote server
     * @param ignoreExisting
     *            - if true an existing directory will be ignored, else an Exception will be thrown if the (remote) directory
     *            already exists.
     * @throws XenonException
     */
    public void mkdir(String dirpath, boolean ignoreExisting) throws XenonException {
        logger.debug("mkdir(): {}", dirpath);

        if (exists(dirpath)) {
            if (ignoreExisting) {
                return; // already exists: ok. 
            } else {
                throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "Path already exists:" + dirpath);
            }
        }
        try {
            synchronized (clientMutex) {

                assertConnected();
                // mkdir doesn't need data channel
                this.updatePassiveMode(false);
                client.makeDir(dirpath);
            }
        } catch (ServerException | IOException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "mkdir() failed for path:" + dirpath + "\n" + e.getMessage(), e);
        }
    }

    /**
     * Remove remote file.
     * 
     * @param filepath
     *            - path of the file to be removed.
     * @throws XenonException
     */
    public void rmfile(RelativePath filepath) throws XenonException {
        rmpath(filepath.getParent().getAbsolutePath(), filepath.getFileNameAsString(), false);
    }

    /**
     * Remove remote directory. Directory must be empty.
     * 
     * @param dirpath
     * @throws XenonException
     */
    public void rmdir(RelativePath dirpath) throws XenonException {
        rmpath(dirpath.getParent().getAbsolutePath(), dirpath.getFileNameAsString(), true);
    }

    /**
     * Delete single file or empty sub-directory from parent directory.
     * 
     * @param parentdir
     *            - Accessible parent directory.
     * @param basename
     *            - File or sub-directory name.
     * @param isDirectory
     *            - whether to delete a file or sub-directory.
     * @throws XenonException
     */
    public void rmpath(String parentdir, String basename, boolean isDirectory) throws XenonException {

        try {
            synchronized (clientMutex) {

                assertConnected();
                // changedir/delete doesn't need data channel:
                updatePassiveMode(false);
                // first CD into directory, this also serves as extra permission check. (dir must be 'accessible').
                client.changeDir(parentdir);

                if (isDirectory) {
                    client.deleteDir(basename);
                } else {
                    client.deleteFile(basename);
                }
            }

        } catch (ServerException | IOException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "rmpath() failed for:" + parentdir + "/" + basename + "\n"
                    + e.getMessage(), e);
        }
    }

    public void rename(String absolutePath, String otherPath) throws XenonException {

        try {
            synchronized (clientMutex) {
                assertConnected();
                updatePassiveMode(false); // no data channel needed. 
                client.rename(absolutePath, otherPath);
            }

        } catch (ServerException | IOException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "rename() failed for:" + absolutePath + " -> " + otherPath + "\n"
                    + e.getMessage(), e);
        }
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

    public String getChecksum(String algorithm, long offset, long length, String filePath) throws XenonException {

        if (algorithm == null || algorithm.equalsIgnoreCase("")) {
            return null;
        }

        ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithm(algorithm);

        try {
            synchronized (this.clientMutex) {
                return client.checksum(checksumAlgorithm, offset, length, filePath);
            }
        } catch (Exception e) {
            throw new XenonException(adaptor.getName(), e.getMessage(), e);
        }
    }

    /**
     * Grid FTP 1.0 compatible method. Fetches file size using FTP (1.0) method 'getSize' and does not stat remote file entry
     * using mlst method.
     * 
     * @param filePath
     * @return file size or -1 if unknown or not applicable.
     * @throws XenonException
     */
    public long getSize(String path) throws XenonException {
        long size = 0;

        try {
            synchronized (clientMutex) {
                assertConnected();
                updatePassiveMode(false); // no data channel needed. 
                size = this.client.getSize(path);
                if (size < -1)
                    size = -1; // 
                return size;
            }
        } catch (Exception e) {
            throw new XenonException(adaptor.getName(), "Couldn't get size of file (" + this.getHostname() + "):" + path
                    + "\nError:" + e.getMessage(), e);
        }
    }

    // ========================================================================
    // InputStream//OutputStream 
    // ========================================================================

    /**
     * Create new InputStream.
     * <p>
     * This method doesn't not block the current GridFTPClient so multiple InputStreams may be created to the same server.
     * 
     * @param filePath
     * @param append
     * @return
     * @throws XenonException
     */
    protected GridFTPInputStream createInputStream(String filepath) throws IOException, XenonException {

        logger.debug("createInputStream(): {}", filepath);

        // Create stand-alone GridFTPInputStream. 
        // Make sure to call close() on this stream ! 

        GridFTPInputStream inps;

        try {

            // ---
            // Create Custom GFTP Stream for multi-threaded access to remote files. 
            // This way this method will not block the GftpSession while the file is read. 
            // ---

            if (useActiveMode()) {
                logger.error("FIXME: createInputStream(): Active Mode not tested!");
            }

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

    /**
     * Create new OutputStream.
     * <p>
     * This method doesn't not block the current GridFTPClient so multiple OutputStreams to the same server may be created
     * simultaneously.
     * 
     * @param filePath
     *            - File path to open for writing.
     * @param append
     *            - whether to start writing at the end of the file. If false the stream will start writing at the beginning of
     *            the file.
     * @return Outputstream to the remote file.
     * @throws XenonException
     */
    protected OutputStream createOutputStream(String filePath, boolean append) throws XenonException {
        logger.info("createOutputStream(): (append={}):{} ", append, filePath);

        GridFTPOutputStream outps;

        try {

            // ---
            // Create Custom GFTP Stream for multi-threaded access to remote files. 
            // This way this method will not block the GftpSession while the file is written to. 
            // ---

            if (useActiveMode()) {
                logger.error("FIXME: createOutputStream(): Active Mode not tested!");
            }

            outps = new GridFTPOutputStream(getValidGSSCredential(), getAuthorization(), getHostname(), getPort(), filePath,
                    append, usePassiveMode(), // always passive ?
                    Session.TYPE_IMAGE, false);

            // Wrap ? 
            return outps;

        } catch (Exception e) {
            logger.error("createOutputStream(): {} => Exception={}", filePath, e);
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    // ========================================================================
    // MLST/MLSD commands 
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

        try {

            synchronized (clientMutex) {

                String pathStr = filepath.getAbsolutePath();
                MlsxEntry entry = null;

                assertConnected();
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

    /**
     * Grid FTP V1.0 compatible 'mlst' method. Tries to get the FileInfo and converts it to a MlsxEntry object. Method is also
     * needed for some old SRM Servers.
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

            boolean exists = false;

            // update mode for exists().
            synchronized (clientMutex) {
                assertConnected();
                updatePassiveMode(false);
                exists = client.exists(pathStr);
            }

            if (exists == false) {

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
     * Create simple directory or file MslxEntry without any other file attributes. Used for Grid FTP V1.
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

    /**
     * Convert FTP FileInfo object to MslxEntry.
     * 
     * @param path
     *            - file or directory path
     * @param basename
     *            - lastpart of the filename
     * @param finfo
     *            - actual FileInfo Object
     * @return MslxEntry
     * @throws XenonException
     */
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
     * Update and check status of Passive/Active Mode connection.
     * <p>
     * Some GridFTP commands setup a Data Channel. Before setting up this channel either Passive or Active mode has to be
     * specified.
     * <p>
     * DataChannel methods are: msld, get, put and variants of the methods.
     * 
     * @param dataChannelCommand
     *            - if true then update Active/Passive mode for a DataChannel Command.
     * @throws XenonException
     */
    private void updatePassiveMode(boolean dataChannelCommand) throws XenonException {
        try {
            // do no use assertConnected() here...
            synchronized (clientMutex) {

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

    /**
     * Performs list directory command ('mlsd') on remote Grid FTP Server.<br>
     * This method creates a custom GftpClient and does not block the curent <code>GftpSession</code>. <br>
     * This way multiple directories can be listed in parallel on the same server.
     * 
     * @param dirpath
     *            - directory path to list
     * @return Contents of remote directory.
     * @throws XenonException
     */
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
            // Create Custom GFTP client for multi-threaded listing of remote directories. 
            // This way this method will not block the GftpSession. 
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

            if (listV.size() <= 0) {
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
     * GridFTP V1.0 compatible list command which mimics 'mlsd'. <br>
     * Performs different backward compatible methods to list the remote contents.
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

    // =================================
    // Third party transfer/copy methods  
    // =================================

    /**
     * Copy remote file but on same server. This can be done with 3rd party copying from and to the same server.
     * 
     * @param sourcePath
     *            - source file to copy
     * @param targetFilepath
     *            - target file path to copy to on the same server
     * @throws XenonException
     */
    public void activeRemoteCopy(RelativePath sourcePath, RelativePath targetFilepath) throws XenonException {
        active3rdPartyTransfer(this, sourcePath.getAbsolutePath(), this, targetFilepath.getAbsolutePath(),false);
    }

    /**
     * Perform active third party transfer and initiate active file transfer from this (source) server.<br>
     * This server will be the active party. If this is not possible use the reverse method. 
     * 
     * @param sourcePath
     *            - file path on this GridFTP server
     * @param remoteServer
     *            - remote GridFTP server.
     * @param targetFilepath
     *            - remote File path to copy to
     * @throws XenonException
     */
    public void active3rdPartyTransfer(RelativePath sourcePath, GftpSession remoteServer, RelativePath targetFilepath, boolean targetServerIsActive)
            throws XenonException {
        active3rdPartyTransfer(this, sourcePath.getAbsolutePath(), remoteServer, targetFilepath.getAbsolutePath(),targetServerIsActive);
    }

    /**
     * Initiate 3rd party transfer from sourceServer to targetServer. This is a static method as transfers must be done between
     * two authenticated GridFTP Sessions.
     * 
     * @param sourceServer
     *            - source GFTP FileSystem: this server is the Active Party during transfer.
     * @param sourceFilepath
     *            - source path of file
     * @param targetServer
     *            - target GFTP FileSystem: this server is the Passive Party during transfer.
     * @param targetFilepath
     *            - target path of file
     * @return new Target VFile object
     * @throws VrsException
     */
    protected static void active3rdPartyTransfer(GftpSession sourceServer, String sourceFilepath, GftpSession targetServer,
            String targetFilepath,boolean reverseActiveMode) throws XenonException {

        String transferInfoStr = "third party copy from " + sourceServer.getHostname() + ":" + sourceFilepath + " to "
                + targetServer.getHostname() + ":" + targetFilepath;
        logger.info("> Performing:{}", transferInfoStr);
        //monitor.logPrintf("Performing: %s\n",transferInfoStr); 

        // Multi threaded support: Create private GridFTP clients for each location:  
        GridFTPClient privateSourceClient = sourceServer.createGFTPClient();
        GridFTPClient privateTargetClient = targetServer.createGFTPClient();       
        
        // Grid FTP 1.0 compatibility
        // Both must support DCAU. If one of them doesn't: disable at the other ! 
        boolean sourceDCAU = sourceServer.useDataChannelAuthentication();
        boolean destDCAU = targetServer.useDataChannelAuthentication();

        logger.debug(" - GridFTP 3rd party transfer source Server DCAU = {}", sourceDCAU);
        logger.debug(" - GridFTP 3rd party transfer dest   Server DCAU = {}", destDCAU);

        long sourceSize = -1;
        try {
            sourceSize = sourceServer.getSize(sourceFilepath);
        } catch (Exception e) {
            // SRM 'BlindMode' Patch: not always possible to fetch file size from transport URLs !
            logger.warn("Couldn't determine size of source file {}:{}", sourceFilepath, e);
        }

        // Both must be true or false. 
        if (sourceDCAU != destDCAU) {
            logger.warn("Warning: Grid FTP Data Channel Authentication mismatch between source: {}  and destination: {} ",
                    targetServer.getHostname(), sourceServer.getHostname());
        }

        // Disable DCAU from source to target. 
        if (destDCAU == false) {
            try {
                privateSourceClient.setLocalNoDataChannelAuthentication();
                privateTargetClient.setDataChannelAuthentication(DataChannelAuthentication.NONE);
                logger.warn("Disabled DataChannel Authentication for target server:{} => {}", targetServer);
            } catch (Exception e) {
                // API might not be supported, continue anyway. 
                logger.error("Exception disabling DataChannel Authentication for target server:{} => {}", targetServer, e);
            }
        }

        // Disable DCAU from target to source (if needed).  
        if (sourceDCAU == false) {
            try {
                privateTargetClient.setLocalNoDataChannelAuthentication();
                privateSourceClient.setDataChannelAuthentication(DataChannelAuthentication.NONE);
                logger.warn("Disabled DataChannel Authentication for source server:{} => {}", sourceServer);
            } catch (Exception e) {
                // API might not be supported, continue anyway. 
                logger.error("Exception disabling DataChannel Authentication for source server:{} => {}", sourceServer, e);
            }
        }

        Throwable transferEx = null;
        GftpTransferMonitor listener = new GftpTransferMonitor(targetServer, targetFilepath, sourceSize);

        try {
            // to be tested: 
            if (reverseActiveMode==false)
            {
                // The source server is active party, should be default mode for 3rd party transfers. 
                //privateSourceClient.setLocalPassive();
                //privateSourceClient.setActive(); 
            }
            else // if (reverseActiveMode)
            {
                logger.info("Switching active/passive mode between servers. Target Server is active party");
                // reverse polarity of transfer, this means target server should connect back to source server. 
                privateSourceClient.setPassive(); 
                privateSourceClient.setLocalActive();
                privateTargetClient.setLocalPassive(); 
                privateTargetClient.setActive(); 
            }
            
            boolean append = false;
            // initiate transfer from active source to passive destination  
            privateSourceClient.transfer(sourceFilepath, privateTargetClient, targetFilepath, append, listener);

        } 
        catch (Throwable e) {
            listener.setException(e);
            logger.error("Exception during 3rd party transfer:{}", e);
            // retry ? 
            transferEx = e;

            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "Couldn't perform " + transferInfoStr + ". Error="
                    + transferEx.getMessage(), transferEx);
        } finally {
            // CLEANUP !
            sourceServer.closeGFTPClient(privateSourceClient);
            targetServer.closeGFTPClient(privateTargetClient);
        }

        logger.info("GridFTP: Finished 3rd party transfer.\n");
        return; // ok 
    }

    /**
     * Set whether DataChannelAuthentication must be used. This value is automatically set after connecting to a remote server.
     * Change this value only after connecting to override the default settings.
     */
    public void setUseDataChannelAuthentication(boolean value) {
        this.useDataChannelAuthentication = value;
    }

    /**
     * Returns whether DataChannelAuthentication will be used. This value is automatically set after connecting to a remote
     * server. First connect to a remote server then check this value whether DCAU is supported.
     */
    public boolean getUseDataChannelAuthentication() {
        return useDataChannelAuthentication;
    }

    // ---
    // Miscellaneous methods 
    // ---

    @Override
    public String toString() {
        return "GftpSession [location=" + location + ",features=" + features + ", useDCAU=" + useDataChannelAuthentication
                + ", entryPath=" + entryPath + ", options=" + options + "]";
    }

}
