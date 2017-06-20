/**
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

package nl.esciencecenter.xenon.engine.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.util.CopyEngine;
import nl.esciencecenter.xenon.engine.util.CopyInfo;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAdaptorDescription;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 */
public abstract class FileAdaptor extends Adaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileAdaptor.class);
    
    private final FilesEngine filesEngine;
    
    /**
     * @param xenonEngine
     * @param name
     * @param description
     * @param supportedSchemes
     * @param supportedLocations
     * @param validProperties
     * @param properties
     */
    protected FileAdaptor(FilesEngine filesEngine, String name, String description, ImmutableArray<String> supportedSchemes,
            ImmutableArray<String> supportedLocations, ImmutableArray<XenonPropertyDescription> validProperties) {
        super(name, description, supportedSchemes, supportedLocations, validProperties);
        this.filesEngine = filesEngine;
    }
    
    public Path newPath(FileSystem filesystem, RelativePath location) throws XenonException {
        return new PathImplementation(filesystem, location);
    }
    
    /**
     * @return
     */
    public FileAdaptorDescription getAdaptorDescription() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {
        LOGGER.debug("copy source = {} target = {} options = {}", source, target, options);
        CopyEngine ce = filesEngine.getCopyEngine();

        CopyInfo info = CopyInfo.createCopyInfo(getName(), ce.getNextID("COPY_" + getName()), source, target, options);

        ce.copy(info);

        Copy result;

        if (info.isAsync()) {
            result = info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new XenonException(getName(), "Copy failed!", e);
            }

            result = null;
        }

        LOGGER.debug("copy OK result = {}", result);

        return result;
    }
    
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {
        LOGGER.debug("getCopyStatus copy = {}", copy);
        CopyStatus result = filesEngine.getCopyEngine().getStatus(copy);
        LOGGER.debug("getCopyStatus OK result = {}", result);
        return result;
    }

    public CopyStatus cancelCopy(Copy copy) throws XenonException {
        LOGGER.debug("cancelCopy copy = {}", copy);
        CopyStatus result = filesEngine.getCopyEngine().cancel(copy);
        LOGGER.debug("cancelCopy OK result = {}", result);
        return result;
    }

    public void createDirectories(Path path) throws XenonException {
        LOGGER.debug("createDirectories dir = {}", path);
        RelativePath relativeParent = path.getRelativePath().getParent();

        if (relativeParent != null) {
            PathImplementation parentPath = new PathImplementation(path.getFileSystem(), relativeParent);
            if (!exists(parentPath)) {
                // Recursive call
                createDirectories(parentPath);
            }
        }
        createDirectory(path);
        LOGGER.debug("createDirectories OK");
    }

    public abstract FileSystem newFileSystem(String location, Credential credential, Map<String, String> properties)
            throws XenonException;

    public abstract void close(FileSystem filesystem) throws XenonException;

    public abstract boolean isOpen(FileSystem filesystem) throws XenonException;

    public abstract void move(Path source, Path target) throws XenonException; 
  
    public abstract void createDirectory(Path dir) throws XenonException;

    public abstract void createFile(Path path) throws XenonException;

    public abstract void delete(Path path) throws XenonException;

    public abstract boolean exists(Path path) throws XenonException;

    public abstract DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException;

    public abstract DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException;
   
    public abstract DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException;

    public abstract DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter) throws XenonException;

    public abstract InputStream newInputStream(Path path) throws XenonException;

    public abstract OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException;

    public abstract FileAttributes getAttributes(Path path) throws XenonException;

    public abstract Path readSymbolicLink(Path link) throws XenonException;

    public abstract void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException;

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract void end();
    
}

