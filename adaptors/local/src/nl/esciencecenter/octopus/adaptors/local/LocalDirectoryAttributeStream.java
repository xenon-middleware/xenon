package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.engine.files.AbstractPathAttributes;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;

class LocalDirectoryAttributeStream implements DirectoryStream<PathAttributes>, Iterator<PathAttributes> {

    private final LocalFiles localFiles;

    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;

    private final Iterator<java.nio.file.Path> iterator;

    private final DirectoryStream.Filter filter;

    private final ArrayList<Path> readAhead;

    private final Path dir;

    LocalDirectoryAttributeStream(LocalFiles localFiles, Path dir, DirectoryStream.Filter filter) throws OctopusException {
        this.localFiles = localFiles;
        this.dir = dir;
        this.filter = filter;
        this.readAhead = new ArrayList<Path>();

        try {
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
        } catch (IOException e) {
            throw new OctopusException("could not create directory stream", e, null, null);
        }
    }

    private Path gatPath(java.nio.file.Path path) throws OctopusException {
        return dir.resolve(path.getFileName().toString());
    }

    @Override
    public Iterator<PathAttributes> iterator() {
        return this;
    }

    @Override
    public void close() throws OctopusException {
        try {
            stream.close();
        } catch (IOException e) {
            throw new OctopusException("Cannot close stream", e, null, null);
        }
    }

    @Override
    public synchronized boolean hasNext() {
        try {
            if (!readAhead.isEmpty()) {
                return true;
            }
            while (iterator.hasNext()) {
                Path next = gatPath(iterator.next());
                if (filter.accept(next)) {
                    readAhead.add(next);
                    return true;
                }
            }
            return false;
        } catch (OctopusException e) {
            throw new DirectoryIteratorException("error on getting next element", e, "local", dir.toUri());
        }
    }

    @Override
    public synchronized PathAttributes next() {
        try {
            if (!readAhead.isEmpty()) {
                Path path = readAhead.remove(0);
                FileAttributes attributes = this.localFiles.readAttributes(path);
                return new AbstractPathAttributes(path, attributes);
            }

            while (iterator.hasNext()) {
                Path next = gatPath(iterator.next());
                if (filter.accept(next)) {
                    FileAttributes attributes = this.localFiles.readAttributes(next);
                    return new AbstractPathAttributes(next, attributes);
                }
            }
            throw new NoSuchElementException("no more files in directory");
        } catch (OctopusException e) {
            throw new DirectoryIteratorException("error on getting next element", e, "local", dir.toUri());
        }
    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException("DirectoryStream iterator does not support remove", "local",
                dir.toUri());

    }
}