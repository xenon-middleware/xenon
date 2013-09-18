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
package nl.esciencecenter.cobalt.adaptors.ssh;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.cobalt.engine.files.PathImplementation;
import nl.esciencecenter.cobalt.files.DirectoryStream;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.PathAttributesPair;

import com.jcraft.jsch.ChannelSftp.LsEntry;

class SshDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {

    private final Deque<PathAttributesPair> stream;

    SshDirectoryAttributeStream(Path dir, DirectoryStream.Filter filter, List<LsEntry> listing) throws CobaltException {

        stream = new LinkedList<PathAttributesPair>();

        for (LsEntry e : listing) {

            String filename = e.getFilename();

            if (filename.equals(".") || filename.equals("..")) {
                // filter out the "." and ".."
            } else {
                Path tmp = new PathImplementation(dir.getFileSystem(), dir.getRelativePath().resolve(filename));

                if (filter.accept(tmp)) {
                    SshFileAttributes attributes = new SshFileAttributes(e.getAttrs(), tmp);
                    stream.add(new PathAttributesPairImplementation(tmp, attributes));
                }
            }
        }
    }

    @Override
    public Iterator<PathAttributesPair> iterator() {
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
    public synchronized PathAttributesPair next() {

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