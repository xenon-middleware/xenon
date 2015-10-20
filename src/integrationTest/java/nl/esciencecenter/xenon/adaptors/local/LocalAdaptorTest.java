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

import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalAdaptorTest {

    @org.junit.Test
    public void test_supports_null() throws Exception {
        boolean value = new LocalAdaptor(Util.createXenonEngine(null), null).supports(null);
        assert (value);
    }

    @org.junit.Test
    public void test_supports_wrong() throws Exception {
        boolean value = new LocalAdaptor(Util.createXenonEngine(null), null).supports("ssh");
        assert (!value);
    }

    @org.junit.Test
    public void test_supports_correct_file() throws Exception {
        boolean value = new LocalAdaptor(Util.createXenonEngine(null), null).supports("file");
        assert (value);
    }

    @org.junit.Test
    public void test_supports_correct_local() throws Exception {
        boolean value = new LocalAdaptor(Util.createXenonEngine(null), null).supports("local");
        assert (value);
    }

    @org.junit.Test
    public void test_getSupportedProperties() throws Exception {
        XenonPropertyDescription[] p = new LocalAdaptor(Util.createXenonEngine(null), null).getSupportedProperties();
        assert (p != null);
    }

    @org.junit.Test
    public void test_credentialsAdaptor() throws Exception {
        new LocalAdaptor(Util.createXenonEngine(null), null).credentialsAdaptor();
    }

    @org.junit.Test
    public void test_filesAdaptor() throws Exception {
        Files files = new LocalAdaptor(Util.createXenonEngine(null), null).filesAdaptor();
        assert (files != null);
    }

    @org.junit.Test
    public void test_jobsAdaptor() throws Exception {
        Jobs jobs = new LocalAdaptor(Util.createXenonEngine(null), null).jobsAdaptor();
        assert (jobs != null);
    }
}
