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
 * Implementation of Path. Will create new Paths directly, using either an adaptor identical to the original in case of an
 * absolute path, or the local adaptor in case of a relative path.
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

//    @Override
//    public boolean isLocal() {
//        return filesystem.getAdaptorName().equals("local");
//    }

//    @Override
//    public String getFileName() {
//        return pathname.getFileName();
//    }

//    @Override
//    public Path getParent() {
//        Pathname path = pathname.getParent();
//        if (path == null) {
//            return null;
//        }
//        return new PathImplementation(filesystem, path);
//    }

//    @Override
//    public int getNameCount() {
//        return pathname.getNameCount();
//    }

//    @Override
//    public String[] getNames() {
//        return pathname.getNames();
//    }

//    @Override
//    public String getName(int index) {
//        return pathname.getName(index);
//    }

//    @Override
//    public Path subpath(int beginIndex, int endIndex) {
//        return new PathImplementation(filesystem, pathname.subpath(beginIndex, endIndex));
//    }

//    @Override
//    public Path normalize() {
//        return new PathImplementation(filesystem, pathname.normalize());
//    }
//
//    @Override
//    public boolean startsWith(Pathname other) {
//        return pathname.startsWith(other);
//    }
//
//    @Override
//    public boolean endsWith(Pathname other) {
//        return pathname.endsWith(other);
//    }

//    @Override
//    public Path resolve(Pathname other) {
//        return new PathImplementation(filesystem, pathname.resolve(other));
//    }

//    @Override
//    public Path resolveSibling(Pathname other) {
//        return new PathImplementation(filesystem, pathname.resolveSibling(other));
//    }
//
//    @Override
//    public Pathname relativize(Pathname other) {
//        return pathname.relativize(other);
//    }
//
//    @Override
//    public Iterator<Path> iterator() {
//        return new PathIterator(pathname.iterator());
//    }

    @Override
    public String getPath() {

        if (pathname.isEmpty()) {
            return "" + pathname.getSeparator();
        }

        return pathname.getPath();
    }

    public String toString() {
        return filesystem.toString() + pathname.toString();
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
