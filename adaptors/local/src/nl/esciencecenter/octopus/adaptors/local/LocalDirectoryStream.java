package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;

class LocalDirectoryStream implements DirectoryStream<AbsolutePath>, Iterator<AbsolutePath> {

    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;

    private final Iterator<java.nio.file.Path> iterator;

    private final DirectoryStream.Filter filter;

    private final ArrayList<AbsolutePath> readAhead;

    private final AbsolutePath dir;

    LocalDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter) throws OctopusIOException {
        try {
            this.dir = dir;
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
            this.filter = filter;
            this.readAhead = new ArrayList<AbsolutePath>();

        } catch (IOException e) {
            throw new OctopusIOException("LocalDirectoryStream", "could not create directory stream", e);
        }
    }

    private AbsolutePath getPath(java.nio.file.Path path) {
        return dir.resolve(new RelativePath(path.getFileName().toString()));
    }

    @Override
    public Iterator<AbsolutePath> iterator() {
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
                AbsolutePath next = getPath(iterator.next());
                if (filter.accept(next)) {
                    readAhead.add(next);
                    return true;
                }
            }
            return false;
    }

    @Override
    public synchronized AbsolutePath next() {
        if (!readAhead.isEmpty()) {
            return readAhead.remove(0);
        }

        while (iterator.hasNext()) {
            AbsolutePath next = getPath(iterator.next());
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