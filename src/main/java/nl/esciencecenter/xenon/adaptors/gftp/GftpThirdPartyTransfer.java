package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.Closeable;
import java.io.IOException;

import nl.esciencecenter.xenon.XenonException;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.HostPort;
import org.globus.ftp.exception.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated Grid FTP third party transfer Object.<br>
 * A GftpThirdPartyTransfer object manages a Third Party Transfer between two Grid FTP servers.
 * 
 * @author Piter T. de Boer.
 */
public class GftpThirdPartyTransfer implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(GftpThirdPartyTransfer.class);

    // ========
    // Instance
    // ========

    private GftpSession sourceSession;
    private String sourcePath;
    private GftpSession targetSession;
    private String targetPath;
    private boolean reverseActiveMode;
    private GridFTPClient privateSourceClient = null;
    private GridFTPClient privateTargetClient = null;
    private GftpTransferMonitor monitor = null;

    private Throwable transferEx;

    private String transferInfoStr;

    private long sourceFileSize;

    private int gftpMode = -1;

    private boolean isBusy = false;

    private Object busyMutex = new Object();

    /**
     * 
     * @param sourceSession
     * @param sourcePath
     * @param targetSession
     * @param targetPath
     * @param reverseActiveMode
     * @throws XenonException
     */
    public GftpThirdPartyTransfer(GftpSession sourceSession, String sourcePath, GftpSession targetSession, String targetPath,
            boolean reverseActiveMode) throws XenonException {

        // session can not be null 
        if (sourceSession == null)
            throw new NullPointerException("Source Session can not be null!");

        if (targetSession == null)
            throw new NullPointerException("Target Session can not be null!");

        this.sourceSession = sourceSession;
        this.targetSession = targetSession;

        // paths may be null
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        // defaults: 
        this.reverseActiveMode = reverseActiveMode;
        this.isBusy = false;
        this.transferEx = null;
        this.sourceFileSize = -1;

        init();
    }

    private void init() throws XenonException {

        this.transferInfoStr = "third party copy from " + sourceSession.getHostname() + ":" + sourcePath + " to "
                + targetSession.getHostname() + ":" + targetPath;
        logger.info("> Initializing: {}", transferInfoStr);
        //monitor.logPrintf("Performing: %s\n",transferInfoStr); 

        // Multi threaded support: Create private GridFTP clients for each location:  
        privateSourceClient = sourceSession.createGFTPClient();
        privateTargetClient = targetSession.createGFTPClient();

        initDCAU();

        initMonitor();

        try {
            sourceFileSize = sourceSession.getSize(sourcePath);
        } catch (Exception e) {
            // SRM 'BlindMode' Patch: not always possible to fetch file size from transport URLs !
            logger.warn("Couldn't determine size of source file {}:{}", sourcePath, e);
        }
    }

    private void initMonitor() {
        monitor = new GftpTransferMonitor(targetSession, targetPath, sourceFileSize);
    }

    private void initDCAU() {

        // ---
        // Grid FTP 1.0 compatibility
        // Both must support DCAU. If one of them doesn't: disable at the other !
        // ---

        boolean sourceDCAU = sourceSession.useDataChannelAuthentication();
        boolean destDCAU = targetSession.useDataChannelAuthentication();

        logger.debug(" - GridFTP 3rd party transfer source Server DCAU = {}", sourceDCAU);
        logger.debug(" - GridFTP 3rd party transfer dest   Server DCAU = {}", destDCAU);

        // Both must be true or false. 
        if (sourceDCAU != destDCAU) {
            logger.warn("Warning: Grid FTP Data Channel Authentication mismatch between source: {}  and destination: {} ",
                    targetSession.getHostname(), sourceSession.getHostname());
        }

        // Disable DCAU from source to target. 
        if (destDCAU == false) {
            try {
                privateSourceClient.setLocalNoDataChannelAuthentication();
                privateTargetClient.setDataChannelAuthentication(DataChannelAuthentication.NONE);
                logger.warn("Disabled DataChannel Authentication for target server:{} => {}", targetSession);
            } catch (Exception e) {
                // API might not be supported, continue anyway. 
                logger.error("Exception disabling DataChannel Authentication for target server:{} => {}", targetSession, e);
            }
        }

        // Disable DCAU from target to source (if needed).  
        if (sourceDCAU == false) {
            try {
                privateTargetClient.setLocalNoDataChannelAuthentication();
                privateSourceClient.setDataChannelAuthentication(DataChannelAuthentication.NONE);
                logger.warn("Disabled DataChannel Authentication for source server:{} => {}", sourceSession);
            } catch (Exception e) {
                // API might not be supported, continue anyway. 
                logger.error("Exception disabling DataChannel Authentication for source server:{} => {}", sourceSession, e);
            }
        }
    }

    /**
     * Update Grid FTP mode of source and target client, if different then previous GridFTP mode.
     */
    protected void updateGftpMode(int newMode) throws ServerException, IOException {
        if (newMode == gftpMode)
            return;

        if (privateSourceClient == null)
            throw new NullPointerException("GridFTPClient not initialized!");

        if (privateTargetClient == null)
            throw new NullPointerException("GridFTPClient not initialized!");

        privateSourceClient.setMode(newMode);
        privateTargetClient.setMode(newMode);
    }

    /**
     * Start complete file transfer.
     * 
     * @param append
     *            - whether to append to the remote file or not.
     * @throws XenonException
     */
    public void transfer(boolean append) throws XenonException {

        try {

            doTransfer(false, append, 0, 0, 0);

        } finally {
            closeAll();
        }

        logger.info("> GridFTP: Finished 3rd party transfer.\n");
        return; // ok 
    }

    /**
     * Perform partial extended block mode (Mode-E) transfer. <br>
     * By using extended mode a part of a file can be transfered. This way random access transfers can be done. This method can
     * also be used to transfer parts of a file in parallel, but multiple clients would have to be created for this mode to be
     * effective.
     * <p>
     * <Strong>Concurrency note</strong>: This method is NOT thread safe. Use multiple GridThirdPartyTansfer objects for parallel
     * copying of the same file.
     * 
     * @param sourceOffset
     *            - starting offset in source file.
     * @param numBytes
     *            - number of bytes to transfer.
     * @param destinationOffset
     *            - offset into destination file.
     * @param autoClose
     *            - whether to (auto) close the clients after the transfer.
     * 
     * @throws XenonException
     */
    public void transferPart(long sourceOffset, long numBytes, long destinationOffset, boolean autoClose) throws XenonException {

        try {

            doTransfer(false, false, sourceOffset, numBytes, destinationOffset);

        } finally {
            // last partial transfer: close
            if (autoClose) {
                closeAll();
            }
        }

        logger.info("> GridFTP: Finished partial third party transfer.\n");
        return; // ok 
    }

    protected void doTransfer(boolean partialTransfer, boolean append, long sourceOffset, long numBytes, long destOffset)
            throws XenonException {

        if (partialTransfer && append) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME,
                    "Performing a partial transfer with file ofsets is incompatible with append mode.");
        }

        if (partialTransfer && reverseActiveMode) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME,
                    "Can not combine partial transfers, which need Mode-E, in combination with reversed Active Mode");
        }

        try {

            setIsBusy();

            if (reverseActiveMode) {
                // ===
                // Reverse polarity of transfer, this means target server should connect back to source server
                // and perform a get() of the file. 
                // === 

                logger.info("Switching active/passive mode between servers. Target Server is active party.");

                // Create passive port at source server: 
                HostPort port = privateSourceClient.setPassive();
                // Skip: 
                // privateSourceClient.setLocalActive();  
                // privateTargetClient.setLocalPassive();
                // Let target server connect back to passive port on source server(!) 
                privateTargetClient.setActive(port);

            } else {

                logger.info("Source server will be active party in this party tranfer.");
                // The source server is active party, should be default mode for 3rd party transfers.
                // Following configuration should be default for source client:
                // privateSourceClient.setLocalPassive();
                // privateSourceClient.setActive(); 
            }

            if (partialTransfer == false) {
                // Initiate transfer from active source to passive destination  
                privateSourceClient.transfer(sourcePath, privateTargetClient, targetPath, append, monitor);
            } else {
                // Set to Extended mode or MODE-E. 
                updateGftpMode(GridFTPSession.MODE_EBLOCK);

                logger.info("Performing partial transfer using Extend Block Mode (MODE E): sourceFile bytes:[{},{}] ==>"
                        + " targetFile bytes:[{},{}] \n", sourceOffset, sourceOffset + numBytes, destOffset, destOffset
                        + numBytes);

                // perform partial transfer. This mode uses Extended Block Mode (Mode E) ! 
                privateSourceClient.extendedTransfer(sourcePath, sourceOffset, numBytes, privateTargetClient, targetPath,
                        destOffset, monitor);
            }

            setIsDone();

        } catch (Throwable e) {

            // Keep state at busy and set Exception. Transfer may not be restarted or called again. 
            logger.error("Exception during third transfer (partial={}) :{}", partialTransfer, e);
            transferEx = e;
            monitor.notifyException(e);

            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "Couldn't perform " + transferInfoStr + ". Error="
                    + transferEx.getMessage(), transferEx);
        }
    }

    public GftpTransferMonitor getMonitor() {
        return monitor;
    }

    /**
     * Return current file size of destination target. This way an ongoing file transfer can be monitored.
     * 
     * @return
     * @throws XenonException
     */
    public long getTargetFileSize() throws XenonException {

        if (targetSession == null) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "No TargetSession. Transfer already closed ?");
        }
        return this.targetSession.getSize(this.targetPath);
    }

    public boolean isBusy() {
        return isBusy;
    }

    /**
     * Update current state to busy: no two transfer() methods may be called at the same time.
     * 
     * @throws XenonException
     *             If busy state is wrong, for example a transfer() method is called again when the previous hasn't finished yet
     *             or an exception has occurred.
     */
    protected void setIsBusy() throws XenonException {

        synchronized (busyMutex) {
            if (isBusy == true) {
                throw new XenonException(GftpAdaptor.ADAPTOR_NAME,
                        "Concurrency Error: setIsBusy(): can not be called again, transfer is already running or previous has failed.");
            }
            isBusy = true;
        }

        if (monitor != null) {
            monitor.notifyHasStarted();
        }

    }

    /**
     * Update current state to finished: no two transfer() methods may be called at the same time.
     * 
     * @throws XenonException
     *             If busy state is wrong, for example a transfer() method already finished or an exception occurred.
     */
    protected void setIsDone() throws XenonException {

        synchronized (busyMutex) {

            if (isBusy == false) {
                throw new XenonException(GftpAdaptor.ADAPTOR_NAME,
                        "Concurrency Error: setIsDone(): transfer already finished, or an exception has occured!");
            }

            isBusy = false;
        }

        if (monitor != null) {
            monitor.notifyIsDone();
        }
    }

    // Idempotent close.
    public void close() {
        closeAll();
    }

    /**
     * Idempotent close. If called during an ongoing transfer this will result in termination of the ongoing transfer.
     */
    protected void closeAll() {

        if (isBusy()) {
            logger.warn("*** Warning: calling close on running transfer: {}\n", transferInfoStr);
        }

        try {
            if (privateSourceClient != null) {
                sourceSession.closeGFTPClient(privateSourceClient);
                privateSourceClient = null;
            }
        } catch (Exception e) {
            logger.warn("Exception when closing source Grid FTP client: {}\n", e);
        }

        try {
            if (privateTargetClient != null) {
                targetSession.closeGFTPClient(privateTargetClient);
                privateTargetClient = null;
            }
        } catch (Exception e) {
            logger.warn("Exception when closing target Grid FTP client: {}\n", e);
        }
    }

    public Throwable getException() {
        return this.transferEx;
    }

    /**
     * Dispose object, closes client if not yet closed:
     */
    public void dispose() {
        closeAll();
    }

}
