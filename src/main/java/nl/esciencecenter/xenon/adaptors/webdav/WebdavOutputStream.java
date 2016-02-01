package nl.esciencecenter.xenon.adaptors.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

public class WebdavOutputStream extends OutputStream {
    private final WebdavFiles files;
    private final Path path;
    ArrayList<Byte> buffer = new ArrayList<Byte>();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public WebdavOutputStream(Path path, WebdavFiles webdavFiles) {
        this.path = path;
        files = webdavFiles;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        // first delete the file otherwise we get a 405 when executing the put
        try {
            if (files.exists(path)) {
                files.delete(path);
            }
            files.createFile(path, outputStream.toByteArray());
        } catch (XenonException e) {
            throw new IOException(e);
        }

    }

}
