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
package nl.esciencecenter.xenon.adaptors.filesystems.ftp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wraps an InputStream instance. Only functionality added is sending a pending command completed signal after closing the input
 * stream.
 *
 *
 */
public class FtpInputStream extends InputStream {
    private final InputStream inputStream;
    private final FTPClient ftpClient;
    private boolean completedPendingFtpCommand = false;

    public FtpInputStream(InputStream inputStream, FTPClient ftpClient) {
        this.inputStream = inputStream;
        this.ftpClient = ftpClient;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();

        // Added functionality:
        if (!completedPendingFtpCommand) {
            ftpClient.completePendingCommand();
            completedPendingFtpCommand = true;
            ftpClient.disconnect();
        }
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public String toString() {
        return inputStream.toString();
    }

}
