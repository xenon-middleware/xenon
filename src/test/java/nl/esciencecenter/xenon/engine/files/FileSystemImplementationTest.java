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

import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.RelativePath;

import org.junit.Test;

/**
 * 
 */
public class FileSystemImplementationTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_fail_adaptor_null() throws Exception {
        new FileSystemImplementation(null, null, null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_fail_identifier_null() throws Exception {
        new FileSystemImplementation("AAP", null,null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_fail_URI_null() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", null, null,null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_fail_path_null() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", "file", null, null, null, null);
    }

    @Test
    public void test_ok() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"), null, null);
    }

    @Test
    public void test_ok2() throws Exception {
        new FileSystemImplementation("AAP", "NOOT", "file", "C:", new RelativePath("aap"), null, null);
    }

    
    @Test
    public void test_ID_ok() throws Exception {
        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);

        String tmp = fi.getUniqueID();
        assertEquals("NOOT", tmp);
    }

    @Test
    public void test_getCredential() throws Exception {
        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file","/", new RelativePath("aap"),
                null, null);

        Credential c = fi.getCredential();

        assertNull(c);
    }

    @Test
    public void test_getCredential2() throws Exception {
        Credential c = new PasswordCredential("aap", "noot".toCharArray());

        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                c, null);

        Credential c2 = fi.getCredential();

        assertSame(c, c2);
    }

    @Test
    public void test_name_ok() throws Exception {
        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);

        String tmp = fi.getAdaptorName();
        assertEquals("AAP", tmp);
    }

    @Test
    public void test_properties_ok() throws Exception {

        Map<String, String> tmp = new HashMap<>();
        tmp.put("test", "test");

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescriptionImplementation(
                "test", Type.STRING, EnumSet.of(Component.XENON), "test", "test property"));

        XenonProperties p = new XenonProperties(valid, tmp);

        FileSystemImplementation fi = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, p);

        Map<String, String> p2 = fi.getProperties();
        assertEquals("test", p2.get("test"));
    }

    @Test
    public void test_hashCode() throws Exception {
        int tmp = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"), null, null)
                .hashCode();

        int result = 31 + "AAP".hashCode();
        result = 31 * result + "NOOT".hashCode();

        assertEquals(tmp, result);
    }

    @Test
    public void test_equals1() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);

        assertEquals(f1, f1);
    }

    @Test
    public void test_equals2() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);
        boolean v = f1.equals(null);
        assertFalse(v);
    }

    @Test
    public void test_equals3() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);
        boolean v = f1.equals("Hello World");
        assertFalse(v);
    }

    @Test
    public void test_equals4() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);
        FileSystemImplementation f2 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);
        assertEquals(f1, f2);
    }

    @Test
    public void test_equals5() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);
        FileSystemImplementation f2 = new FileSystemImplementation("MIES", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);

        assertNotEquals(f1, f2);
    }

    @Test
    public void test_equals6() throws Exception {
        FileSystemImplementation f1 = new FileSystemImplementation("AAP", "NOOT", "file", "/", new RelativePath("aap"),
                null, null);
        FileSystemImplementation f2 = new FileSystemImplementation("AAP", "MIES", "file", "/", new RelativePath("aap"),
                null, null);

        assertNotEquals(f1, f2);
    }
}
