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
package nl.esciencecenter.xenon.engine.files;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonRuntimeException;
import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.RelativePath;

import org.junit.Test;

public class FilesEngineTest {

    @Test
    public void testFilesEngine() throws Exception {

        XenonEngine oe = Util.createXenonEngine(new HashMap<String, String>());
        FilesEngine engine = new FilesEngine(oe);
        String tmp = engine.toString();
        assertNotNull(tmp);
    }

    @Test(expected = XenonRuntimeException.class)
    public void testUnknownFileSystem() throws Exception {

        XenonEngine oe = Util.createXenonEngine(new HashMap<String, String>());
        FilesEngine engine = new FilesEngine(oe);

        FileSystemImplementation fsi = new FileSystemImplementation("test", "test1", "test", "/", new RelativePath(),
                null, null);

        // Should throw exception
        engine.newPath(fsi, new RelativePath("tmp/bla.txt"));
    }

    @Test(expected = XenonException.class)
    public void testInterSchemeCopy() throws Exception {

        XenonEngine oe = Util.createXenonEngine(new HashMap<String, String>());
        FilesEngine engine = new FilesEngine(oe);

        FileSystemImplementation fs1 = new FileSystemImplementation("aap", "test1", "test", "/", new RelativePath(),
                null, null);

        PathImplementation p1 = new PathImplementation(fs1, new RelativePath("test"));

        FileSystemImplementation fs2 = new FileSystemImplementation("noot", "test1", "test", "/", new RelativePath(),
                null, null);

        PathImplementation p2 = new PathImplementation(fs2, new RelativePath("test"));

        // Should throw exception
        engine.copy(p1, p2, CopyOption.CREATE);
    }
}
