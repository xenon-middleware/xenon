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
package nl.esciencecenter.xenon.adaptors.webdav;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

public class WebdavOutputStreamTest {
    private Path path;
    private WebdavFiles files;
    private WebdavOutputStream webdavOutputStream;

    @Before
    public void setUp() {
        path = getPathMock();
        files = mock(WebdavFiles.class);
        webdavOutputStream = new WebdavOutputStream(path, files);
    }

    @Test
    public void close_deleteExistingFile() throws IOException, XenonException {
        when(files.exists(Mockito.isA(Path.class))).thenReturn(true);
        webdavOutputStream.close();
        verify(files, atLeastOnce()).delete(Mockito.isA(Path.class));
    }

    @Test
    public void close_dontDeleteNonexistingFile() throws IOException, XenonException {
        when(files.exists(Mockito.isA(Path.class))).thenReturn(false);
        webdavOutputStream.close();
        verify(files, never()).delete(Mockito.isA(Path.class));
    }

    private Path getPathMock() {
        return new Path() {

            @Override
            public RelativePath getRelativePath() {
                return null;
            }

            @Override
            public FileSystem getFileSystem() {
                return null;
            }
        };
    }
}
