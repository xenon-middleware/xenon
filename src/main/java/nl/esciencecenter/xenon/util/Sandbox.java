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
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;

/**
 * Sandbox represents a (possibly remote and usually temporary) directory used for running jobs. 
 * <p>
 * A Sandbox is created before the job is started. The input files (or directories) necessary to run the job are then
 * added to the Sandbox using {@link #addUploadFile(Path, String)}. Once all files have been added they can be uploaded to the 
 * Sandbox using {@link #upload(CopyOption...)}.
 * </p><p>
 * Similarly, the output files (or directories) produced by the job can be registered with the Sandbox using 
 * {@link #addDownloadFile(String, Path)}. These may be added before or after the job runs. 
 * </p><p>
 * Next the job is run using the sandbox as a working directory.
 * </p><p>
 * After the job has terminated, the output files can be downloaded using {@link #download(CopyOption...)}. 
 * </p><p>
 * Finally, the Sandbox can be deleted using {@link #delete()}. 
 * </p>       
 * 
 * @author Stefan Verhoeven <S.Verhoeven@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0
 */
public class Sandbox {

    private final Files files;

    private final Path path;

    private List<Pair> uploadFiles = new LinkedList<>();
    private final List<Pair> downloadFiles = new LinkedList<>();

    /**
     * Pair represents the combination of a source and destination path
     */
    public static class Pair {

        private final Path source;
        private final Path destination;

        public Pair(Path source, Path destination) {
            this.source = source;
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
     * @throws XenonException
     */
    public Sandbox(Files files, Path root, String sandboxName) throws XenonException {

        String name = sandboxName;

        if (files == null) {
            throw new XenonException("Sandbox", "Need an files interface to create a sandbox!");
        }

        if (root == null) {
            throw new XenonException("Sandbox", "Need an root directory to create a sandbox!");
        }

        if (name == null) {
            name = "xenon_sandbox_" + UUID.randomUUID();
        }

        this.files = files;
        this.path = resolve(files, root, name);
    }

    private static Path resolve(Files files, Path root, String path) throws XenonException {
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(path));
    }
    
    /**
     * The sandbox directory.
     * 
     * @return the sandbox directory.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the list of files that will be uploaded when calling {@link #upload(CopyOption []) upload}.
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
     */
    public void addUploadFile(Path src, String dest) throws XenonException {

        String destination = dest;

        if (src == null) {
            throw new IllegalArgumentException("the source path cannot be null when adding an upload file");
        }
        
        if (destination == null) {
            destination = src.getRelativePath().getFileNameAsString();
        } 

        uploadFiles.add(new Pair(src, resolve(files, path, destination)));
    }

    /**
     * Returns the list of files that will be downloaded when calling {@link #download(CopyOption []) download}.
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
     */
    public void addDownloadFile(String src, Path dest) throws XenonException {

        String source = src;
        
        if (dest == null) {
            throw new IllegalArgumentException("the destination path cannot be null when adding a download file");
        }
        
        if (source == null) {
            source = dest.getRelativePath().getFileNameAsString();
        }

        downloadFiles.add(new Pair(resolve(files, path, source), dest));
    }

    private void copy(List<Pair> pairs, CopyOption... options) throws XenonException {
        for (Pair pair : pairs) {
            Utils.recursiveCopy(files, pair.source, pair.destination, options);
        }
    }

    /**
     * Upload files to sandbox.
     * 
     * Also creates sandbox directory if it does not exist yet.
     * 
     * @param options
     *          the options to use while copying. See {@link CopyOption} for details.
     * @throws InvalidCopyOptionsException
     *           if an invalid combination of options is used.
     * @throws XenonException
     *           if an I/O error occurs during the copying
     */
    public void upload(CopyOption... options) throws XenonException {
        if (!files.exists(path)) {
            files.createDirectory(path);
        }
        copy(uploadFiles, options);
    }

    /**
     * Download files from sandbox.
     * 
     * @param options
     *          the options to use while copying. See {@link CopyOption} for details.
     * @throws InvalidCopyOptionsException
     *           if an invalid combination of options is used.
     * @throws XenonException
     *           if an I/O error occurs during the copying
     */
    public void download(CopyOption... options) throws XenonException {
        copy(downloadFiles, options);
    }

    /**
     * Recursively delete the sandbox.
     * 
     * @throws XenonException
     *         if an I/O error occurs during deletion 
     */
    public void delete() throws XenonException {
        Utils.recursiveDelete(files, path);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + files.hashCode();
        result = prime * result + path.hashCode();
        result = prime * result + uploadFiles.hashCode();
        result = prime * result + downloadFiles.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Sandbox other = (Sandbox) obj;
        return files.equals(other.files)
                && path.equals(other.path)
                && downloadFiles.equals(other.downloadFiles)
                && uploadFiles.equals(other.uploadFiles);
    }

    @Override
    public String toString() {
        return "Sandbox [files=" + files + ", path=" + path + ", uploadFiles=" +
                uploadFiles + ", downloadFiles=" + downloadFiles + "]";
    }
}
