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
package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.adaptors.local.LocalAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.engine.util.CopyEngine;
import nl.esciencecenter.xenon.engine.util.CopyInfo;
import nl.esciencecenter.xenon.engine.util.OpenOptions;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.InvalidOpenOptionsException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;

import org.globus.ftp.MlsxEntry;
import org.globus.io.streams.GridFTPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Files implementation for Grid FTP. 
 * 
 * @author Piter T. de Boer. 
 */
public class GftpFiles implements Files {

    private static final Logger LOGGER = LoggerFactory.getLogger(GftpFiles.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "gftp" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     */
    static class FileSystemInfo {
        private final FileSystemImplementation impl;

        private final GftpSession session;

        public FileSystemInfo(FileSystemImplementation impl, GftpSession session) {
            super();
            this.impl = impl;
            this.session = session;
        }

        public FileSystemImplementation getImpl() {
            return impl;
        }

        public GftpSession getSession() {
            return session;
        }
    }

    private final XenonEngine xenonEngine;

    private final GftpAdaptor adaptor;

    private Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    public GftpFiles(GftpAdaptor sshAdaptor, XenonEngine xenonEngine) {
        this.xenonEngine = xenonEngine;
        this.adaptor = sshAdaptor;
    }

    private void checkParent(Path path) throws XenonException {

        RelativePath parentName = path.getRelativePath().getParent();

        if (parentName == null) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Parent directory does not exist!");
        }

        Path parent = newPath(path.getFileSystem(), parentName);

        if (!exists(parent)) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
        }
    }

    protected FileSystem newFileSystem(GftpSession session, String scheme, String location, Credential credential,
            XenonProperties properties) throws XenonException {

        String uniqueID = getNewUniqueID();

        LOGGER.debug("* newFileSystem scheme = {} location = {} credential = {} properties = {}", scheme, location, credential,
                properties);

        // (re)connect, keep possible already connected session. Session might be shared and/or re-used.
        session.connect(false);

        String wd = session.pwd();

        RelativePath entryPath = new RelativePath(wd);

        FileSystemImplementation result = new FileSystemImplementation(GftpAdaptor.ADAPTOR_NAME, uniqueID, scheme, location,
                entryPath, credential, properties);

        fileSystems.put(uniqueID, new FileSystemInfo(result, session));

        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", wd, entryPath, uniqueID);

        return result;
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties)
            throws XenonException {

        LOGGER.debug("newFileSystem scheme = {} location = {} credential = {} properties = {}", scheme, location, credential,
                properties);

        GftpLocation sshLocation = GftpLocation.parse(location);

        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), properties);

        // Create New private owned Session for each Grid FTP FileSystem Implementation. 
        GftpSession session = adaptor.createNewSession(sshLocation, (GlobusProxyCredential) credential, xenonProperties);

        return newFileSystem(session, scheme, location, credential, xenonProperties);
    }

    private GftpSession getSession(Path path) throws XenonException {

        FileSystemImplementation fs = (FileSystemImplementation) path.getFileSystem();
        FileSystemInfo info = fileSystems.get(fs.getUniqueID());

        if (info == null) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "File system is already closed");
        }

        return info.getSession();
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws XenonException {

        LOGGER.debug("close fileSystem = {}", filesystem);

        FileSystemImplementation fs = (FileSystemImplementation) filesystem;

        FileSystemInfo info = fileSystems.remove(fs.getUniqueID());

        if (info == null) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "file system is already closed");
        }

        info.getSession().disconnect();

        LOGGER.debug("close OK");
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws XenonException {

        LOGGER.debug("isOpen fileSystem = {}", filesystem);

        FileSystemImplementation fs = (FileSystemImplementation) filesystem;
        boolean result = (fileSystems.get(fs.getUniqueID()) != null);

        LOGGER.debug("isOpen OK result = {}", result);

        return result;
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {

        createDirectory(dir, false);
    }

    /**
     * 
     * @param dir
     *            - directory path to create, may only contian one subdirectory which may be created.
     * @param ignoreExisting
     *            - equavalent with -f flag: create or keep existing (sub) directory.
     * @throws XenonException
     */
    public void createDirectory(Path dir, boolean ignoreExisting) throws XenonException {

        LOGGER.debug("createDirectory dir = {}", dir);

        if (exists(dir)) {
            throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        checkParent(dir);
        GftpSession session = getSession(dir);
        session.mkdir(dir.getRelativePath().getAbsolutePath(), false);

        LOGGER.debug("createDirectory OK");
    }

    @Override
    public void createDirectories(Path dir) throws XenonException {

        LOGGER.debug("createDirectories dir = {}", dir);

        if (exists(dir)) {
            throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        Iterator<RelativePath> itt = dir.getRelativePath().iterator();

        while (itt.hasNext()) {
            Path tmp = newPath(dir.getFileSystem(), itt.next());

            if (!exists(tmp)) {
                createDirectory(tmp);
            }
        }

        LOGGER.debug("createDirectories OK");
    }

    @Override
    public void createFile(Path path) throws XenonException {

        LOGGER.debug("createFile path = {}", path);

        if (exists(path)) {
            throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "File " + path + " already exists!");
        }

        checkParent(path);

        OutputStream out = null;

        try {
            out = newOutputStream(path, OpenOption.CREATE, OpenOption.WRITE, OpenOption.APPEND);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

        LOGGER.debug("createFile OK");
    }

    @Override
    public void delete(Path path) throws XenonException {

        LOGGER.debug("delete path = {}", path);

        if (!exists(path)) {
            throw new NoSuchPathException(getClass().getName(), "Cannot delete file, as it does not exist");
        }

        GftpSession session = getSession(path);

        FileAttributes att = getAttributes(path);

        try {
            if (att.isDirectory()) {
                if (newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                    throw new DirectoryNotEmptyException(GftpAdaptor.ADAPTOR_NAME, "cannot delete dir " + path
                            + " as it is not empty");
                }

                session.rmdir(path.getRelativePath());
            } else {
                session.rmfile(path.getRelativePath());
            }
        } catch (Exception e) {
            throw new XenonException(this.adaptor.getName(), e.getMessage(), e);
        }

        LOGGER.debug("delete OK");
    }

    /**
     * Move or rename an existing source path to a non-existing target path.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the source is a link, the link itself will be moved, not the path to which it refers. If the source is a directory, it
     * will be renamed to the target. This implies that a moving a directory between physical locations may fail.
     * 
     * @param source
     *            the existing source path.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchPathException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws PathAlreadyExistsException
     *             If the target file already exists.
     * @throws XenonException
     *             If the move failed.
     */
    @Override
    public void move(Path source, Path target) throws XenonException {

        LOGGER.debug("move source = {} target = {}", source, target);

        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (!sourcefs.getLocation().equals(targetfs.getLocation())) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "Cannot move between different FileSystems: "
                    + sourcefs.getLocation() + " and " + targetfs.getLocation());
        }

        if (!exists(source)) {
            throw new NoSuchPathException(GftpAdaptor.ADAPTOR_NAME, "Source " + source + " does not exist!");
        }

        RelativePath sourceName = source.getRelativePath().normalize();
        RelativePath targetName = target.getRelativePath().normalize();

        if (sourceName.equals(targetName)) {
            return;
        }

        if (exists(target)) {
            throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "Target " + target + " already exists!");
        }

        checkParent(target);

        // Ok, here, we just have a local move
        GftpSession session = getSession(target);

        try {
            LOGGER.debug("move from " + source + " to " + target);
            session.rename(source.getRelativePath().getAbsolutePath(), target.getRelativePath().getAbsolutePath());
        } catch (Exception e) {
            throw new XenonException(this.adaptor.getName(), e.getMessage(), e);
        }

        LOGGER.debug("move OK");
    }

    private List<MlsxEntry> listDirectory(Path path, Filter filter) throws XenonException {

        FileAttributes att = getAttributes(path);

        if (!att.isDirectory()) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "Filter is null.");
        }

        GftpSession session = getSession(path);

        List<MlsxEntry> result = null;

        try {
            result = session.list(path.getRelativePath());
            return result;
        } catch (Exception e) {
            throw new XenonException(this.adaptor.getName(), e.getMessage(), e);
        }

    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", path);
        return new GftpDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {} filter = <?>", path);
        return new GftpDirectoryStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {}", dir);
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
        LOGGER.debug("newAttributesDirectoryStream path = {}", dir);
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {

        LOGGER.debug("newInputStream path = {}", path);

        if (!exists(path)) {
            throw new NoSuchPathException(GftpAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        FileAttributes attrs = getAttributes(path);

        if (attrs.isDirectory()) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "Path " + path + " is a directory!");
        }

        GftpSession session = getSession(path);

        GridFTPInputStream in = null;

        try {
            in = session.createInputStream(path.getRelativePath().getAbsolutePath());
        } catch (Exception e) {
            throw new XenonException(this.adaptor.getName(), e.getMessage(), e);
        }

        LOGGER.debug("newInputStream OK");

        return in;
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {

        LOGGER.debug("newOutputStream path = {} option = {}", path, options);

        OpenOptions tmp = OpenOptions.processOptions(GftpAdaptor.ADAPTOR_NAME, options);

        if (tmp.getReadMode() != null) {
            throw new InvalidOpenOptionsException(GftpAdaptor.ADAPTOR_NAME, "Disallowed open option: READ");
        }

        if (tmp.getAppendMode() == null) {
            throw new InvalidOpenOptionsException(GftpAdaptor.ADAPTOR_NAME, "No append mode provided!");
        }

        if (tmp.getWriteMode() == null) {
            tmp.setWriteMode(OpenOption.WRITE);
        }

        if (tmp.getOpenMode() == OpenOption.CREATE) {
            if (exists(path)) {
                throw new PathAlreadyExistsException(GftpAdaptor.ADAPTOR_NAME, "File already exists: " + path);
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) {
            if (!exists(path)) {
                throw new NoSuchPathException(GftpAdaptor.ADAPTOR_NAME, "File does not exist: " + path);
            }
        }

        boolean append = false;

        if (OpenOption.contains(OpenOption.APPEND, options)) {
            append = true;
        }

        GftpSession session = getSession(path);

        OutputStream out = null;

        try {
            out = session.createOutputStream(path.getRelativePath().getAbsolutePath(), append);
        } catch (Exception e) {
            throw new XenonException(this.adaptor.getName(), e.getMessage(), e);
        }

        LOGGER.debug("newOutputStream OK");
        return out;
    }

    @Override
    public Path readSymbolicLink(Path path) throws XenonException {
        throw new XenonException(adaptor.getName(), "readSymbolicLink(): Not Implemented");
    }

    public void end() {
        LOGGER.debug("end called, closing all file systems");

        while (fileSystems.size() > 0) {
            Set<String> keys = fileSystems.keySet();
            String first = keys.iterator().next();
            FileSystem fs = fileSystems.get(first).getImpl();

            try {
                close(fs);
            } catch (XenonException e) {
                // ignore for now
            }
        }

        LOGGER.debug("end OK");
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new XenonException(adaptor.getName(), "setPosixFilePermissions(): Not Implemented");
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        LOGGER.debug("getAttributes path = {}", path);

        FileAttributes att = new GftpFileAttributes(stat(path), path);
        LOGGER.debug("getAttributes OK result = {}", att);

        return att;
    }

    private MlsxEntry stat(Path path) throws XenonException {
        MlsxEntry entry = getSession(path).mlst(path.getRelativePath());

        if (entry != null) {
            return entry;
        }

        throw new NoSuchPathException(adaptor.getName(), "Couldn't stat remote file path:" + path);
    }

    @Override
    public boolean exists(Path path) throws XenonException {

        LOGGER.debug("exists path = {}", path);

        boolean result;

        try {
            stat(path);
            result = true;
        } catch (NoSuchPathException e) {
            result = false;
        }

        LOGGER.debug("exists OK result = {}", result);

        return result;
    }

    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {

        LOGGER.debug("copy source = {} target = {} options = {}", source, target, options);

        CopyEngine ce = xenonEngine.getCopyEngine();

        CopyInfo info = CopyInfo.createCopyInfo(GftpAdaptor.ADAPTOR_NAME, ce.getNextID("SSH_COPY_"), source, target, options);

        ce.copy(info);

        Copy result;

        if (info.isAsync()) {
            result = info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "Copy failed!", e);
            }

            result = null;
        }

        LOGGER.debug("copy OK result = {}", result);

        return result;
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {

        LOGGER.debug("getCopyStatus copy = {}", copy);

        CopyStatus result = xenonEngine.getCopyEngine().getStatus(copy);

        LOGGER.debug("getCopyStatus OK result = {}", result);

        return result;
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws XenonException {

        LOGGER.debug("cancelCopy copy = {}", copy);

        CopyStatus result = xenonEngine.getCopyEngine().cancel(copy);

        LOGGER.debug("cancelCopy OK result = {}", result);

        return result;
    }
}
