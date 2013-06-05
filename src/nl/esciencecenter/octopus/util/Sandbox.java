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

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;

public class Sandbox {

    private final Octopus octopus;

    private final AbsolutePath path;

    private List<Pair> uploadFiles = new LinkedList<Pair>();

    private List<Pair> downloadFiles = new LinkedList<Pair>();

    public class Pair {

        final AbsolutePath source;
        final AbsolutePath destination;

        public Pair(AbsolutePath source, AbsolutePath destination) {
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
     * @param octopus
     *            An Octopus instance
     * @param root
     *            Directory in which sandbox will be created.
     * @param sandboxName
     *            Name of the sandbox. If null a random name will be used.
     * @throws OctopusException
     * @throws OctopusIOException
     */
    public Sandbox(Octopus octopus, AbsolutePath root, String sandboxName) throws OctopusException, OctopusIOException {
        this.octopus = octopus;

        if (sandboxName == null) {
            path = root.resolve(new RelativePath("octopus_sandbox_" + UUID.randomUUID()));
        } else {
            path = root.resolve(new RelativePath(sandboxName));
        }
    }

    /**
     * @return Path in which files will be uploaded and downloaded.
     */
    public AbsolutePath getPath() {
        return path;
    }

    /**
     * @return List of files that will be uploaded on execution of {@Link #upload() upload} method.
     */
    public List<Pair> getUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(AbsolutePath... files) {
        uploadFiles = new LinkedList<Pair>();
        for (int i = 0; i < files.length; i++) {
            addUploadFile(files[i]);
        }
    }

    /**
     * Add a file to the list of files to upload.
     *
     * @param src
     *            Source path of file. Can not be null.
     */
    public void addUploadFile(AbsolutePath src) {
        addUploadFile(src, null);
    }

    /**
     * Add a file to the list of files to upload.
     *
     * @param src
     *            Where file should be uploaded from. Can not be null.
     * @param dest
     *            Name of file in sandbox. When null the src.getFilename() will be used.
     */
    public void addUploadFile(AbsolutePath src, String dest) {
        if (src == null) {
            throw new NullPointerException("the source path cannot be null when adding a preStaged file");
        }
        if (dest == null) {
            dest = src.getFileName();
        }

        uploadFiles.add(new Pair(src, path.resolve(new RelativePath(dest))));
    }

    /**
     * @return List of files that will be downloaded on execution of {@Link #download() download} method.
     */
    public List<Pair> getDownloadFiles() {
        return downloadFiles;
    }

    /**
     * Add file to the list of files to download.
     *
     * @param src
     *            Name of file in sandbox. When null the dest.getFilename() will be used.
     * @param dest
     *            Where file should be downloaded to. Can not be null.
     */
    public void addDownloadFile(String src, AbsolutePath dest) {
        if (dest == null) {
            throw new NullPointerException("the destination path cannot be null when adding a postStaged file");
        }
        if (src == null) {
            src = dest.getFileName();
        }

        downloadFiles.add(new Pair(path.resolve(new RelativePath(src)), dest));
    }

    private void copy(List<Pair> pairs, CopyOption... options) throws OctopusIOException, UnsupportedOperationException {
        for (Pair pair : pairs) {
            FileUtils.recursiveCopy(octopus, pair.source, pair.destination, options);
        }
    }

    /**
     * Copy uploaded files to sandbox.
     *
     * Creates sandbox directory when needed.
     *
     * @param options
     * @throws OctopusIOException
     * @throws UnsupportedOperationException
     */
    public void upload(CopyOption... options) throws OctopusIOException, UnsupportedOperationException {
        Files files = octopus.files();
        if (!files.exists(path)) {
            files.createDirectory(path);
        }
        copy(uploadFiles, options);
    }

    /**
     * Copy downloaded files from sandbox.
     *
     * @param options
     * @throws OctopusIOException
     * @throws UnsupportedOperationException
     */
    public void download(CopyOption... options) throws OctopusIOException, UnsupportedOperationException {
        copy(downloadFiles, options);
    }

    public void wipe() throws OctopusIOException {
        FileUtils.recursiveWipe(octopus, path);
    }

    /**
     * Deletes all files in sandbox.
     *
     * @throws OctopusIOException
     */
    public void delete() throws OctopusIOException {
        FileUtils.recursiveDelete(octopus, path);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((octopus == null) ? 0 : octopus.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (octopus == null) {
            if (other.octopus != null) {
                return false;
            }
        } else if (!octopus.equals(other.octopus)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
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
        builder.append("Sandbox [octopus=").append(octopus).append(", path=").append(path).append(", uploadFiles=")
                .append(uploadFiles).append(", downloadFiles=").append(downloadFiles).append("]");
        return builder.toString();
    }
}
