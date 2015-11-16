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
package nl.esciencecenter.xenon.adaptors.generic;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.Path;

public abstract class DirectoryStreamBase<I, O> implements DirectoryStream<O>, Iterator<O> {

    private final Deque<O> stream;

    public DirectoryStreamBase(Path dir, DirectoryStream.Filter filter, List<I> listing) throws XenonException {
        stream = new LinkedList<>();
        for (I entry : listing) {
            String filename = getFileNameFromEntry(entry, dir);
            if (".".equals(filename) || "..".equals(filename)) {
                // filter out the "." and ".."
            } else {
                Path entryPath = new PathImplementation(dir.getFileSystem(), dir.getRelativePath().resolve(filename));
                if (filter.accept(entryPath)) {
                    O streamElement = getStreamElementFromEntry(entry, entryPath);
                    stream.add(streamElement);
                }
            }
        }
    }

    protected abstract O getStreamElementFromEntry(I entry, Path entryPath) throws XenonException;

    protected abstract String getFileNameFromEntry(I entry, Path parentPath);

    @Override
    public Iterator<O> iterator() {
        return this;
    }

    @Override
    public synchronized void close() throws IOException {
        stream.clear();
    }

    @Override
    public synchronized boolean hasNext() {
        return (stream.size() > 0);
    }

    @Override
    public synchronized O next() {

        if (stream.size() > 0) {
            return stream.removeFirst();
        }

        throw new NoSuchElementException("No more files in directory");
    }

    @Override
    public synchronized void remove() {
        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove");
    }
}