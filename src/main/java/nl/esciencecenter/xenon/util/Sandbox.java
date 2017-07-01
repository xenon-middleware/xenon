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
package nl.esciencecenter.xenon.util;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.CopyMode;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidOptionsException;
import nl.esciencecenter.xenon.files.Path;

/**
 * Sandbox represents a (possibly remote and usually temporary) directory used for running jobs. 
 * <p>
 * A Sandbox is created before the job is started. The input files (or directories) necessary to run the job are then
 * added to the Sandbox using {@link #addUploadFile(Path, String)}. Once all files have been added they can be uploaded to the 
 * Sandbox using {@link #upload(CopyMode...)}.
 * </p><p>
 * Similarly, the output files (or directories) produced by the job can be registered with the Sandbox using 
 * {@link #addDownloadFile(String, Path)}. These may be added before or after the job runs. 
 * </p><p>
 * Next the job is run using the sandbox as a working directory.
 * </p><p>
 * After the job has terminated, the output files can be downloaded using {@link #download(CopyMode...)}. 
 * </p><p>
 * Finally, the Sandbox can be deleted using {@link #delete()}. 
 * </p>       
 * 
 * @version 1.0 
 * @since 1.0
 */
public class Sandbox {

    private final FileSystem sourceFS;
    private final Path sourceRoot;

    private final FileSystem targetFS;
    private final Path targetRoot;
    
    private final Path path;
    
    private List<Pair> uploadFiles = new LinkedList<>();
    private final List<Pair> downloadFiles = new LinkedList<>();

    /**
     * Pair represents the combination of a source and destination path
     */
    public static class Pair {

        private final FileSystem sourceFS;
        private final Path source;
        
        private final FileSystem destinationFS;
        private final Path destination;

        public Pair(FileSystem sourceFS, Path source, FileSystem destinationFS, Path destination) {
        	this.sourceFS = sourceFS;
            this.source = source;
            this.destinationFS = destinationFS;
            this.destination = destination;
        }

        public Path getSource() {
            return source;
        }

        public Path getDestination() {
            return destination;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((destination == null) ? 0 : destination.hashCode());
            result = prime * result + ((source == null) ? 0 : source.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Pair other = (Pair) obj;
            if (destination == null) {
                if (other.destination != null) {
                    return false;
                }
            } else if (!destination.equals(other.destination)) {
                return false;
            }
            if (source == null) {
                if (other.source != null) {
                    return false;
                }
            } else if (!source.equals(other.source)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Pair [source=" + source + ", destination=" + destination + "]";
        }
    }

    /**
     * Creates a sandbox. 
     * 
     * Root and sandboxName will be concatenated to a path into which files can be uploaded/downloaded.
     * 
     * @param files
     *            A Files interface used to access the files.
     * @param root
     *            Directory in which sandbox will be created.
     * @param sandboxName
     *            Name of the sandbox. If null a random name will be used.
     * @throws XenonException
     *            If an I/O error occurred.
     */
    public Sandbox(FileSystem sourceFS, Path sourceRoot, FileSystem targetFS, Path targetRoot, String sandboxName) throws XenonException {

        String name = sandboxName;

        if (sourceFS == null) {
            throw new XenonException("Sandbox", "Need an source filesystem to create a sandbox!");
        }

        if (sourceRoot == null) {
            throw new XenonException("Sandbox", "Need an source root directory to create a sandbox!");
        }
        
        if (targetFS == null) {
            throw new XenonException("Sandbox", "Need an target filesystem to create a sandbox!");
        }

        if (targetRoot == null) {
            throw new XenonException("Sandbox", "Need an target root directory to create a sandbox!");
        }
        
        if (name == null) {
            name = "xenon_sandbox_" + UUID.randomUUID();
        }

        this.sourceFS = sourceFS;
        this.targetFS = targetFS;
        
        this.sourceRoot = sourceRoot;
        this.targetRoot = targetRoot;
        
        path = targetRoot.resolve(name); 
    }

//    private Path resolve(Path root, String path) throws XenonException {
//        return fs.newPath(root.getRelativePath().resolve(path));
//    }
    
    /**
     * The sandbox directory.
     * 
     * @return the sandbox directory.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the list of files that will be uploaded when calling {@link #upload(CopyMode []) upload}.
     * 
     * @return list of files that will be uploaded.
     */
    public List<Pair> getUploadFiles() {
        return uploadFiles;
    }

    /**
     * Sets the list of files that will be uploaded to <code>files</code>. 
     * 
     * Any existing upload files will be discarded.
     * 
     * @param files the files to upload.
     * @throws XenonException
     *                   If an I/O error occurred.
     */
    public void setUploadFiles(Path... files) throws XenonException {
        uploadFiles = new LinkedList<>();
        for (Path file : files) {
            addUploadFile(file);
        }
    }

    /**
     * Add a file to the list of files to upload.
     * 
     * @param src
     *            Source path of file. May not be <code>null</code>.
     * @throws XenonException 
     *            If an I/O error occurred.
     */
    public void addUploadFile(Path src) throws XenonException {
        addUploadFile(src, null);
    }

    /**
     * Add a file to the list of files to upload.
     * 
     * @param src
     *            The source file. May not be <code>null</code>.
     * @param dest
     *            The name of file in the sandbox. If <code>null</code> then <code>src.getFilename()</code> will be used.
     * @throws XenonException
     *            If an I/O error occurred. 
     */
    public void addUploadFile(Path src, String dest) throws XenonException {

        String destination = dest;

        if (src == null) {
            throw new IllegalArgumentException("the source path cannot be null when adding an upload file");
        }
        
        if (destination == null) {
            destination = src.getFileNameAsString();
        } 

        uploadFiles.add(new Pair(sourceFS, src, targetFS, path.resolve(destination)));
    }

    /**
     * Returns the list of files that will be downloaded when calling {@link #download(CopyMode []) download}.
     * 
     * @return list of files that will be downloaded.
     */
    public List<Pair> getDownloadFiles() {
        return downloadFiles;
    }

    /**
     * Add a file to the list of files to download.
     * 
     * @param src
     *            Name of the source file in the sandbox. When <code>null</code> the <code>dest.getFilename()</code> will be used.
     * @param dest
     *            The target file. May not be <code>null</code>.
     * @throws XenonException
     *            If an I/O error occurred.  
     */
    public void addDownloadFile(String src, Path dest) throws XenonException {

        String source = src;
        
        if (dest == null) {
            throw new IllegalArgumentException("the destination path cannot be null when adding a download file");
        }
        
        if (source == null) {
            source = dest.getFileNameAsString();
        }

        downloadFiles.add(new Pair(targetFS, path.resolve(source), sourceFS, dest));
    }

    private void copy(List<Pair> pairs, CopyMode option) throws XenonException {
        for (Pair pair : pairs) {
        	// TODO: reimplement!
        	// FileSystemUtil.recursiveCopy(pair.sourceFS, pair.source, pair.destinationFS, pair.destination, option);
        }
    }

    /**
     * Upload files to sandbox.
     * 
     * Also creates sandbox directory if it does not exist yet.
     * 
     * @param options
     *          the options to use while copying. See {@link CopyMode} for details.
     * @throws InvalidOptionsException
     *           if an invalid combination of options is used.
     * @throws XenonException
     *           if an I/O error occurs during the copying
     */
    public void upload(CopyMode option) throws XenonException {
    	
        if (!targetFS.exists(path)) {
            targetFS.createDirectory(path);
        }
        
        copy(uploadFiles, option);
    }

    /**
     * Download files from sandbox.
     * 
     * @param options
     *          the options to use while copying. See {@link CopyMode} for details.
     * @throws InvalidOptionsException
     *           if an invalid combination of options is used.
     * @throws XenonException
     *           if an I/O error occurs during the copying
     */
    public void download(CopyMode option) throws XenonException {
        copy(downloadFiles, option);
    }

    /**
     * Recursively delete the sandbox.
     * 
     * @throws XenonException
     *         if an I/O error occurs during deletion 
     */
    public void delete() throws XenonException {
    	// TODO: reimplement
    	// FileSystemUtil.recursiveDelete(targetFS, path);
    }
    
    @Override
    public String toString() {
        return "Sandbox [source filesystem=" + sourceFS + ", source path=" + sourceRoot + "destination filesystem=" + targetFS + ", destination path=" + path +  
        		", uploadFiles=" + uploadFiles + ", downloadFiles=" + downloadFiles + "]";
    }
}
