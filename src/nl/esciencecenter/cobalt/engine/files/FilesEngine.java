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
package nl.esciencecenter.cobalt.engine.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltRuntimeException;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.Adaptor;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.files.Copy;
import nl.esciencecenter.cobalt.files.CopyOption;
import nl.esciencecenter.cobalt.files.CopyStatus;
import nl.esciencecenter.cobalt.files.DirectoryStream;
import nl.esciencecenter.cobalt.files.FileAttributes;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.files.OpenOption;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.PathAttributesPair;
import nl.esciencecenter.cobalt.files.PosixFilePermission;
import nl.esciencecenter.cobalt.files.RelativePath;
import nl.esciencecenter.cobalt.files.DirectoryStream.Filter;

/**
 * Engine for File operations. Implements functionality using File operations, Octopus create functions, and Adaptors' Files
 * object.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 */
public class FilesEngine implements Files {

    public static final DirectoryStream.Filter ACCEPT_ALL_FILTER = new DirectoryStream.Filter() {
        public boolean accept(Path file) {
            return true;
        }
    };

    private final CobaltEngine octopusEngine;

    public FilesEngine(CobaltEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    private Files getFilesAdaptor(FileSystem filesystem) {
        try {
            Adaptor adaptor = octopusEngine.getAdaptor(filesystem.getAdaptorName());
            return adaptor.filesAdaptor();
        } catch (CobaltException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot dissapear suddenly.
            // Therefore, we make this a runtime exception.
            throw new CobaltRuntimeException("FilesEngine", "Could not find adaptor named " + filesystem.getAdaptorName(), e);
        }
    }

    private Files getFilesAdaptor(Path path) throws CobaltException {
        return getFilesAdaptor(path.getFileSystem());
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws CobaltException {
        
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.filesAdaptor().newFileSystem(scheme, location, credential, properties);
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) throws CobaltException {
        return getFilesAdaptor(filesystem).newPath(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws CobaltException {
        getFilesAdaptor(filesystem).close(filesystem);
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws CobaltException {
        return getFilesAdaptor(filesystem).isOpen(filesystem);
    }

    @Override
    public void createDirectories(Path dir) throws CobaltException {
        getFilesAdaptor(dir).createDirectories(dir);
    }

    @Override
    public void createDirectory(Path dir) throws CobaltException {
        getFilesAdaptor(dir).createDirectory(dir);
    }

    @Override
    public void createFile(Path path) throws CobaltException {
        getFilesAdaptor(path).createFile(path);
    }

    @Override
    public void delete(Path path) throws CobaltException {
        getFilesAdaptor(path).delete(path);
    }

    @Override
    public boolean exists(Path path) throws CobaltException {
        return getFilesAdaptor(path).exists(path);
    }

    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws CobaltException {
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName())) {
            return getFilesAdaptor(source).copy(source, target, options);
        } else if (sourcefs.getAdaptorName().equals(CobaltEngine.LOCAL_ADAPTOR_NAME)) {
            return getFilesAdaptor(target).copy(source, target, options);
        } else if (targetfs.getAdaptorName().equals(CobaltEngine.LOCAL_ADAPTOR_NAME)) {
            return getFilesAdaptor(source).copy(source, target, options);
        } else {
            throw new CobaltException("FilesEngine", "Cannot do inter-scheme third party copy!");
        }
    }

    @Override
    public void move(Path source, Path target) throws CobaltException {

        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName())) {
            getFilesAdaptor(source).move(source, target);
            return;
        }

        throw new CobaltException("FilesEngine", "Cannot do inter-scheme third party move!");
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws CobaltException {
        return getFilesAdaptor(copy.getSource()).getCopyStatus(copy);
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws CobaltException {
        return getFilesAdaptor(copy.getSource()).cancelCopy(copy);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws CobaltException {
        return getFilesAdaptor(dir).newDirectoryStream(dir);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws CobaltException {
        return getFilesAdaptor(dir).newAttributesDirectoryStream(dir);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws CobaltException {
        return getFilesAdaptor(dir).newDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter)
            throws CobaltException {
        return getFilesAdaptor(dir).newAttributesDirectoryStream(dir, filter);
    }

    @Override
    public InputStream newInputStream(Path path) throws CobaltException {
        return getFilesAdaptor(path).newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws CobaltException {
        return getFilesAdaptor(path).newOutputStream(path, options);
    }

    @Override
    public FileAttributes getAttributes(Path path) throws CobaltException {
        return getFilesAdaptor(path).getAttributes(path);
    }

    @Override
    public Path readSymbolicLink(Path link) throws CobaltException {
        return getFilesAdaptor(link).readSymbolicLink(link);
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws CobaltException {
        getFilesAdaptor(path).setPosixFilePermissions(path, permissions);
    }
    
    @Override
    public String toString() {
        return "FilesEngine [octopusEngine=" + octopusEngine + "]";
    }
}
