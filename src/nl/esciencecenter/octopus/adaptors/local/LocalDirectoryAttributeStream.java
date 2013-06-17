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
import java.util.Iterator;

import nl.esciencecenter.octopus.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.PathAttributesPair;

/**
 * LocalDirectoryAttributeStream implements a {@link DirectoryStream} for local directories. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */

// FIXME: use localdistream ? 
class LocalDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {

    /** LocalFiles to retrieve the attributes of a file */
    private final LocalFiles localFiles;

//    /** The DirectoryStream from the underlying java.nio implementation */
//    private final java.nio.file.DirectoryStream<java.nio.file.Path> stream;
//
//    /** The Iterator from the underlying java.nio implementation. */
//    private final Iterator<java.nio.file.Path> iterator;
//
//    /** The filter to use. */
//    private final DirectoryStream.Filter filter;
//
//    /** A buffer to read ahead. */
//    private final LinkedList<AbsolutePath> readAhead;
//
//    /** The directory to produce a stream for. */
//    private final AbsolutePath dir;
    
    private final LocalDirectoryStream stream;    

    LocalDirectoryAttributeStream(LocalFiles localFiles, LocalDirectoryStream stream)
            throws OctopusIOException {
        this.localFiles = localFiles;
        this.stream = stream;
    }
//
//    private AbsolutePath gatPath(java.nio.file.Path path) throws OctopusIOException {
//        return dir.resolve(new RelativePath(path.getFileName().toString()));
//    }

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
    public boolean hasNext() {
        return stream.hasNext();
//        
//        
//        try {
//            if (!readAhead.isEmpty()) {
//                return true;
//            }
//            while (iterator.hasNext()) {
//                AbsolutePath next = gatPath(iterator.next());
//                if (filter.accept(next)) {
//                    readAhead.addLast(next);
//                    return true;
//                }
//            }
//            return false;
//        } catch (OctopusIOException e) {
//            throw new DirectoryIteratorException(LocalAdaptor.ADAPTOR_NAME, "Failed to get next element.", e);
//        }
    }

    @Override
    public PathAttributesPair next() {
       
        AbsolutePath path = stream.next();
        
        try { 
            FileAttributes attributes = localFiles.getAttributes(path);
            return new PathAttributesPairImplementation(path, attributes);
        } catch (OctopusIOException e) {
            throw new DirectoryIteratorException(LocalAdaptor.ADAPTOR_NAME, "Failed to get next element.", e);
        }
//        
//        
//        
//        try {
//            if (!readAhead.isEmpty()) {
//                AbsolutePath path = readAhead.removeFirst();
//                FileAttributes attributes = localFiles.getAttributes(path);
//                return new PathAttributesPairImplementation(path, attributes);
//            }
//
//            while (iterator.hasNext()) {
//                AbsolutePath next = gatPath(iterator.next());
//                if (filter.accept(next)) {
//                    FileAttributes attributes = localFiles.getAttributes(next);
//                    return new PathAttributesPairImplementation(next, attributes);
//                }
//            }
//            throw new NoSuchElementException("No more files in directory.");
//        } catch (OctopusIOException e) {
//            throw new DirectoryIteratorException(LocalAdaptor.ADAPTOR_NAME, "Failed to get next element.", e);
//        }
    }

    @Override
    public void remove() {
        stream.remove();
//        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove.");
    }
}