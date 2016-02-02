package nl.esciencecenter.xenon.adaptors.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

/**
 * Webdav does not support resuming or appending. This class acts as a buffer. On closing, the complete content is written at once
 * using the WebdavFiles adaptor.
 *
 * @author Christiaan Meijer
 *
 */
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
        try {
            if (files.exists(path)) {
                // first delete the file otherwise we get a 405 when executing the put
                files.delete(path);
            }
            files.createFile(path, outputStream.toByteArray());
        } catch (XenonException e) {
            throw new IOException(e);
        }

    }

}
