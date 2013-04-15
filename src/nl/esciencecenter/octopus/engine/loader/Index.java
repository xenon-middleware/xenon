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
package nl.esciencecenter.octopus.engine.loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Index {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(Index.class);

    private final Map<JarEntry, HashSet<String>> entries;

    Index() {
        entries = new HashMap<JarEntry, HashSet<String>>();
    }

    //add a jar file to the index
    public void add(JarEntry entry, JarFile mainJarFile) throws OctopusException {
        HashSet<String> names = new HashSet<String>();
        entries.put(entry, names);

        try (JarInputStream subJarStream = new JarInputStream(mainJarFile.getInputStream(entry))) {
            while (true) {
                JarEntry subEntry = subJarStream.getNextJarEntry();
                if (subEntry == null) {
                    return;
                }

                names.add(subEntry.getName());
            }
        } catch (IOException e) {
            throw new OctopusException("Index", "failed to get adaptor files from jar");
        }
    }

    public JarEntry findEntryFor(String filename) {
        for (Map.Entry<JarEntry, HashSet<String>> entry : entries.entrySet()) {
            if (entry.getValue().contains(filename)) {
                return entry.getKey();
            }
        }
        return null;
    }
}