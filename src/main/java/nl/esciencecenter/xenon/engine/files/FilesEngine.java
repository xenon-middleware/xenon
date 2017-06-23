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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.InvalidAdaptorException;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonRuntimeException;
import nl.esciencecenter.xenon.adaptors.file.file.LocalFileAdaptorFactory;
import nl.esciencecenter.xenon.adaptors.file.ftp.FtpFileAdaptorFactory;
import nl.esciencecenter.xenon.adaptors.file.sftp.SftpFileAdaptorFactory;
import nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptorFactory;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.DuplicateAdaptorException;
import nl.esciencecenter.xenon.engine.util.CopyEngine;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAdaptorDescription;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.FileSystemClosedException;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * Engine for File operations. Implements functionality using File operations, Xenon create functions, and Adaptors' Files
 * object.
 * 
 */
public class FilesEngine implements Files {
    
    /** The name of this component, for use in exceptions */
    private static final String COMPONENT_NAME = "FilesEngine";
    
    // TODO: make this configurable!!
    /** Factories for all supported file adaptors */
    private static final FileAdaptorFactory [] ADAPTOR_FACTORIES = new FileAdaptorFactory [] { 
            new LocalFileAdaptorFactory(),
            new SftpFileAdaptorFactory(),
            new FtpFileAdaptorFactory(), 
            new WebdavFileAdaptorFactory()
    };    
    
    public static final DirectoryStream.Filter ACCEPT_ALL_FILTER = new DirectoryStream.Filter() {
        public boolean accept(Path file) {
            return true;
        }
    };
    
    private final HashMap<String, FileAdaptor> adaptors = new HashMap<>();
    
    private final CopyEngine copyEngine;
    
    public FilesEngine() throws XenonException {
        this.copyEngine = new CopyEngine(this);
        loadAdaptors();
    }
    
    private void loadAdaptors() throws XenonException {
        
        for (FileAdaptorFactory a : ADAPTOR_FACTORIES) {
        	
        	FileAdaptor adaptor = a.createAdaptor(this);
        	
        	String name = adaptor.getName();
            
            if (adaptors.containsKey(name)) { 
                throw new DuplicateAdaptorException(COMPONENT_NAME, "File adaptor " + name + " already exists!");
            }
            
            adaptors.put(name, adaptor);	
        }
    }
     
    public FileAdaptorDescription [] getAdaptorDescriptions() { 
        ArrayList<FileAdaptorDescription> tmp = new ArrayList<>();
        
        for (FileAdaptor a : adaptors.values()) { 
            tmp.add(a.getAdaptorDescription());
        }
        
        return tmp.toArray(new FileAdaptorDescription[tmp.size()]);
    }

    public FileAdaptorDescription getAdaptorDescription(String adaptorName) throws InvalidAdaptorException { 
        return getFileAdaptorByName(adaptorName).getAdaptorDescription();
    }
    
    private FileAdaptor getFileAdaptorByName(String adaptorName) throws InvalidAdaptorException {
       
    	if (adaptorName == null) {
            throw new InvalidAdaptorException(COMPONENT_NAME, "Adaptor name may not be null");
    	}
    	
    	
    	FileAdaptor adaptor = adaptors.get(adaptorName);
        
        if (adaptor == null) { 
            throw new InvalidAdaptorException(COMPONENT_NAME, "Could not find file adaptor named " + adaptorName);
        }
        
        return adaptor;
    }
        
    private FileAdaptor getFileAdaptorFromEngine(FileSystem filesystem) {
        try { 
            return getFileAdaptorByName(filesystem.getAdaptorName());
        } catch (InvalidAdaptorException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot disappear suddenly.
            // Therefore, we make this a runtime exception.
            throw new XenonRuntimeException(COMPONENT_NAME, "Could not find file adaptor named " + filesystem.getAdaptorName(), e);
        }
    }
        
    private FileAdaptor getFileAdaptor(FileSystem filesystem) throws XenonException {        

        FileAdaptor adaptor = getFileAdaptorFromEngine(filesystem);
        
        if (!adaptor.isOpen(filesystem)) {
            throw new FileSystemClosedException(COMPONENT_NAME, "FileSystem " + filesystem.getLocation() + " is closed");
        }
        
        return adaptor;
    }

    private FileAdaptor getFileAdaptor(Path path) throws XenonException {
        return getFileAdaptor(path.getFileSystem());
    }
     
    public CopyEngine getCopyEngine() {
        return copyEngine;
    }
    
    @Override
    public FileSystem newFileSystem(String adaptorName, String location, Credential credential, Map<String, String> properties) 
            throws XenonException {
        
        return getFileAdaptorByName(adaptorName).newFileSystem(location, credential, properties);
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) throws XenonException {
        return getFileAdaptor(filesystem).newPath(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws XenonException {
        getFileAdaptor(filesystem).close(filesystem);
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws XenonException {
        return getFileAdaptorFromEngine(filesystem).isOpen(filesystem);
    }

    @Override
    public void createDirectories(Path dir) throws XenonException {
        getFileAdaptor(dir).createDirectories(dir);
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
        getFileAdaptor(dir).createDirectory(dir);
    }

    @Override
    public void createFile(Path path) throws XenonException {
        getFileAdaptor(path).createFile(path);
    }

    @Override
    public void delete(Path path) throws XenonException {
        getFileAdaptor(path).delete(path);
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        return getFileAdaptor(path).exists(path);
    }

    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName()) ||
                targetfs.getAdaptorName().equals(Xenon.LOCAL_FILE_ADAPTOR_NAME)) {
        	
        	System.out.println("COPY A");
        	
            return getFileAdaptor(source).copy(source, target, options);
        } else if (sourcefs.getAdaptorName().equals(Xenon.LOCAL_FILE_ADAPTOR_NAME)) {
            
        	System.out.println("COPY B");
        	
        	return getFileAdaptor(target).copy(source, target, options);
        } else {
            throw new XenonException(COMPONENT_NAME, "Cannot do inter-scheme third party copy!");
        }
    }

    @Override
    public void move(Path source, Path target) throws XenonException {

        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName())) {
            getFileAdaptor(source).move(source, target);
            return;
        }

        throw new XenonException(COMPONENT_NAME, "Cannot do inter-scheme third party move!");
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {
        return getFileAdaptor(copy.getSource()).getCopyStatus(copy);
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws XenonException {
        return getFileAdaptor(copy.getSource()).cancelCopy(copy);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        return getFileAdaptor(dir).newDirectoryStream(dir);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
        return getFileAdaptor(dir).newAttributesDirectoryStream(dir);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
        return getFileAdaptor(dir).newDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter)
            throws XenonException {
        return getFileAdaptor(dir).newAttributesDirectoryStream(dir, filter);
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        return getFileAdaptor(path).newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        return getFileAdaptor(path).newOutputStream(path, options);
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        return getFileAdaptor(path).getAttributes(path);
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        return getFileAdaptor(link).readSymbolicLink(link);
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        getFileAdaptor(path).setPosixFilePermissions(path, permissions);
    }
    
    @Override
    public String toString() {
        return "FilesEngine";
    }
    
    public void end() { 
        copyEngine.done();
    
        // TODO: close all filesystems
    }    
}
