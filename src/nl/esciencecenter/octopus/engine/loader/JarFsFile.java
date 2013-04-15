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

import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

public class JarFsFile {

    private final String adaptor;
    private final String filename;
    private final JarEntry jarEntry;
    private final boolean inMainJar;
    private Manifest manifest;
    private ByteBuffer bytes;

    public JarFsFile(String adaptor, String filename, JarEntry jarEntry, boolean inMainJar) {
        this.adaptor = adaptor;
        this.filename = filename;
        this.jarEntry = jarEntry;
        this.inMainJar = inMainJar;
        this.manifest = null;
        this.bytes = null;
    }

    public String getAdaptor() {
        return adaptor;
    }

    public String getFilename() {
        return filename;
    }

    public JarEntry getJarEntry() {
        return jarEntry;
    }

    public boolean inMainJar() {
        return inMainJar;
    }

    public ByteBuffer getBytes() {
        return bytes;
    }

    public Manifest getManifest() {
        return manifest;
    }

    void setBytes(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    @Override
    public String toString() {
        return "JarFsFile [adaptor=" + adaptor + ", filename=" + filename + ", jarEntry=" + jarEntry + ", inMainJar=" + inMainJar
                + ", manifest=" + manifest + ", bytes=" + bytes + "]";
    }

}
