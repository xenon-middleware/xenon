/**
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
package nl.esciencecenter.xenon.files;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * DirectoryStream is a {@link Iterable} set of elements that represent the entries found in a directory. 
 * 
 * The elements typically consist of {@link Path} objects when returned by one of the <code>Files.newDirectoryStream</code> calls
 * or {@link PathAttributesPair} objects when returned by one of the <code>Files.newAttributesDirectoryStream</code> calls. 
 */
public interface DirectoryStream<T> extends Closeable, Iterable<T> {

    static final Filter filterNothing = new Filter() {
        @Override
        public boolean accept(Path entry) {
            return true;
        }
    };

    /**
     * A filter use to decides if the given directory entry should be accepted.
     */
    interface Filter {

        /**
         * Decide if the entry should be accepted.
         * 
         * @param entry
         *            the path to test.
         * 
         * @return if the entry should be accepted.
         */
        boolean accept(Path entry);
    }

    @Override
    Iterator<T> iterator();

    @Override
    void close() throws IOException;
}
