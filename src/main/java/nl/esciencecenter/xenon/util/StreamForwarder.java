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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple stream forwarder that uses a daemon thread to read from an {@link java.io.InputStream} and write it to a 
 * {@link java.io.OutputStream}. 
 * A small buffer is used (typically 1 KB) to improve performance. Any exceptions will be ignored.
 */
public final class StreamForwarder extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamForwarder.class);

    private static final int BUFFER_SIZE = 1024;

    private final InputStream in;
    private final OutputStream out;

    private boolean done = false;
    
    /**
     * Create a new StreamForwarder and start it immediately.
     * 
     * @param in the {@link java.io.InputStream} to read from.
     * @param out the {@link java.io.OutputStream} to write to.
     */
    public StreamForwarder(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;

        setDaemon(true);
        setName("Stream forwarder");
        start();
    }

    /**
     * Closes the input stream, thereby stopping the stream forwarder, and closing the output stream.
     * 
     * @param c The {@link java.io.Closable} to close (i.e., the {@link java.io.InputStream} or {@link java.io.OutputStream}) 
     * @param error The error message to print if the close results in an Exception
     */
    private void close(Closeable c, String error) {
        try {
            c.close();
        } catch (Exception e) {
            if (error != null) {
                LOGGER.error(error, e);
            }
        }
    }

    /**
     * Tell the daemon thread that we are done.
     */
    private synchronized void done() {
        done = true;
        notifyAll();
    }
    
    /**
     * Wait for a given timeout for the StreamForwarder to terminate by reading an end-of-stream on the input. When the timeout
     * expires both input and output streams will be closed, regardless of whether the input has reached end-of-line.
     * 
     * @param timeout
     *          The number of milliseconds to wait for termination. 
     */
    public synchronized void terminate(long timeout) { 

        if (done) { 
            return;
        }
        
        if (timeout > 0) { 
            long deadline = System.currentTimeMillis() + timeout;
            long left = timeout;

            while (!done && left > 0) { 
            
                try { 
                    wait(left);
                } catch (InterruptedException e) { 
                    // ignored
                }

                left = deadline - System.currentTimeMillis();
            }
        }
        
        if (!done) {
            close(in, "InputStream did not close within " + timeout + " ms. Forcing close!");
            
            if (out != null) { 
                close(out, null);
            }
        }
    }
    
    /**
     * Main entry method for the daemon thread.
     */
    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (true) {
                int read = in.read(buffer);

                if (read == -1) {
                    // NOTE: Streams must be closed before done is called, or we'll have a race condition!                  
                    close(in, null);

                    if (out != null) {
                        close(out, null);
                    }

                    done();
                    return;
                }
                                
                if (out != null) {
                    out.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            close(in, null);

            if (out != null) {
                close(out, null);
            }
        }
    }
}