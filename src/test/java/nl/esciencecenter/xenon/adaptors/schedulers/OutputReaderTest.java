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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.junit.Test;

public class OutputReaderTest {

	@Test(expected=IllegalArgumentException.class)
	public void test_create_null() {
		OutputReader r = new OutputReader(null);
	}
	
	@Test
	public void test_simpleInput() {

		ByteArrayInputStream in = new ByteArrayInputStream("Hello World".getBytes());
		
		OutputReader r = new OutputReader(in);
		
		r.waitUntilFinished();
		
		assertTrue(r.isFinished());
		assertEquals("Hello World", r.getResultAsString());
	}
	
	@Test
	public void test_bigInput() {

		byte [] tmp = new byte[2500];
		
		for (int i=0;i<tmp.length;i++) { 
			tmp[i] = (byte) (i & 0xff);
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(tmp);
		
		OutputReader r = new OutputReader(in);
		
		r.waitUntilFinished();
		
		assertArrayEquals(tmp, r.getResult());
	}

	public class BrokenInputStream extends InputStream {

		private int breakAfter;
		private int readBytes;
		
		public BrokenInputStream(int breakAfter) { 
			this.breakAfter = breakAfter;
		}
		
		@Override
		public int read() throws IOException {
			if (readBytes >= breakAfter) { 
	 			throw new IOException("Boom!");
	 		}
	 		readBytes++;
			return 0;
		} 
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
	 		if (readBytes >= breakAfter) { 
	 			throw new IOException("Boom!");
	 		}
	 		
	 		if (readBytes + len >= breakAfter) { 
	 			len = breakAfter - readBytes;
	 			readBytes = breakAfter;
	 			return len;
	 		} else { 
	 			readBytes += len;
	 			return len;
	 		}
	 	}
	 	
		@Override
		public void close() throws IOException { 
	 		throw new IOException("Boom!");
	 	}
	}
	
	@Test
	public void test_brokenStream() {

		BrokenInputStream in = new BrokenInputStream(2000);
		
		OutputReader r = new OutputReader(in);
		
		r.waitUntilFinished();
		
		assertTrue(r.isFinished());
		assertEquals(2000, r.getResult().length);
	}

	
}
