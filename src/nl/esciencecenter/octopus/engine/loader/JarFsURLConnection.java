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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

class JarFsURLConnection extends URLConnection {

    private final JarFileSystem fileSystem;

    private final JarFsFile file;

    private InputStream in = null;

    public JarFsURLConnection(JarFileSystem fileSystem, JarFsFile file, URL url) {
        super(url);

        this.fileSystem = fileSystem;
        this.file = file;
    }

    @Override
    public void connect() throws IOException {
        fileSystem.loadFileData(file);

        in = new ByteArrayInputStream(file.getBytes().array(), file.getBytes().position(), file.getBytes().remaining());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (in == null) {
            throw new IOException("not connected yet");
        }
        return in;
    }
}