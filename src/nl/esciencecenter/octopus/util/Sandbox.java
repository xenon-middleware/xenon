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
import nl.esciencecenter.octopus.files.AbsolutePath;
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
    }

    public Sandbox(Octopus octopus, AbsolutePath root, String sandboxName) throws OctopusException {
        this.octopus = octopus;

        if (sandboxName == null) {
            path = root.resolve(new RelativePath("octopus_sandbox_" + UUID.randomUUID()));
        } else {
            path = root.resolve(new RelativePath(sandboxName));
        }
    }

    public List<Pair> getUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(AbsolutePath... files) {
        uploadFiles = new LinkedList<Pair>();
        for (int i = 0; i < files.length; i++) {
            addUploadFile(files[i]);
        }
    }

    public void addUploadFile(AbsolutePath src) {
        addUploadFile(src, null);
    }

    public void addUploadFile(AbsolutePath src, String dest) {
        if (src == null) {
            throw new NullPointerException("the source file cannot be null when adding a preStaged file");
        }

        uploadFiles.add(new Pair(src, path.resolve(new RelativePath(dest))));
    }

    public List<Pair> getDownloadFiles() {
        return downloadFiles;
    }

    public void setDownloadFiles(String... files) {
        downloadFiles = new LinkedList<Pair>();
        for (int i = 0; i < files.length; i++) {
            addDownloadFile(files[i]);
        }
    }

    public void addDownloadFile(String src) {
        addDownloadFile(src, null);
    }

    public void addDownloadFile(String src, AbsolutePath dest) {
        if (src == null) {
            throw new NullPointerException("the source file cannot be null when adding a postStaged file");
        }

        downloadFiles.add(new Pair(path.resolve(new RelativePath(src)), dest));
    }

    private void copy(List<Pair> pairs) throws OctopusIOException {

        for (Pair pair : pairs) {
            FileUtils.recursiveCopy(octopus, pair.source, pair.destination, CopyOption.COPY_ATTRIBUTES);
        }
    }

    public void upload() throws OctopusIOException {
        copy(uploadFiles);
    }

    public void download() throws OctopusIOException {
        copy(downloadFiles);
    }

    public void wipe() throws OctopusIOException {
        FileUtils.recursiveWipe(octopus, path);
    }

    public void delete() throws OctopusIOException {
        FileUtils.recursiveDelete(octopus, path);
    }
}
