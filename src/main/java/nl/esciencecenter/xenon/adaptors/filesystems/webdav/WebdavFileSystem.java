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
//import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.OK_CODE;
//import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.isOkish;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.xenon.filesystems.*;

import org.apache.http.HttpStatus;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpMethod;
//import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
//import org.apache.jackrabbit.webdav.DavConstants;
//import org.apache.jackrabbit.webdav.DavException;
//import org.apache.jackrabbit.webdav.MultiStatus;
//import org.apache.jackrabbit.webdav.MultiStatusResponse;
//import org.apache.jackrabbit.webdav.client.methods.DavMethod;
//import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
//import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
//import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
//import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
//import org.apache.jackrabbit.webdav.client.methods.PutMethod;
//import org.apache.jackrabbit.webdav.property.DavProperty;
//import org.apache.jackrabbit.webdav.property.DavPropertyName;
//import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
//import org.apache.jackrabbit.webdav.property.DavPropertySet;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;

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

        public void run() {
            try {
                client.put(url, in);
            } catch (Exception e) {

            }
        }
    }

    private final Sardine client;
    private final String server;

    protected WebdavFileSystem(String uniqueID, String name, String location, String server, Path entryPath,
            Sardine client, XenonProperties properties) {
        super(uniqueID, name, location, entryPath, properties);
        this.client = client;
        this.server = server;
    }

    private String getFilePath(Path path) {

        return server + (path.isAbsolute() ? "" : "/") +  path.toString();
    }

    private String getDirectoryPath(Path path) {
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

        // TODO: no clue if this is right ?
        attributes.setReadable(true);
        attributes.setWritable(false);

        return attributes;
    }

    @Override
    protected List<PathAttributes> listDirectory(Path path)  throws XenonException {

        List<DavResource> list = null;

        try {
            list = client.list(getDirectoryPath(path), 1);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to list directory: " + path, e);
        }

        ArrayList<PathAttributes> result = new ArrayList<>(list.size());

        String dirPath = path.toString() + "/";

        for (DavResource d : list) {
            // The list also returns the directory itself, so ensure we don't return it!
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

        assertPathExists(source);

        if (areSamePaths(source, target)) {
            return;
        }

        assertParentDirectoryExists(target);
        assertPathNotExists(target);

        PathAttributes a = getAttributes(source);

        try {
            if (a.isDirectory()) {
                client.move(getDirectoryPath(source), getDirectoryPath(target), false);
            } else {
                client.move(getFilePath(source), getFilePath(target), false);
            }
        } catch (SardineException e) {
            if (e.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
                return;
            }
            throw new XenonException(ADAPTOR_NAME, "Failed to move from " + source + " to " + target, e);
        } catch (Exception e1) {
            throw new XenonException(ADAPTOR_NAME, "Failed to move from " + source + " to " + target, e1);
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
        LOGGER.debug("createDirectory dir = {}", dir);

        assertPathNotExists(dir);
        assertParentDirectoryExists(dir);

        try {
            client.createDirectory(getDirectoryPath(dir));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create directory: " + dir, e);
        }
    }

    @Override
    public void createFile(Path file) throws XenonException {
        LOGGER.debug("createFile path = {}", file);

        assertPathNotExists(file);
        assertParentDirectoryExists(file);

        try {
            client.put(getFilePath(file), new byte[0]);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create file: " + file, e);
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

        assertNotNull(path);

        try {
            return client.exists(getDirectoryPath(path)) || client.exists(getFilePath(path));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to check existence of directory: " + path);
        }
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {

        assertFileExists(path);

        try {
            return client.get(getFilePath(path));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to access file: " + path);
        }
    }

    @Override
    public OutputStream writeToFile(Path file, long size) throws XenonException {

        assertParentDirectoryExists(file);
        assertPathNotExists(file);

        try {
            PipedInputStream in = new PipedInputStream(4096);
            PipedOutputStream out = new PipedOutputStream(in);

            // Create a separate thread here to handle the writing
            new StreamToFileWriter(getFilePath(file), in).start();

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

        assertPathExists(path);

//        if (directoryExist(path)) {
//            try {
//                List<DavResource> result = client.list(getFilePath(path), 1);
//
//                for (DavResource d : result) {
//
//                    String dirPath = path.getAbsolutePath() + "/";
//
//                    if (dirPath.equals(d.getPath())) {
//                        return getAttributes(path, d);
//                    }
//                }
//            } catch (Exception e) {
//                throw new XenonException(ADAPTOR_NAME, "Failed to get attributes for directory: " + path, e);
//            }
//        } else if (fileExist(path)) {

            try {
                List<DavResource> result = client.list(getFilePath(path), 0);
                return getAttributes(path, result.get(0));
            } catch (Exception e) {
                throw new XenonException(ADAPTOR_NAME, "Failed to get attributes for file: " + path, e);
            }
//        }
//        throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
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
