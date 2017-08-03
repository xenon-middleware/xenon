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
package nl.esciencecenter.xenon.filesystems;

import nl.esciencecenter.xenon.XenonException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CopyStatusTest {

	@Test
	public void test_getHandle() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, null);
		assertEquals("ID", s.getCopyIdentifier());
	}

	@Test
	public void test_getState() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, null);
		assertEquals("TEST_STATE", s.getState());
	}

	@Test
	public void test_bytesToCopy() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, null);
		assertEquals(42, s.bytesToCopy());
	}

	@Test
	public void test_bytesCopied() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, null);
		assertEquals(31, s.bytesCopied());
	}

	@Test
	public void test_hasException_null_false() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, null);
		assertFalse(s.hasException());
	}

	@Test
	public void test_hasException2_filled_true() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, new XenonException("file", "Something went wrong"));
		assertTrue(s.hasException());
	}

	@Test
	public void test_getException() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, new XenonException("file", "Something went wrong"));
		assertEquals("file adaptor: Something went wrong", s.getException().getMessage());
	}

	@Test( expected = XenonException.class)
	public void test_maybeThrowException() throws XenonException {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, new XenonException("file", "Something went wrong"));
		s.maybeThrowException();
	}

	@Test
	public void test_isRunning1() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "TEST_STATE", 42, 31, null);
		assertFalse(s.isRunning());
	}

	@Test
	public void test_isRunning2() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "RUNNING", 42, 31, null);
		assertTrue(s.isRunning());
	}

	@Test
	public void test_isDone1() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "RUNNING", 42, 31, null);
		assertFalse(s.isDone());
	}

	@Test
	public void test_isDone2() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "DONE", 42, 31, null);
		assertTrue(s.isDone());
	}


	@Test
	public void test_isDone3() {
		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", "FAILED", 42, 31, null);
		assertTrue(s.isDone());
	}

	@Test
	public void test_toString() {
		String state = "STATE";
		XenonException e = new XenonException("file", "Something went wrong");
		long bytesToCopy = 42;
		long bytesCopied = 3;

		String expected = "CopyStatus [copyIdentifier=ID" + ", state=" + state + ", exception=" + e +
				", bytesToCopy=" + bytesToCopy + ", bytesCopied=" + bytesCopied + "]";

		CopyStatus s = new FileSystem.CopyStatusImplementation("ID", state, bytesToCopy, bytesCopied, e);
		assertEquals(expected, s.toString());
	}

			

	
}
