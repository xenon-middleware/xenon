package nl.esciencecenter.xenon.filesystems;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.CopyHandle;
import nl.esciencecenter.xenon.filesystems.CopyStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;

public class CopyStatusTest {

	class FakeCopyHandle implements CopyHandle {
		@Override
		public FileSystem getSourceFileSystem() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Path getSourcePath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileSystem getDestinatonFileSystem() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Path getDestinationPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopyMode getMode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isRecursive() {
			// TODO Auto-generated method stub
			return false;
		}
		
		public String toString() { 
			return "FakeCopyHandle";
		}
	}
	
	@Test
	public void test_getHandle() {
		CopyHandle h = new FakeCopyHandle();
		CopyStatus s = new CopyStatus(h, "TEST_STATE", 42, 31, null);
		assertEquals(h, s.getCopy());
	}
	
	@Test
	public void test_getState() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "TEST_STATE", 42, 31, null);
		assertEquals("TEST_STATE", s.getState());
	}
	
	@Test
	public void test_bytesToCopy() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "TEST_STATE", 42, 31, null);
		assertEquals(42, s.bytesToCopy());
	}
	
	@Test
	public void test_bytesCopied() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "TEST_STATE", 42, 31, null);
		assertEquals(31, s.bytesCopied());
	}
	
	@Test
	public void test_hasException1() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "TEST_STATE", 42, 31, null);
		assertFalse(s.hasException());
	}
	
	@Test
	public void test_hasException2() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "TEST_STATE", 42, 31, new Exception());
		assertTrue(s.hasException());
	}
	
	@Test
	public void test_isRunning1() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "TEST_STATE", 42, 31, null);
		assertFalse(s.isRunning());
	}

	@Test
	public void test_isRunning2() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "RUNNING", 42, 31, null);
		assertTrue(s.isRunning());
	}

	@Test
	public void test_isDone1() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "RUNNING", 42, 31, null);
		assertFalse(s.isDone());
	}

	@Test
	public void test_isDone2() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "DONE", 42, 31, null);
		assertTrue(s.isDone());
	}


	@Test
	public void test_isDone3() {
		CopyStatus s = new CopyStatus(new FakeCopyHandle(), "FAILED", 42, 31, null);
		assertTrue(s.isDone());
	}
	
	@Test
	public void test_toString() {
		CopyHandle copy = new FakeCopyHandle();
		String state = "STATE";
		Exception e = new Exception("OOPS");
		long bytesToCopy = 42;
		long bytesCopied = 3;
		
		String expected = "CopyStatus [copy=" + copy + ", state=" + state + ", exception=" + e + 
				", bytesToCopy=" + bytesToCopy + ", bytesCopied=" + bytesCopied + "]";
		
		CopyStatus s = new CopyStatus(copy, state, bytesToCopy, bytesCopied, e);
		assertEquals(expected, s.toString());
	}

			

	
}
