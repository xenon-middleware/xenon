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
package nl.esciencecenter.xenon.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A simple output reader that uses a daemon thread to read from an {@link java.io.InputStream} and buffer this data. Once 
 * end-of-stream is reached, this data will be made available as a {@link java.lang.String}. 
 * 
 * Note that since the data is buffered in memory, so it is not advisable to use this OutputReader to read large amounts of data. 
 */
public final class OutputReader extends Thread {

    private static final int BUFFER_SIZE = 1024;

    private final InputStream source;

    private ByteBuffer buffer;

    // Reached End Of File or got exception.
    private boolean finished = false;

    /**
     * Create an OutputReader that reads from <code>source</code>.
     * 
     * @param source
     *          the {#link InputStream} to read from.
     */
    
    public OutputReader(InputStream source) {
        this.source = source;

        buffer = ByteBuffer.allocate(BUFFER_SIZE);

        setDaemon(true);
        setName("Output reader");
        start();
    }

    private synchronized void setFinished() {
        finished = true;
        notifyAll();
    }

    /**
     * Returns if the OutputReader has finished (i.e., has reached the end-of-stream on the input). If so, the data that has been 
     * read is now available through {@link #getResult()}.
     * 
     * @return
     *          if the OutputReader has finished reading.
     */
    public synchronized boolean isFinished() {
        return finished;
    }

    /**
     * Waits until the OutputReader has finished (i.e., has reached the end-of-stream on the input). After this method returns, 
     * the data that has been read is available through {@link #getResult()}.
     */
    public synchronized void waitUntilFinished() {
        while (!finished) {
            try {
                wait();
            } catch (InterruptedException t) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void addToBuffer(byte[] bytes, int length) {
        while (buffer.remaining() < length) {
            // create new buffer with double the capacity of the old buffer
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);

            // copy data into new buffer
            buffer.flip();
            newBuffer.put(buffer);

            // replace old buffer
            buffer = newBuffer;
        }
        buffer.put(bytes, 0, length);
    }

    /** 
     * Entry method for daemon thread.
     */
    public void run() {
        byte[] bytes = new byte[BUFFER_SIZE];

        try {
            while (true) {
                int readCount = source.read(bytes);

                if (readCount < 0) {
                    // end-of-stream, we're done
                    setFinished();
                    return;
                }

                addToBuffer(bytes, readCount);
            }
        } catch (Exception e) {
            setFinished();
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Returns the data that has been read from the {@link java.io.InputStream} as a {@link java.lang.String}. If the 
     * OutputReader has not finished reading, this method will block until end-of-stream has been reached.
     *  
     * @return
     *          the data that has been read.
     */
    public synchronized String getResult() {
        waitUntilFinished();
        return new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);
    }
}
