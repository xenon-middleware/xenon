package nl.esciencecenter.octopus.engine.util;

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
public class StreamForwarder extends Thread {

    static final Logger logger = LoggerFactory.getLogger(StreamForwarder.class);

    public static final int BUFFER_SIZE = 1024;

    private final InputStream in;
    private final OutputStream out;

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
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            logger.error("Cannot close input stream", e);
        }
    }

    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int read = in.read(buffer);

                if (read == -1) {
                    return;
                }

                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            logger.error("Cannot forward stream", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Cannot close input stream", e);
            }
            try {
                out.close();
            } catch (IOException e) {
                logger.error("Cannot close output stream", e);
            }
        }
    }
}