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
package nl.esciencecenter.octopus.engine.files;

import java.util.Iterator;

import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.Pathname;

/**
 * Implementation of Path. 
 */
public final class PathImplementation implements Path {

    class PathIterator implements Iterator<Path> {

        private final Iterator<Pathname> iterator;

        PathIterator(Iterator<Pathname> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Path next() {
            return new PathImplementation(filesystem, iterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("PathIterator does not support remove!");
        }
    }

    private final FileSystem filesystem;
    private final Pathname pathname;

    public PathImplementation(FileSystem filesystem, Pathname pathname) {

        if (filesystem == null) {
            throw new IllegalArgumentException("FileSystem may not be null!");
        }

        if (pathname == null) {
            throw new IllegalArgumentException("Pathname may not be null!");
        }

        this.filesystem = filesystem;
        this.pathname = pathname;
    }

    public PathImplementation(FileSystem filesystem, Pathname... pathnames) {

        if (filesystem == null) {
            throw new IllegalArgumentException("FileSystem may not be null!");
        }

        this.filesystem = filesystem;

        if (pathnames.length == 0) {
            throw new IllegalArgumentException("PathImplementation requires at least one pathname");
        }

        this.pathname = new Pathname(pathnames);
    }

    @Override
    public FileSystem getFileSystem() {
        return filesystem;
    }

    @Override
    public Pathname getPathname() {
        return pathname;
    }

    public String toString() {
        return filesystem.getUri() + pathname.getAbsolutePath();        
    }

    @Override
    public int hashCode() {
        int result = 31 + filesystem.getAdaptorName().hashCode();
        result = 31 * result + filesystem.getUri().hashCode();
        return 31 * result + pathname.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof PathImplementation)) {
            return false;
        }

        PathImplementation other = (PathImplementation) obj;

        if (!filesystem.getAdaptorName().equals(other.filesystem.getAdaptorName())) {
            return false;
        }

        if (!filesystem.getUri().equals(other.filesystem.getUri())) {
            return false;
        }

        return pathname.equals(other.pathname);
    }
}
