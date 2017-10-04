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
package nl.esciencecenter.xenon.adaptors.filesystems.webdav;

import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.ADAPTOR_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class WebdavFileSystem extends FileSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFileAdaptor.class);

    class StreamToFileWriter extends Thread {

        private final String url;
        private final InputStream in;

        StreamToFileWriter(String url, InputStream in) {
            this.url = url;
            this.in = in;
            setName("WebdavStreamToFileWriter");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                client.put(url, in);
            } catch (Exception e) {

            }
        }
    }

    private final Sardine client;
    private final String server;

    protected WebdavFileSystem(String uniqueID, String name, String location, String server, Path entryPath, int bufferSize, Sardine client,
            XenonProperties properties) {
        super(uniqueID, name, location, entryPath, bufferSize, properties);
        this.client = client;
        this.server = server;
    }

    private String getFilePath(Path path) {

        if (!path.isAbsolute()) {
            throw new IllegalArgumentException("Path must be absolute!");
        }

        return server + path.toString();
    }

    private String getDirectoryPath(Path path) {

        if (!path.isAbsolute()) {
            throw new IllegalArgumentException("Path must be absolute!");
        }

        return server + path.toString() + "/";
    }

    private PathAttributes getAttributes(Path path, DavResource p) {
        PathAttributesImplementation attributes = new PathAttributesImplementation();

        attributes.setPath(path);
        attributes.setDirectory(p.isDirectory());
        attributes.setRegular(!p.isDirectory());

        attributes.setCreationTime(p.getCreation().getTime());
        attributes.setLastModifiedTime(p.getModified().getTime());
        attributes.setLastAccessTime(attributes.getLastModifiedTime());
        attributes.setSize(p.getContentLength());

        // Not sure is this is right ?
        attributes.setReadable(true);
        attributes.setWritable(false);

        return attributes;
    }

    @Override
    protected List<PathAttributes> listDirectory(Path path) throws XenonException {

        List<DavResource> list = null;

        try {
            list = client.list(getDirectoryPath(path), 1);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to list directory: " + path, e);
        }

        ArrayList<PathAttributes> result = new ArrayList<>(list.size());

        String dirPath = path.toString() + "/";

        for (DavResource d : list) {
            // The list also returns the directory itself, so ensure we don't
            // return it!
            if (!dirPath.equals(d.getPath())) {
                String filename = d.getName();
                result.add(getAttributes(path.resolve(filename), d));
            }
        }

        return result;
    }

    @Override
    public boolean isOpen() throws XenonException {
        return true;
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {

        LOGGER.debug("move source = {} to target = {}", source, target);

        Path absSource = toAbsolutePath(source);
        Path absTarget = toAbsolutePath(target);

        assertPathExists(absSource);

        if (areSamePaths(absSource, absTarget)) {
            return;
        }

        assertParentDirectoryExists(absTarget);
        assertPathNotExists(absTarget);

        PathAttributes a = getAttributes(absSource);

        try {
            if (a.isDirectory()) {
                client.move(getDirectoryPath(absSource), getDirectoryPath(absTarget), false);
            } else {
                client.move(getFilePath(absSource), getFilePath(absTarget), false);
            }
        } catch (SardineException e) {
            if (e.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
                return;
            }
            throw new XenonException(ADAPTOR_NAME, "Failed to move from " + absSource + " to " + absTarget, e);
        } catch (Exception e1) {
            throw new XenonException(ADAPTOR_NAME, "Failed to move from " + absSource + " to " + absTarget, e1);
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
        LOGGER.debug("createDirectory dir = {}", dir);

        Path absDir = toAbsolutePath(dir);
        assertPathNotExists(absDir);
        assertParentDirectoryExists(absDir);

        try {
            client.createDirectory(getDirectoryPath(absDir));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create directory: " + absDir, e);
        }
    }

    @Override
    public void createFile(Path file) throws XenonException {
        LOGGER.debug("createFile path = {}", file);

        Path absFile = toAbsolutePath(file);
        assertPathNotExists(absFile);
        assertParentDirectoryExists(absFile);

        try {
            client.put(getFilePath(absFile), new byte[0]);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create file: " + absFile, e);
        }
    }

    @Override
    public void createSymbolicLink(Path link, Path path) throws XenonException {
        throw new UnsupportedOperationException(ADAPTOR_NAME, "Operation not supported");
    }

    @Override
    protected void deleteFile(Path path) throws XenonException {
        try {
            client.delete(getFilePath(path));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to delete file: " + path, e);
        }
    }

    @Override
    protected void deleteDirectory(Path path) throws XenonException {
        try {
            client.delete(getDirectoryPath(path));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to delete directory: " + path, e);
        }
    }

    @Override
    public boolean exists(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);

        try {
            return client.exists(getDirectoryPath(absPath)) || client.exists(getFilePath(absPath));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to check existence of directory: " + absPath);
        }
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);
        assertFileExists(absPath);

        try {
            return client.get(getFilePath(absPath));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to access file: " + absPath);
        }
    }

    @Override
    public OutputStream writeToFile(Path file, long size) throws XenonException {

        Path absFile = toAbsolutePath(file);
        assertPathNotExists(absFile);
        assertParentDirectoryExists(absFile);

        try {
            PipedInputStream in = new PipedInputStream(4096);
            PipedOutputStream out = new PipedOutputStream(in);

            // Create a separate thread here to handle the writing
            new StreamToFileWriter(getFilePath(absFile), in).start();

            return out;
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to open stream for writing", e);
        }
    }

    @Override
    public OutputStream appendToFile(Path file) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "Appending to file not supported");
    }

    @Override
    public OutputStream writeToFile(Path file) throws XenonException {
        return writeToFile(file, -1);
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);
        assertPathExists(absPath);

        try {
            List<DavResource> result = client.list(getFilePath(absPath), 0);
            return getAttributes(absPath, result.get(0));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to get attributes for file: " + absPath, e);
        }
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "Operation not supported");
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "Operation not supported");
    }
}
