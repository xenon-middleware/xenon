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
package nl.esciencecenter.octopus.engine.util;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Reads output from a stream, buffers it, and makes it available as a string when the stream reaches EndOfStream.
 * 
 * @author Niels Drost
 * 
 */
public class OutputReader extends Thread {

    private final InputStream source;

    private ByteBuffer buffer;

    boolean finished = false; // reached eof or got exception.

    public OutputReader(InputStream source) {
        this.source = source;

        buffer = ByteBuffer.allocate(1024);

        setDaemon(true);
        setName("Output reader");
        start();
    }

    private synchronized void setFinished() {
        finished = true;
        notifyAll();
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized void waitUntilFinished() {
        while (!finished) {
            try {
                wait();
            } catch (Throwable t) {
                // Ignore.
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

    public void run() {
        byte[] bytes = new byte[1024];

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
        } catch (Throwable e) {
            setFinished();
        } finally {
            try {
                source.close();
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    public synchronized String getResult() {
        waitUntilFinished();

        return new String(buffer.array(), 0, buffer.position());
    }
}
