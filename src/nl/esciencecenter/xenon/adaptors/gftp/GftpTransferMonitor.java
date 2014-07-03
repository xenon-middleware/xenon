package nl.esciencecenter.xenon.adaptors.gftp;

import nl.esciencecenter.xenon.XenonException;

import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;

/**
 * Grid FTP monitor for third party transfers.
 * 
 * @author Piter T. de Boer
 */
public class GftpTransferMonitor implements MarkerListener {

    protected GftpSession targetSession;
    protected String targetFilePath;
    protected Throwable exception;
    protected long sourceFileSize = -1;
    protected boolean isDone=false;
    protected boolean hasStarted=false;

    public GftpTransferMonitor(GftpSession targetSession, String targetPath, long fileSize) {
        this.targetSession = targetSession;
        this.targetFilePath = targetPath;
        this.sourceFileSize = fileSize;
    }

    @Override
    public void markerArrived(Marker arg0) {
        // not used for now. 
    }

    /**
     * Returns number of bytes transferred to the target file. During third party copy the remote file size can be fetched in
     * parallel during the transfer. This is useful for monitoring the file transfer during (3rd party) copying.
     * 
     * @return current size of target file during copy (actual copied number of bytes).
     * 
     * @throws XenonException
     */
    public long getTargetFileSize() throws XenonException {
        try {
            return targetSession.getSize(targetFilePath);
        } catch (Exception e) {
            //
            //this.exception=e; 
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    /**
     * Returns length of file to be transferred.
     * 
     * @return file size of source file.
     */
    public long getSourceFileSize() {
        return this.sourceFileSize;
    }

    public boolean hasException() {
        return (this.exception != null);
    }

    public Throwable getException() {
        return this.exception;
    }

    /**
     * Keep Exception for monitoring purposes.
     * 
     * @param exception
     *            - the Throwable 
     */
    public void notifyException(Throwable exception) {
        this.exception = exception;
    }

    public void notifyIsDone() {
        isDone = true;
    }

    public void notifyHasStarted() {
        hasStarted = true;
    }

}
