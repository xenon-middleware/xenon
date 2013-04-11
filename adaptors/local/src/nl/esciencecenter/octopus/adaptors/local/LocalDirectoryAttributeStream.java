package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

class LocalDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {

    private final LocalFiles localFiles;

    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;

    private final Iterator<java.nio.file.Path> iterator;

    private final DirectoryStream.Filter filter;

    private final ArrayList<AbsolutePath> readAhead;

    private final AbsolutePath dir;

    LocalDirectoryAttributeStream(LocalFiles localFiles, AbsolutePath dir, DirectoryStream.Filter filter) throws OctopusIOException {
        this.localFiles = localFiles;
        this.dir = dir;
        this.filter = filter;
        this.readAhead = new ArrayList<AbsolutePath>();

        try {
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "could not create directory stream", e);
        }
    }

    private AbsolutePath gatPath(java.nio.file.Path path) throws OctopusIOException {
        return dir.resolve(new RelativePath(path.getFileName().toString()));
    }

    @Override
    public Iterator<PathAttributesPair> iterator() {
        return this;
    }

    @Override
    public void close() throws OctopusIOException {
        try {
            stream.close();
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Cannot close stream", e);
        }
    }

    @Override
    public synchronized boolean hasNext() {
        try {
            if (!readAhead.isEmpty()) {
                return true;
            }
            while (iterator.hasNext()) {
                AbsolutePath next = gatPath(iterator.next());
                if (filter.accept(next)) {
                    readAhead.add(next);
                    return true;
                }
            }
            return false;
        } catch (OctopusIOException e) {
            throw new DirectoryIteratorException(getClass().getName(), "error on getting next element", e);
        }
    }

    @Override
    public synchronized PathAttributesPair next() {
        try {
            if (!readAhead.isEmpty()) {
                AbsolutePath path = readAhead.remove(0);
                FileAttributes attributes = this.localFiles.getAttributes(path);
                return new PathAttributesPairImplementation(path, attributes);
            }

            while (iterator.hasNext()) {
                AbsolutePath next = gatPath(iterator.next());
                if (filter.accept(next)) {
                    FileAttributes attributes = this.localFiles.getAttributes(next);
                    return new PathAttributesPairImplementation(next, attributes);
                }
            }
            throw new NoSuchElementException("no more files in directory");
        } catch (OctopusIOException e) {
            throw new DirectoryIteratorException(getClass().getName(), "error on getting next element", e);
        }
    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException(getClass().getName(), "DirectoryStream iterator does not support remove");
    }
}