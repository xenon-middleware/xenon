package nl.esciencecenter.octopus.util;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DeleteOption;
import nl.esciencecenter.octopus.files.Path;

public class Sandbox {

    private final Octopus octopus;

    private final Path path;

    private List<Pair> uploadFiles = new LinkedList<Pair>();

    private List<Pair> downloadFiles = new LinkedList<Pair>();

    public class Pair {

        final Path source;
        final Path destination;

        public Pair(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    public Sandbox(Octopus octopus, Path root, Path sandboxName) throws OctopusException {
        this.octopus = octopus;

        if (sandboxName == null) {
            path = root.resolve("octopus_sandbox_" + UUID.randomUUID());
        } else {
            path = root.resolve(sandboxName);
        }
    }

    public List<Pair> getUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(Path... files) {
        uploadFiles = new LinkedList<Pair>();
        for (int i = 0; i < files.length; i++) {
            addUploadFile(files[i]);
        }
    }

    public void addUploadFile(Path src) {
        addUploadFile(src, null);
    }

    public void addUploadFile(Path src, Path dest) {
        if (src == null) {
            throw new NullPointerException("the source file cannot be null when adding a preStaged file");
        }

        uploadFiles.add(new Pair(src, path.resolve(dest)));
    }

    public List<Pair> getDownloadFiles() {
        return downloadFiles;
    }

    public void setDownloadFiles(Path... files) {
        downloadFiles = new LinkedList<Pair>();
        for (int i = 0; i < files.length; i++) {
            addDownloadFile(files[i]);
        }
    }

    public void addDownloadFile(Path src) {
        addDownloadFile(src, null);
    }

    public void addDownloadFile(Path src, Path dest) {
        if (src == null) {
            throw new NullPointerException("the source file cannot be null when adding a postStaged file");
        }

        downloadFiles.add(new Pair(path.resolve(src), dest));
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
        octopus.files().delete(path, DeleteOption.RECURSIVE, DeleteOption.WIPE);
    }

    public void delete() throws OctopusIOException {
        octopus.files().delete(path, DeleteOption.RECURSIVE);
    }
}
