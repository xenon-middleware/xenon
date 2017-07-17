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
package nl.esciencecenter.xenon.schedulers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

public class StreamsTest {
	
	@Test
	public void test_handle() throws Exception {
		String id = "JOB-42";
		InputStream stdout = new ByteArrayInputStream(new byte[0]);
		OutputStream stdin = new ByteArrayOutputStream();
		InputStream stderr = new ByteArrayInputStream(new byte[0]);
		
		Streams s = new Streams(id, stdout, stdin, stderr);
		assertEquals(id, s.getJobIdentifier());
	}
	
	@Test
	public void test_stdout() throws Exception {
		String id = "JOB-42";
		InputStream stdout = new ByteArrayInputStream(new byte[0]);
		OutputStream stdin = new ByteArrayOutputStream();
		InputStream stderr = new ByteArrayInputStream(new byte[0]);
		
		Streams s = new Streams(id, stdout, stdin, stderr);
		assertEquals(stdout, s.getStdout());
	}
	
	@Test
	public void test_stderr() throws Exception {
		String id = "JOB-42";
		InputStream stdout = new ByteArrayInputStream(new byte[0]);
		OutputStream stdin = new ByteArrayOutputStream();
		InputStream stderr = new ByteArrayInputStream(new byte[0]);
		
		Streams s = new Streams(id, stdout, stdin, stderr);
		assertEquals(stderr, s.getStderr());
	}

	@Test
	public void test_stdin() throws Exception {
		String id = "JOB-42";
		InputStream stdout = new ByteArrayInputStream(new byte[0]);
		OutputStream stdin = new ByteArrayOutputStream();
		InputStream stderr = new ByteArrayInputStream(new byte[0]);
		
		Streams s = new Streams(id, stdout, stdin, stderr);
		assertEquals(stdin, s.getStdin());
	}
}
