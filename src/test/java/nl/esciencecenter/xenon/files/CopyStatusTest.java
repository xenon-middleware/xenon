package nl.esciencecenter.xenon.files;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.CopyHandle;
import nl.esciencecenter.xenon.filesystems.CopyStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;

public class CopyStatusTest {

	@Test
	public void test_getHandle() {
		CopyHandle h = mock(CopyHandle.class);
		CopyStatus s = new CopyStatus(h, "TEST_STATE", 42, 31, null);
		assertEquals(h, s.getCopy());
	}
	
	@Test
	public void test_getState() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "TEST_STATE", 42, 31, null);
		assertEquals("TEST_STATE", s.getState());
	}
	
	@Test
	public void test_bytesToCopy() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "TEST_STATE", 42, 31, null);
		assertEquals(42, s.bytesToCopy());
	}
	
	@Test
	public void test_bytesCopied() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "TEST_STATE", 42, 31, null);
		assertEquals(31, s.bytesCopied());
	}
	
	@Test
	public void test_hasException1() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "TEST_STATE", 42, 31, null);
		assertFalse(s.hasException());
	}
	
	@Test
	public void test_hasException2() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "TEST_STATE", 42, 31, new Exception());
		assertTrue(s.hasException());
	}
	
	@Test
	public void test_isRunning1() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "TEST_STATE", 42, 31, null);
		assertFalse(s.isRunning());
	}

	@Test
	public void test_isRunning2() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "RUNNING", 42, 31, null);
		assertTrue(s.isRunning());
	}

	@Test
	public void test_isDone1() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "RUNNING", 42, 31, null);
		assertFalse(s.isDone());
	}

	@Test
	public void test_isDone2() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "DONE", 42, 31, null);
		assertTrue(s.isDone());
	}


	@Test
	public void test_isDone3() {
		CopyStatus s = new CopyStatus(mock(CopyHandle.class), "FAILED", 42, 31, null);
		assertTrue(s.isDone());
	}
	
	
}
