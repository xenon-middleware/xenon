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

package nl.esciencecenter.octopus.engine.files;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Component;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Type;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusPropertyDescriptionImplementation;
import nl.esciencecenter.octopus.engine.util.ImmutableArray;
import nl.esciencecenter.octopus.files.Pathname;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class FileSystemImplementationTest {

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_fail_adaptor_null() throws Exception {
        new FileSystemImplementation(null, null, null, null, null, null, null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_fail_identifier_null() throws Exception {
        new FileSystemImplementation("AAP", null,null, null, null, null, null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_fail_URI_null() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", null, null,null, null, null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_fail_path_null() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", "file", null, null, null, null);
    }

    @org.junit.Test
    public void test_ok() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"), null, null);
    }

    @org.junit.Test
    public void test_ok2() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", "file", "C:", new Pathname("aap"), null, null);
    }

    
    @org.junit.Test
    public void test_ID_ok() throws Exception {
        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);

        String tmp = fi.getUniqueID();
        assert (tmp.equals("NOOT"));
    }

    @org.junit.Test
    public void test_getCredential() throws Exception {
        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file","/", new Pathname("aap"),
                null, null);

        Credential c = fi.getCredential();

        assertNull(c);
    }

    @org.junit.Test
    public void test_getCredential2() throws Exception {
        Credential c = new Credential() {

            @Override
            public Map<String, String> getProperties() {
                return null;
            }

            @Override
            public String getAdaptorName() {
                return "test";
            }
        };

        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                c, null);

        Credential c2 = fi.getCredential();

        assertTrue(c == c2);
    }

    @org.junit.Test
    public void test_name_ok() throws Exception {
        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);

        String tmp = fi.getAdaptorName();
        assert (tmp.equals("AAP"));
    }

    @org.junit.Test
    public void test_properties_ok() throws Exception {

        Map<String, String> tmp = new HashMap<>();
        tmp.put("test", "test");

        ImmutableArray<OctopusPropertyDescription> valid = new ImmutableArray<OctopusPropertyDescription>(
                new OctopusPropertyDescriptionImplementation(
                "test", Type.STRING, EnumSet.of(Component.OCTOPUS), "test", "test property"));

        OctopusProperties p = new OctopusProperties(valid, tmp);

        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, p);

        Map<String, String> p2 = fi.getProperties();
        assert ("test".equals((String) p2.get("test")));
    }

    @org.junit.Test
    public void test_hashCode() throws Exception {
        int tmp = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"), null, null)
                .hashCode();

        int result = 31 + "AAP".hashCode();
        result = 31 * result + "NOOT".hashCode();

        assert (tmp == result);
    }

    @org.junit.Test
    public void test_equals1() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        boolean v = f1.equals(f1);

        assert (v);
    }

    @org.junit.Test
    public void test_equals2() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        boolean v = f1.equals(null);
        assert (!v);
    }

    @org.junit.Test
    public void test_equals3() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        boolean v = f1.equals("Hello World");
        assert (!v);
    }

    @org.junit.Test
    public void test_equals4() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        FileSystemImplementation f2 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        boolean v = f1.equals(f2);

        assert (v);
    }

    @org.junit.Test
    public void test_equals5() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        FileSystemImplementation f2 = new FileSystemImplementation("MIES", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        boolean v = f1.equals(f2);

        assert (!v);
    }

    @org.junit.Test
    public void test_equals6() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new Pathname("aap"),
                null, null);
        FileSystemImplementation f2 = new FileSystemImplementation("AAP", "MIES", "file", "/", new Pathname("aap"),
                null, null);
        boolean v = f1.equals(f2);

        assert (!v);
    }
}
