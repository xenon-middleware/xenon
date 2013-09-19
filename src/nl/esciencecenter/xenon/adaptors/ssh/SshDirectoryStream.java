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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.Path;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SshDirectoryStream implements DirectoryStream<Path>, Iterator<Path> {

    private final Deque<Path> stream;

    SshDirectoryStream(Path dir, DirectoryStream.Filter filter, List<LsEntry> listing) throws CobaltException {

        stream = new LinkedList<Path>();

        for (LsEntry e : listing) {

            String filename = e.getFilename();

            if (filename.equals(".") || filename.equals("..")) {
                // filter out the "." and ".."
            } else {
                Path tmp = new PathImplementation(dir.getFileSystem(), dir.getRelativePath().resolve(filename));
                
                if (filter.accept(tmp)) {
                    stream.add(tmp);
                }
            }
        }
    }

    @Override
    public Iterator<Path> iterator() {
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
    public synchronized Path next() {

        if (stream.size() > 0) {
            return stream.removeFirst();
        }

        throw new NoSuchElementException("No more files in directory");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove");
    }
}