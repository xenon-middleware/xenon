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
package nl.esciencecenter.xenon.adaptors.filesystems.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.Path;

public class LocalFileSystemSimpleTest {

    // @Test(expected = XenonException.class)
    // public void test_getLocalRoot_fails() throws XenonException {
    // LocalFileSystem f = new LocalFileSystem("test", "/", "/", new Path("/"), null);
    // f.getRelativePath("/usr/local", "C:/Users");
    // }
    //
    // @Test
    // public void test_getLocalRoot() throws XenonException {
    // LocalFileSystem f = new LocalFileSystem("test", "/", "/", new Path("/"), null);
    // Path tmp = f.getRelativePath("/usr/local", "/usr");
    // assertEquals("/local", tmp.toString());
    // }
    //
    // @Test
    // public void test_getLocalRoot_nothingLeft() throws XenonException {
    // LocalFileSystem f = new LocalFileSystem("test", "/", "/", new Path("/"), null);
    // Path tmp = f.getRelativePath("/usr/local", "/usr/local");
    // assertTrue(tmp.toString().isEmpty());
    // }

    @Test
    public void test_xenonPermissions_null() throws XenonException {
        try (LocalFileSystem f = new LocalFileSystem("test", "/", new DefaultCredential(), "/", new Path("/"), 4096, null)) {
            assertNull(f.xenonPermissions(null));
        }
    }

    @Test
    public void test_javaPermissions_null() throws XenonException {
        try (LocalFileSystem f = new LocalFileSystem("test", "/", new DefaultCredential(), "/", new Path("/"), 4096, null)) {
            assertEquals(new HashSet<java.nio.file.attribute.PosixFilePermission>(0), f.javaPermissions(null));
        }
    }
}
