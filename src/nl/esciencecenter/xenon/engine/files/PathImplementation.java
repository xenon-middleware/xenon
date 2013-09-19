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
package nl.esciencecenter.xenon.engine.files;

import java.util.Iterator;

import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * Implementation of Path. 
 */
public final class PathImplementation implements Path {

    class PathIterator implements Iterator<Path> {

        private final Iterator<RelativePath> iterator;

        PathIterator(Iterator<RelativePath> iterator) {
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
    private final RelativePath relativePath;

    public PathImplementation(FileSystem filesystem, RelativePath path) {

        if (filesystem == null) {
            throw new IllegalArgumentException("FileSystem may not be null!");
        }

        if (path == null) {
            throw new IllegalArgumentException("RelatvePath may not be null!");
        }

        this.filesystem = filesystem;
        this.relativePath = path;
    }

    public PathImplementation(FileSystem filesystem, RelativePath... paths) {

        if (filesystem == null) {
            throw new IllegalArgumentException("FileSystem may not be null!");
        }

        this.filesystem = filesystem;

        if (paths.length == 0) {
            throw new IllegalArgumentException("PathImplementation requires at least one RelativePath");
        }

        this.relativePath = new RelativePath(paths);
    }

    @Override
    public FileSystem getFileSystem() {
        return filesystem;
    }

    @Override
    public RelativePath getRelativePath() {
        return relativePath;
    }

    public String toString() {
        return filesystem.getScheme() + "://" + filesystem.getLocation() + relativePath.getAbsolutePath();        
    }

    @Override
    public int hashCode() {
        int result = 31 + filesystem.getAdaptorName().hashCode();
        result = 31 * result + filesystem.getScheme().hashCode();
        result = 31 * result + filesystem.getLocation().hashCode();
        return 31 * result + relativePath.hashCode();
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

        if (!filesystem.getScheme().equals(other.filesystem.getScheme())) {
            return false;
        }

        if (!filesystem.getLocation().equals(other.filesystem.getLocation())) {
            return false;
        }
        
        return relativePath.equals(other.relativePath);
    }
}
