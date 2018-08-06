package nl.esciencecenter.xenon.adaptors.filesystems;

import java.io.Closeable;
import java.io.IOException;

public class TestClient implements Closeable {

    boolean closed = false;

    @Override
    public void close() throws IOException {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
