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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.RelativePath;

class LocalDirectoryStream implements DirectoryStream<AbsolutePath>, Iterator<AbsolutePath> {

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

    LocalDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter) throws OctopusIOException {
        try {
            this.dir = dir;
            stream = Files.newDirectoryStream(LocalUtils.javaPath(dir));
            iterator = stream.iterator();
            this.filter = filter;
            this.readAhead = new LinkedList<AbsolutePath>();

        } catch (IOException e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Could not create directory stream for " + dir.getPath(), e);
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
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to close stream.", e);
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
                readAhead.addLast(next);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized AbsolutePath next() {
        if (!readAhead.isEmpty()) {
            return readAhead.removeFirst();
        }

        while (iterator.hasNext()) {
            AbsolutePath next = getPath(iterator.next());
            if (filter.accept(next)) {
                return next;
            }
        }
        throw new NoSuchElementException("No more files in directory");

    }

    @Override
    public synchronized void remove() {
        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove");
    }
}