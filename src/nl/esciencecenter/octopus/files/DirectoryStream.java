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
package nl.esciencecenter.octopus.files;

import java.io.Closeable;
import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * DirectoryStream represents an Iterable set of elements.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * @param <T>
 */
public interface DirectoryStream<T> extends Closeable, Iterable<T> {

    /**
     * A filter use to decides if the given directory entry should be accepted.
     */
    public interface Filter {

        /**
         * Decide if the AbsolutePath entry should be accepted.
         * 
         * @param entry
         *            the AbsolutePath to test.
         * 
         * @return if the AbsolutePath entry should be accepted.
         */
        boolean accept(AbsolutePath entry);
    }

    public Iterator<T> iterator();

    public void close() throws OctopusIOException;
}
