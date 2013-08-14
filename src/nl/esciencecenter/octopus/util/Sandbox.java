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
package nl.esciencecenter.octopus.util;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Pathname;

/**
 * A sandbox is a (possibly remote and usually temporary) directory used for running jobs. 
 * <p>
 * A sandbox is typically create before the job is started. The input files (or directories) necessary to run the job are then 
 * uploaded to the sandbox.
 * </p>
 * <p>
 * Next the job is run using the sandbox as a working directory.
 * </p>
 * <p>
 * After the job has terminated, the output files downloaded from the sandbox, and the sandbox is deleted.
 * </p>       
 * 
 * Example to submit a job with input and output files:
 * 
 * <blockquote>
 * 
 * <pre class="code">
 * {
 *     Sandbox sandbox = new Sandbox(octopus, sandboxBase);
 *     sandbox.addUploadFile(inputfile);
 *     sandbox.addDownloadFile(outputfile);
 * 
 *     sandbox.upload();
 * 
 *     Job job = octopus.jobs().submitJob(description);
 * 
 *     JobStatus = octopus.jobs().waitUntilDone(job, 60000);
 * 
 *     sandbox.download();
 *     sandbox.delete();
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Stefan Verhoeven <S.Verhoeven@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0
 */
public class Sandbox {

    private final Files files;

    private final Path path;

    private List<Pair> uploadFiles = new LinkedList<Pair>();
    private List<Pair> downloadFiles = new LinkedList<Pair>();

    /**
     * Pair represents the combination of a source and destination path
     */
    public static class Pair {

        final Path source;
        final Path destination;

        public Pair(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
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
            StringBuilder builder = new StringBuilder();
            builder.append("Pair [source=").append(source).append(", destination=").append(destination).append("]");
            return builder.toString();
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
     * @throws OctopusException
     * @throws OctopusIOException
     */
    public Sandbox(Files files, Path root, String sandboxName) throws OctopusException, OctopusIOException {

        if (files == null) {
            throw new OctopusException("Sandbox", "Need an files interface to create a sandbox!");
        }

        if (root == null) {
            throw new OctopusException("Sandbox", "Need an root directory to create a sandbox!");
        }

        if (sandboxName == null) {
            sandboxName = "octopus_sandbox_" + UUID.randomUUID();
        }

        this.files = files;
        this.path = root.resolve(new Pathname(sandboxName));
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
     */
    public void setUploadFiles(Path... files) {
        uploadFiles = new LinkedList<Pair>();
        for (int i = 0; i < files.length; i++) {
            addUploadFile(files[i]);
        }
    }

    /**
     * Add a file to the list of files to upload.
     * 
     * @param src
     *            Source path of file. May not be <code>null</code>.
     */
    public void addUploadFile(Path src) {
        addUploadFile(src, null);
    }

    /**
     * Add a file to the list of files to upload.
     * 
     * @param src
     *            The source file. May not be <code>null</code>.
     * @param dest
     *            The name of file in the sandbox. If <code>null</code> then <code>src.getFilename()</code> will be used.
     */
    public void addUploadFile(Path src, String dest) {
        if (src == null) {
            throw new IllegalArgumentException("the source path cannot be null when adding an upload file");
        }
        if (dest == null) {
            dest = src.getFileName();
        }

        uploadFiles.add(new Pair(src, path.resolve(new Pathname(dest))));
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
     */
    public void addDownloadFile(String src, Path dest) {
        if (dest == null) {
            throw new IllegalArgumentException("the destination path cannot be null when adding a download file");
        }
        if (src == null) {
            src = dest.getFileName();
        }

        downloadFiles.add(new Pair(path.resolve(new Pathname(src)), dest));
    }

    private void copy(List<Pair> pairs, CopyOption... options) throws OctopusIOException, UnsupportedOperationException {
        for (Pair pair : pairs) {
            FileUtils.recursiveCopy(files, pair.source, pair.destination, options);
        }
    }

    /**
     * Upload files to sandbox.
     * 
     * Also creates sandbox directory if it does not exist yet.
     * 
     * @param options
     *          the options to use while copying. See {@link CopyOption} for details.
     * @throws UnsupportedOperationException
     *           if an invalid combination of options is used.
     * @throws OctopusIOException
     *           if an I/O error occurs during the copying
     */
    public void upload(CopyOption... options) throws OctopusIOException, UnsupportedOperationException {
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
     * @throws UnsupportedOperationException
     *           if an invalid combination of options is used.
     * @throws OctopusIOException
     *           if an I/O error occurs during the copying
     */
    public void download(CopyOption... options) throws OctopusIOException, UnsupportedOperationException {
        copy(downloadFiles, options);
    }

    /**
     * Recursively delete the sandbox.
     * 
     * @throws OctopusIOException
     *         if an I/O error occurs during deletion 
     */
    public void delete() throws OctopusIOException {
        FileUtils.recursiveDelete(files, path);
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Sandbox other = (Sandbox) obj;

        if (!files.equals(other.files)) {
            return false;
        }

        if (!path.equals(other.path)) {
            return false;
        }

        if (!downloadFiles.equals(other.downloadFiles)) {
            return false;
        }

        if (!uploadFiles.equals(other.uploadFiles)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sandbox [files=").append(files).append(", path=").append(path).append(", uploadFiles=")
                .append(uploadFiles).append(", downloadFiles=").append(downloadFiles).append("]");
        return builder.toString();
    }
}
