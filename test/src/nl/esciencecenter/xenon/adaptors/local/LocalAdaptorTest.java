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

import nl.esciencecenter.xenon.CobaltPropertyDescription;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.adaptors.local.LocalAdaptor;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.util.Utils;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalAdaptorTest {

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_null() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation(null);
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_empty() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("");
    }

    @org.junit.Test
    public void test_checkLocation_linuxRoot() throws Exception {
        if (Utils.isLinux() || Utils.isOSX()) { 
            new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("/");
        }
    }

    @org.junit.Test
    public void test_checkLocation_windowsRoot() throws Exception {
        if (Utils.isWindows()) { 
            new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("C:");
        }
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_wrong() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("ABC");
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withPath() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("/aap");
    }

    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withWindowsPath() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("C:/aap");
    }
    
    @org.junit.Test(expected = InvalidLocationException.class)
    public void test_checkLocation_withWindowsPath2() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkLocation("C:\\aap");
    }

    @org.junit.Test
    public void test_supports_null() throws Exception {
        boolean value = new LocalAdaptor(Util.createOctopusEngine(null), null).supports(null);
        assert (value);
    }

    @org.junit.Test
    public void test_supports_wrong() throws Exception {
        boolean value = new LocalAdaptor(Util.createOctopusEngine(null), null).supports("ssh");
        assert (!value);
    }

    @org.junit.Test
    public void test_supports_correct_file() throws Exception {
        boolean value = new LocalAdaptor(Util.createOctopusEngine(null), null).supports("file");
        assert (value);
    }

    @org.junit.Test
    public void test_supports_correct_local() throws Exception {
        boolean value = new LocalAdaptor(Util.createOctopusEngine(null), null).supports("local");
        assert (value);
    }

    @org.junit.Test
    public void test_getSupportedProperties() throws Exception {
        CobaltPropertyDescription[] p = new LocalAdaptor(Util.createOctopusEngine(null), null).getSupportedProperties();
        assert (p != null);
    }

    @org.junit.Test
    public void test_credentialsAdaptor() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).credentialsAdaptor();
    }

    @org.junit.Test
    public void test_filesAdaptor() throws Exception {
        Files files = new LocalAdaptor(Util.createOctopusEngine(null), null).filesAdaptor();
        assert (files != null);
    }

    @org.junit.Test
    public void test_jobsAdaptor() throws Exception {
        Jobs jobs = new LocalAdaptor(Util.createOctopusEngine(null), null).jobsAdaptor();
        assert (jobs != null);
    }
}
