package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

// FIXME: use localdistream ? 
class LocalDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {

    /** LocalFiles to retrieve the attributes of a file */
    private final LocalFiles localFiles;

    /** The DirectoryStream from the underlying java.nio implementation */
    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;

    /** The Iterator from the underlying java.nio implementation. */
    private final Iterator<java.nio.file.Path> iterator;

    /** The filter to use. */
    private final DirectoryStream.Filter filter;

    /** A buffer to read ahead. */
    private final LinkedList<AbsolutePath> readAhead;

    /** The directory to produce a stream for. */
    private final AbsolutePath dir;

    LocalDirectoryAttributeStream(LocalFiles localFiles, AbsolutePath dir, DirectoryStream.Filter filter) throws OctopusIOException {
        this.localFiles = localFiles;
        this.dir = dir;
        this.filter = filter;
        this.readAhead = new LinkedList<AbsolutePath>();

        try {
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
        } catch (IOException e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Could not create directory stream.", e);
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
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Cannot close stream.", e);
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
                    readAhead.addLast(next);
                    return true;
                }
            }
            return false;
        } catch (OctopusIOException e) {
            throw new DirectoryIteratorException(LocalAdaptor.ADAPTOR_NAME, "Failed to get next element.", e);
        }
    }

    @Override
    public synchronized PathAttributesPair next() {
        try {
            if (!readAhead.isEmpty()) {
                AbsolutePath path = readAhead.removeFirst();
                FileAttributes attributes = localFiles.getAttributes(path);
                return new PathAttributesPairImplementation(path, attributes);
            }

            while (iterator.hasNext()) {
                AbsolutePath next = gatPath(iterator.next());
                if (filter.accept(next)) {
                    FileAttributes attributes = localFiles.getAttributes(next);
                    return new PathAttributesPairImplementation(next, attributes);
                }
            }
            throw new NoSuchElementException("No more files in directory.");
        } catch (OctopusIOException e) {
            throw new DirectoryIteratorException(LocalAdaptor.ADAPTOR_NAME, "Failed to get next element.", e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove.");
    }
}