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

package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;

import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.Util;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalAdaptorTest {

    @org.junit.Test
    public void test_checkURI_null() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(null);
    }

    @org.junit.Test
    public void test_checkURI_empty() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI(""));
    }

    @org.junit.Test(expected = OctopusException.class)
    public void test_checkURI_wrongScheme() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI("ssh:///"));
    }

    @org.junit.Test
    public void test_checkURI_withPath() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI("local:///aap/noot/mies"));
    }

    @org.junit.Test
    public void test_checkURI_withPath2() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI("/aap/noot/mies"));
    }

    @org.junit.Test(expected = OctopusException.class)
    public void test_checkURI_wrongLocation() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI("file://google.com"));
    }

    @org.junit.Test
    public void test_checkURI_correct1() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI("file:///"));
    }

    @org.junit.Test
    public void test_checkURI_correct2() throws Exception {
        new LocalAdaptor(Util.createOctopusEngine(null), null).checkURI(new URI("file://localhost/"));
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
        OctopusPropertyDescription [] p = new LocalAdaptor(Util.createOctopusEngine(null), null).getSupportedProperties();
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
