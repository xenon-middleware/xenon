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
package nl.esciencecenter.xenon.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.Path;

/**
 * LocalDirectoryStream implements a {@link DirectoryStream} for local directories.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalDirectoryStream implements DirectoryStream<Path>, Iterator<Path> {

    /** The DirectoryStream from the underlying java.nio implementation */
    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;

    /** The Iterator from the underlying java.nio implementation. */
    private final Iterator<java.nio.file.Path> iterator;

    /** The filter to use. */
    private final DirectoryStream.Filter filter;

    /** The directory to produce a stream for. */
    private final Path dir;

    /** A buffer to read ahead. */
    private Path readAhead;

    LocalDirectoryStream(Path dir, DirectoryStream.Filter filter) throws CobaltException {
        try {
            this.dir = dir;
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
            this.filter = filter;
        } catch (IOException e) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Could not create directory stream for " + dir, e);
        }
    }

    private Path getPath(java.nio.file.Path path) {
        return new PathImplementation(dir.getFileSystem(), dir.getRelativePath().resolve(path.getFileName().toString()));
    }

    @Override
    public Iterator<Path> iterator() {
        return this;
    }

    @Override
    public void close() throws IOException {

        try {
            stream.close();
        } catch (IOException e) {
            // NOTE: No unit test possible here, as this does not occur for local file system.
            throw new IOException("Failed to close stream.", e);
        }
    }

    @Override
    public synchronized boolean hasNext() {

        if (readAhead != null) {
            return true;
        }

        while (iterator.hasNext()) {
            Path next = getPath(iterator.next());
            if (filter.accept(next)) {
                readAhead = next;
                return true;
            }
        }

        return false;
    }

    @Override
    public synchronized Path next() {

        if (readAhead != null) {
            Path tmp = readAhead;
            readAhead = null;
            return tmp;
        }

        while (iterator.hasNext()) {
            Path next = getPath(iterator.next());

            if (filter.accept(next)) {
                return next;
            }
        }

        throw new NoSuchElementException("No more files in directory");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove");
    }
}