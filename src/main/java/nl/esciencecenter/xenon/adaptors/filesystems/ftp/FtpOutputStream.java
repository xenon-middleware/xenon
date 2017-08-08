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
package nl.esciencecenter.xenon.adaptors.filesystems.ftp;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wraps an OutputStream instance. Only functionality added is sending a pending command completed signal after closing the output
 * stream.
 */
public class FtpOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final FTPClient ftpClient;
    private boolean completedPendingFtpCommand = false;

    public FtpOutputStream(OutputStream outputStream, FTPClient ftpClient) {
        this.outputStream = outputStream;
        this.ftpClient = ftpClient;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();

        // Added functionality:
        if (!completedPendingFtpCommand) {
            ftpClient.completePendingCommand();
            completedPendingFtpCommand = true;
            ftpClient.disconnect();
//
//            int replyCode = ftpClient.getReplyCode();
//            String replyString = ftpClient.getReplyString();
//
//            System.out.println("**REPLY " + replyCode + " " + replyString);
//
//            ftpClient.disconnect();
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public String toString() {
        return outputStream.toString();
    }
}
