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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Iterator;
import java.util.Vector;

import nl.esciencecenter.octopus.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

import com.jcraft.jsch.ChannelSftp.LsEntry;

class SshDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {
    private final DirectoryStream.Filter filter;
    private final AbsolutePath dir;
    private Vector<LsEntry> listing;

    private int current = 0;

    SshDirectoryAttributeStream(AbsolutePath dir, DirectoryStream.Filter filter, Vector<LsEntry> listing)
            throws OctopusIOException {
        this.dir = dir;
        this.filter = filter;
        this.listing = listing;

        SshDirectoryStream.filterSpecials(listing);
    }

    @Override
    public Iterator<PathAttributesPair> iterator() {
        return this;
    }

    @Override
    public void close() throws OctopusIOException {
        listing = null;
    }

    @Override
    public synchronized boolean hasNext() {
        return current < listing.size();
    }

    @Override
    public synchronized PathAttributesPair next() {
        while (hasNext()) {
            LsEntry nextEntry = listing.get(current);
            current++;
            AbsolutePath nextPath = dir.resolve(new RelativePath(nextEntry.getFilename()));
            if (filter.accept(nextPath)) {
                SshFileAttributes attributes = new SshFileAttributes(nextEntry.getAttrs(), nextPath);
                PathAttributesPair next = new PathAttributesPairImplementation(nextPath, attributes);
                return next;
            }
        }
        return null;
    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException(SshAdaptor.ADAPTOR_NAME, "DirectoryStream iterator does not support remove");
    }
}