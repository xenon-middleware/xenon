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
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * Implementation of Path. Will create new Paths directly, using either an adaptor identical to the original in case of an
 * absolute path, or the local adaptor in case of a relative path.
 */
public final class AbsolutePathImplementation implements AbsolutePath {

    class AbsolutePathIterator implements Iterator<AbsolutePath> {

        private final Iterator<RelativePath> iterator;

        AbsolutePathIterator(Iterator<RelativePath> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public AbsolutePath next() {
            return new AbsolutePathImplementation(filesystem, iterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("PathIterator does not support remove!");
        }
    }

    private final FileSystem filesystem;
    private final RelativePath relativePath;

    public AbsolutePathImplementation(FileSystem filesystem, RelativePath relativePath) {

        this.filesystem = filesystem;
        this.relativePath = relativePath;

        //        String pathString = location.getPath();
        //
        //        if (pathString == null) {
        //            root = null;
        //            elements = new String[0];
        //        } else if (isLocal() && OSUtils.isWindows()) {
        //            if (location.getPath().matches("^/[a-zA-Z]:/")) {
        //                root = pathString.substring(0, 4);
        //                pathString = pathString.substring(4);
        //            } else {
        //                root = null;
        //            }
        //            this.elements = pathString.split("/+");
        //        } else {
        //            if (pathString.startsWith("/")) {
        //                root = "/";
        //                pathString = pathString.substring(1);
        //            } else {
        //                root = null;
        //            }
        //            this.elements = pathString.split("/+");
        //        }

    }

    public AbsolutePathImplementation(FileSystem filesystem, RelativePath... relativePaths) {

        this.filesystem = filesystem;

        if (relativePaths.length == 0) {
            throw new IllegalArgumentException("AbsolutePathImplementation requires at least one RelativePath");
        }

        this.relativePath = new RelativePath(relativePaths);
    }

    @Override
    public FileSystem getFileSystem() {
        return filesystem;
    }

    @Override
    public RelativePath getRelativePath() {
        return relativePath;
    }

    @Override
    public boolean isLocal() {
        return filesystem.getAdaptorName().equals("local");
    }

    @Override
    public String getFileName() {
        return relativePath.getFileName();
    }

    @Override
    public AbsolutePath getParent() {
        RelativePath path = relativePath.getParent();
        if (path == null) {
            return null;
        }
        return new AbsolutePathImplementation(filesystem, path);
    }

    @Override
    public int getNameCount() {
        return relativePath.getNameCount();
    }

    @Override
    public String[] getNames() {
        return relativePath.getNames();
    }

    @Override
    public String getName(int index) {
        return relativePath.getName(index);
    }

    @Override
    public AbsolutePath subpath(int beginIndex, int endIndex) {
        return new AbsolutePathImplementation(filesystem, relativePath.subpath(beginIndex, endIndex));
    }

    @Override
    public AbsolutePath normalize() {
        return new AbsolutePathImplementation(filesystem, relativePath.normalize());
    }

    @Override
    public boolean startsWith(RelativePath other) {
        return relativePath.startsWith(other);
    }

    @Override
    public boolean endsWith(RelativePath other) {
        return relativePath.endsWith(other);
    }

    @Override
    public AbsolutePath resolve(RelativePath other) {
        return new AbsolutePathImplementation(filesystem, relativePath.resolve(other));
    }

    @Override
    public AbsolutePath resolveSibling(RelativePath other) {
        return new AbsolutePathImplementation(filesystem, relativePath.resolveSibling(other));
    }

    @Override
    public AbsolutePath relativize(RelativePath other) {
        return new AbsolutePathImplementation(filesystem, relativePath.relativize(other));
    }

    @Override
    public Iterator<AbsolutePath> iterator() {
        return new AbsolutePathIterator(relativePath.iterator());
    }

    @Override
    public String getPath() {

        if (relativePath.isEmpty()) {
            return relativePath.getSeparator();
        }

        return relativePath.getPath();
    }

    public String toString() {
        return filesystem.toString() + relativePath.toString();
    }
}
