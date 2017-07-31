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
package nl.esciencecenter.xenon.adaptors.schedulers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.utils.InputWriter;

public class InputWriterTest {

	@Test
	public void testCreate_null_input() { 
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		InputWriter w = new InputWriter(null, bos);
		w.waitUntilFinished();
	
		assertTrue(w.isFinished());
		assertEquals(0, bos.toByteArray().length);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreate_null_output() { 
		new InputWriter("Hello World", null);
	}

	@Test
	public void test_simple() { 
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		InputWriter w = new InputWriter("Hello World", bos);
		w.waitUntilFinished();
	
		assertTrue(w.isFinished());
		assertArrayEquals("Hello World".getBytes(), bos.toByteArray());
	}
	
	class BrokenOutputStream extends OutputStream {

		public void write(int arg0) throws IOException {
			throw new IOException("Boom!");
		} 

		public void write(byte [] data, int off, int len) throws IOException {
			throw new IOException("Boom!");
		} 

		public void close() throws IOException {
			throw new IOException("Boom!");
		} 
	}
	
	@Test
	public void test_brokenStream() { 
		InputWriter w = new InputWriter("Hello World", new BrokenOutputStream());
		w.waitUntilFinished();
		assertTrue(w.isFinished());
	}
	
}
