package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

public abstract class FtpCommand {
    protected String replyString;

    public abstract boolean execute(FTPClient ftpClient, String path) throws IOException;

    /**
     * Gets the status code and message of execution.
     *
     * @return
     */
    public String getReplyString() {
        return replyString;
    }
}
