package nl.esciencecenter.octopus.engine.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OutputStream useful for merging output streams. Uses a seperate thread to
 * write the data to the underlying output stream. May block if the queue is
 * full. Currently not a very efficient implementation.
 * 
 * @author Niels Drost
 * 
 */
public class MergingOutputStream extends OutputStream implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MergingOutputStream.class);

    private final int QUEUE_SIZE = 32;

    //TODO: implement a circular buffer instead of this queue
    private final ArrayBlockingQueue<byte[]> buffers;

    private final OutputStream out;

    private final Thread thread;

    private boolean open = true;

    public MergingOutputStream(OutputStream out) {
        buffers = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);

        this.out = out;

        thread = new Thread(this);
        thread.setDaemon(false);
        thread.setName("Merging output stream for " + out);
        thread.start();
    }

    @Override
    public void write(int b) throws IOException {
        byte[] singleByte = new byte[] { (byte) b };

        try {
            buffers.put(singleByte);
        } catch (InterruptedException e) {
            throw new IOException("thread interrupted on write", e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        byte[] copy = new byte[b.length];

        System.arraycopy(b, 0, copy, 0, b.length);

        try {
            buffers.put(copy);
        } catch (InterruptedException e) {
            throw new IOException("thread interrupted on write", e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] copy = new byte[len];

        System.arraycopy(b, off, copy, 0, len);

        try {
            buffers.put(copy);
        } catch (InterruptedException e) {
            throw new IOException("thread interrupted on write", e);
        }
    }

    @Override
    public void flush() throws IOException {
        // NOTHING
    }

    @Override
    public synchronized void close() throws IOException {
        open = false;
    }

    @Override
    public void run() {
        while (open || buffers.size() > 0) {
            try {
                byte[] buffer = buffers.take();
                out.write(buffer);
            } catch (InterruptedException e) {
                // IGNORE
            } catch (IOException e) {
                logger.error("Error while writing to output", e);
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            // IGNORE
        }
    }
}
