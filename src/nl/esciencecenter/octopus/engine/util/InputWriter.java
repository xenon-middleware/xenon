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

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes given content to the given output stream. Closes the output stream when done.
 * 
 * @author Niels Drost
 * 
 */
public class InputWriter extends Thread {

    static final Logger logger = LoggerFactory.getLogger(StreamForwarder.class);

    private final byte[] bytes;

    private final OutputStream destination;

    boolean finished = false; // written all content or got exception.

    public InputWriter(String content, OutputStream destination) {
        this.destination = destination;

        this.bytes = content.getBytes();

        setDaemon(true);
        setName("Input Writer");
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
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
    }

    public void run() {
        try {
            destination.write(bytes);
        } catch (IOException e) {
            logger.error("Cannot write content to stream", e);
        } finally {
            try {
                destination.close();
            } catch (IOException e) {
                logger.error("Cannot close input stream", e);
            }
            setFinished();
        }
    }
}
