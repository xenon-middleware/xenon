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
package nl.esciencecenter.xenon.engine.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Simple stream forwarder. Uses a daemon thread to read and write data, has a small buffer, and ignores all exceptions.
 * 
 * @author Niels Drost
 * 
 */
public final class StreamForwarder extends Thread {

    static final Logger LOGGER = LoggerFactory.getLogger(StreamForwarder.class);

    public static final int BUFFER_SIZE = 1024;

    private final InputStream in;
    private final OutputStream out;

    private boolean done = false;
    
    public StreamForwarder(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;

        setDaemon(true);
        setName("Stream forwarder");
        start();
    }

    /**
     * Closes the input stream, thereby stopping the stream forwarder, and closing the output stream.
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

    private synchronized void done() { 
        done = true;
        notifyAll();
    }
    
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
        }
    }
    
    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (true) {
                int read = in.read(buffer);

                if (read == -1) {
                    done();
                    return;
                }

                if (out != null) {
                    out.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot forward stream", e);
        } finally {
            close(in, null);

            if (out != null) {
                close(out, null);
            }
        }
    }
}