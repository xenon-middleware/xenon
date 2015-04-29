package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wrapper class for executing a single operation on an FTPClient that does not return anything. The wrapper takes care of
 * checking the status after execution and throwing an exception if necessary.
 *
 * @author Christiaan Meijer
 *
 */
public abstract class FtpCommand {
    protected String replyString;
    protected boolean hasSucceeded;

    public abstract void doWork(FTPClient ftpClient, String path) throws IOException;

    public void execute(FTPClient ftpClient, Path path, String messageInCaseOfError) throws XenonException {
        String absolutePath = path.getRelativePath().getAbsolutePath();
        try {
            doWork(ftpClient, absolutePath);
            replyString = ftpClient.getReplyString();
            hasSucceeded = isCodeSuccessfulCompletion(ftpClient.getReplyCode());
            if (hasSucceeded == false) {
                throw new IOException(replyString);
            }
        } catch (IOException e) {
            throw new XenonException(FtpAdaptor.ADAPTOR_NAME, messageInCaseOfError + " " + absolutePath, e);
        }
    }

    /**
     * Returns true if code is in interval [100,300). See http://en.wikipedia.org/wiki/List_of_FTP_server_return_codes.
     *
     * @param replyCode
     * @return if code implies successful completion
     */
    private boolean isCodeSuccessfulCompletion(int replyCode) {
        return replyCode < 300 && replyCode >= 100;
    }
}
