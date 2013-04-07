package nl.esciencecenter.octopus.engine.util;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Reads output of a process, and puts it in a buffer.
 * 
 * @author Niels Drost
 * 
 */
class OutputReader extends Thread {

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
