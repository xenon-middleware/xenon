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

package nl.esciencecenter.xenon.adaptors.local;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.adaptors.GenericFileAdaptorTestParent;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

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
    
    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_null() throws Exception {
        LocalFiles.checkFileLocation(null);
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_empty() throws Exception {
        LocalFiles.checkFileLocation("");
    }

    @org.junit.Test
    public void test_checkLocation_linuxRoot() throws Exception {
        if (Utils.isLinux() || Utils.isOSX()) { 
            LocalFiles.checkFileLocation("/");
        }
    }

    @org.junit.Test
    public void test_checkLocation_windowsRoot() throws Exception {
        if (Utils.isWindows()) { 
            LocalFiles.checkFileLocation("C:");
        }
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_wrong() throws Exception {
        LocalFiles.checkFileLocation("ABC");
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withPath() throws Exception {
        LocalFiles.checkFileLocation("/aap");
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withWindowsPath() throws Exception {
        LocalFiles.checkFileLocation("C:/aap");
    }
    
    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withWindowsPath2() throws Exception {
        LocalFiles.checkFileLocation("C:\\aap");
    }

}
