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
package nl.esciencecenter.xenon.adaptors.local;

import static org.junit.Assert.assertEquals;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.GenericFileAdaptorTestParent;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalFileAdaptorTest extends GenericFileAdaptorTestParent {

    @BeforeClass
    public static void prepareLocalFileAdaptorTest() throws Exception {
        GenericFileAdaptorTestParent.prepareClass(new LocalFileTestConfig());
    }

    @AfterClass
    public static void cleanupLocalFileAdaptorTest() throws Exception {
        GenericFileAdaptorTestParent.cleanupClass();
    }
    
    @Test
    public void test_checkLocation_null() throws Exception {
        LocalFiles.checkFileLocation(null);
    }

    @Test
    public void test_checkLocation_empty() throws Exception {
        LocalFiles.checkFileLocation("");
    }

    @Test
    public void test_checkLocation_linuxRoot() throws Exception {
        if (Utils.isLinux() || Utils.isOSX()) { 
            LocalFiles.checkFileLocation("/");
        }
    }

    @Test
    public void test_checkLocation_windowsRoot() throws Exception {
        if (Utils.isWindows()) { 
            LocalFiles.checkFileLocation("C:");
        }
    }

    @Test(expected = InvalidLocationException.class)
    public void test_checkLocation_wrong() throws Exception {
        LocalFiles.checkFileLocation("ABC");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withPath() throws Exception {
        LocalFiles.checkFileLocation("/aap");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withWindowsPath() throws Exception {
        LocalFiles.checkFileLocation("C:/aap");
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withWindowsPath2() throws Exception {
        LocalFiles.checkFileLocation("C:\\aap");
    }

    @Test
    public void test_newFileSystem_nullLocation() throws Exception {
        FileSystem fs = files.newFileSystem("local", null, credentials.getDefaultCredential("local"), null);
        if (Utils.isWindows()) {
            assertEquals("", fs.getLocation());
        } else {
            assertEquals("/", fs.getLocation());
        }
    }

    @Test
    public void test_newFileSystem_emptyLocation() throws Exception {
        FileSystem fs = files.newFileSystem("local", "", credentials.getDefaultCredential("local"), null);
        if (Utils.isWindows()) {
            assertEquals("", fs.getLocation());
        } else {
            assertEquals("/", fs.getLocation());
        }
    }

    @Test
    public void test_newFileSystem_windowsNetworkLocation() throws Exception {
        if (!Utils.isWindows()) {
            return;
        }

        FileSystem fs = files.newFileSystem("local", "mynetwork", credentials.getDefaultCredential("local"), null);
        assertEquals("mynetwork", fs.getLocation());
    }

    @Test(expected = InvalidLocationException.class)
    public void test_newFileSystem_linuxNetworkLocation_throws() throws Exception {
        if (Utils.isWindows()) {
            throw new InvalidLocationException(null, null);
        }

        files.newFileSystem("local", "mynetwork", credentials.getDefaultCredential("local"), null);
    }
}
