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
import java.util.Iterator;

import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.CobaltRuntimeException;
import nl.esciencecenter.xenon.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;

/**
 * LocalDirectoryAttributeStream implements a {@link DirectoryStream} for local directories.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {

    /** LocalFiles to retrieve the attributes of a file */
    private final LocalFiles localFiles;

    /** The LocalDirectoryStream to retrieve the files */
    private final LocalDirectoryStream stream;

    LocalDirectoryAttributeStream(LocalFiles localFiles, LocalDirectoryStream stream) throws CobaltException {
        this.localFiles = localFiles;
        this.stream = stream;
    }

    @Override
    public Iterator<PathAttributesPair> iterator() {
        return this;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public boolean hasNext() {
        return stream.hasNext();
    }

    @Override
    public PathAttributesPair next() {

        Path path = stream.next();

        try {
            FileAttributes attributes = localFiles.getAttributes(path);
            return new PathAttributesPairImplementation(path, attributes);
        } catch (CobaltException e) {
            throw new CobaltRuntimeException(LocalAdaptor.ADAPTOR_NAME, "Failed to get next element.", e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("DirectoryAttributeStream iterator does not support remove");
    }
}