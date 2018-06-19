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
package nl.esciencecenter.xenon.adaptors.filesystems;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps an OutputStream instance. Only functionality added is calling an extra close on a transfer client after closing the output stream.
 */
public class TransferClientOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final Closeable client;

    public TransferClientOutputStream(OutputStream outputStream, Closeable client) {
        this.outputStream = outputStream;
        this.client = client;
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
        outputStream.flush();
        outputStream.close();
        client.close();
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
