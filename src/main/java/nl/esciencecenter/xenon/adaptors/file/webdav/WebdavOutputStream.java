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
package nl.esciencecenter.xenon.adaptors.file.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

/**
 * Webdav does not support resuming or appending. This class acts as a buffer. On closing, the complete content is written at once
 * using the WebdavFiles adaptor.
 *
 * @author Christiaan Meijer
 *
 */
public class WebdavOutputStream extends OutputStream {
    private final WebdavFileSystem filesystem;
    private final Path path;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public WebdavOutputStream(Path path, WebdavFileSystem filesystem) {
        this.path = path;
        this.filesystem = filesystem;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        try {
            if (filesystem.exists(path)) {
                // first delete the file otherwise we get a 405 when executing the put
                filesystem.delete(path);
            }
            filesystem.createFile(path, outputStream.toByteArray());
        } catch (XenonException e) {
            throw new IOException(e);
        }

    }

}
