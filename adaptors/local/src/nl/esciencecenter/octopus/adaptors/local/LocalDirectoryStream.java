package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.Path;

class LocalDirectoryStream implements DirectoryStream<Path>, Iterator<Path> {

    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;

    private final Iterator<java.nio.file.Path> iterator;

    private final DirectoryStream.Filter filter;

    private final ArrayList<Path> readAhead;

    private final Path dir;

    LocalDirectoryStream(Path dir, DirectoryStream.Filter filter) throws OctopusIOException {
        try {
            this.dir = dir;
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
            this.filter = filter;
            this.readAhead = new ArrayList<Path>();

        } catch (IOException e) {
            throw new OctopusIOException("LocalDirectoryStream", "could not create directory stream", e);
        }
    }

    private Path getPath(java.nio.file.Path path) {
        return dir.resolve(path.getFileName().toString());
    }

    @Override
    public Iterator<Path> iterator() {
        return this;
    }

    @Override
    public void close() throws OctopusIOException {
        try {
            stream.close();
        } catch (IOException e) {
            throw new OctopusIOException("LocalDirectoryStream", "Cannot close stream", e);
        }
    }

    @Override
    public synchronized boolean hasNext() {
            if (!readAhead.isEmpty()) {
                return true;
            }
            while (iterator.hasNext()) {
                Path next = getPath(iterator.next());
                if (filter.accept(next)) {
                    readAhead.add(next);
                    return true;
                }
            }
            return false;
    }

    @Override
    public synchronized Path next() {
        if (!readAhead.isEmpty()) {
            return readAhead.remove(0);
        }

        while (iterator.hasNext()) {
            Path next = getPath(iterator.next());
            if (filter.accept(next)) {
                return next;
            }
        }
        throw new NoSuchElementException("no more files in directory");

    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException("LocalDirectoryStream", "DirectoryStream iterator does not support remove");
    }
}