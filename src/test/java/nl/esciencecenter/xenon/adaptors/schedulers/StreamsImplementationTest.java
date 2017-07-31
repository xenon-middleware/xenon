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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import nl.esciencecenter.xenon.schedulers.Streams;

import static org.junit.Assert.*;

public class StreamsImplementationTest {

    @Test
    public void test_handle() throws Exception {
        String id = "JOB-42";
        InputStream stdout = new ByteArrayInputStream(new byte[0]);
        OutputStream stdin = new ByteArrayOutputStream();
        InputStream stderr = new ByteArrayInputStream(new byte[0]);

        Streams s = new StreamsImplementation(id, stdout, stdin, stderr);
        assertEquals(id, s.getJobIdentifier());
    }

    @Test
    public void test_stdout() throws Exception {
        String id = "JOB-42";
        InputStream stdout = new ByteArrayInputStream(new byte[0]);
        OutputStream stdin = new ByteArrayOutputStream();
        InputStream stderr = new ByteArrayInputStream(new byte[0]);

        Streams s = new StreamsImplementation(id, stdout, stdin, stderr);
        assertEquals(stdout, s.getStdout());
    }

    @Test
    public void test_stderr() throws Exception {
        String id = "JOB-42";
        InputStream stdout = new ByteArrayInputStream(new byte[0]);
        OutputStream stdin = new ByteArrayOutputStream();
        InputStream stderr = new ByteArrayInputStream(new byte[0]);

        Streams s = new StreamsImplementation(id, stdout, stdin, stderr);
        assertEquals(stderr, s.getStderr());
    }

    @Test
    public void test_stdin() throws Exception {
        String id = "JOB-42";
        InputStream stdout = new ByteArrayInputStream(new byte[0]);
        OutputStream stdin = new ByteArrayOutputStream();
        InputStream stderr = new ByteArrayInputStream(new byte[0]);

        Streams s = new StreamsImplementation(id, stdout, stdin, stderr);
        assertEquals(stdin, s.getStdin());
    }

    @Test
    public void test_hashCode() {
        StreamsImplementation s1 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        StreamsImplementation s2 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void test_equals_sameobj() {
        StreamsImplementation s1 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        assertTrue(s1.equals(s1));
    }

    @Test
    public void test_equals() {
        StreamsImplementation s1 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        StreamsImplementation s2 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        assertTrue(s1.equals(s2));
    }

    @Test
    public void test_equals_diffJobId_notequal() {
        StreamsImplementation s1 = new StreamsImplementation("JOB-11", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        StreamsImplementation s2 = new StreamsImplementation("JOB-88", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        assertFalse(s1.equals(s2));
    }

    @Test
    public void test_equals_diffclass() {
        StreamsImplementation s1 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        String s2 = "not a streams class";
        assertFalse(s1.equals(s2));
    }

    @Test
    public void test_equals_null() {
        StreamsImplementation s1 = new StreamsImplementation("JOB-42", new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), new ByteArrayInputStream(new byte[0]));
        assertFalse(s1.equals(null));
    }
}
