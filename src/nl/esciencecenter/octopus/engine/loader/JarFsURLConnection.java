package nl.esciencecenter.octopus.engine.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

class JarFsURLConnection extends URLConnection {

    private final JarFileSystem fileSystem;

    private final JarFsFile file;

    private InputStream in = null;

    public JarFsURLConnection(JarFileSystem fileSystem, JarFsFile file, URL url) {
        super(url);

        this.fileSystem = fileSystem;
        this.file = file;
    }

    @Override
    public void connect() throws IOException {
        fileSystem.loadFileData(file);

        in = new ByteArrayInputStream(file.getBytes().array(), file.getBytes().position(), file.getBytes().remaining());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (in == null) {
            throw new IOException("not connected yet");
        }
        return in;
    }
}