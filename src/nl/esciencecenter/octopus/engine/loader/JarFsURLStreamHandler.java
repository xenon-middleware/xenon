package nl.esciencecenter.octopus.engine.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


class JarFsURLStreamHandler extends URLStreamHandler {
    
    private final JarFileSystem fileSystem;
    private final JarFsFile file;
    
    JarFsURLStreamHandler(JarFileSystem fileSystem, JarFsFile file) {
        this.fileSystem = fileSystem;
        this.file = file;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new JarFsURLConnection(fileSystem, file, url);
    }
}