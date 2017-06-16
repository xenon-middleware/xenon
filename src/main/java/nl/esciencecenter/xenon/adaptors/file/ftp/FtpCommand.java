/**
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
package nl.esciencecenter.xenon.adaptors.file.ftp;

import java.io.IOException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wrapper class for executing a single operation on an FTPClient that does not return anything. The wrapper takes care of
 * checking the status after execution and throwing an exception if necessary.
 *
 *
 */
public abstract class FtpCommand {
    private String replyString;
    private boolean hasSucceeded;

    public void setReplyString(String replyString) {
        this.replyString = replyString;
    }

    public void setHasSucceeded(boolean hasSucceeded) {
        this.hasSucceeded = hasSucceeded;
    }

    public abstract void doWork(FTPClient ftpClient, String path) throws IOException;

    public void execute(FTPClient ftpClient, Path path, String messageInCaseOfError) throws XenonException {
        String absolutePath = path.getRelativePath().getAbsolutePath();
        try {
            doWork(ftpClient, absolutePath);
            replyString = ftpClient.getReplyString();
            hasSucceeded = isCodeSuccessfulCompletion(ftpClient.getReplyCode());
            if (!hasSucceeded) {
                throw new IOException(replyString);
            }
        } catch (IOException e) {
            throw new XenonException(FtpFileAdaptor.ADAPTOR_NAME, messageInCaseOfError + " " + absolutePath, e);
        }
    }

    /*
     * Returns true if code is in interval [100,300). See http://en.wikipedia.org/wiki/List_of_FTP_server_return_codes.
     *
     * @param replyCode
     * @return if code implies successful completion
     */
    public static boolean isCodeSuccessfulCompletion(int replyCode) {
        return replyCode < 300 && replyCode >= 100;
    }
}
