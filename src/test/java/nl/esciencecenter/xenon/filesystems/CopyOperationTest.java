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
package nl.esciencecenter.xenon.filesystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.FileSystem.CopyOperation;

public class CopyOperationTest {

	class FakeFileSystem extends MockFileSystem {

		public FakeFileSystem(String uniqueID, String name, String location, Path entryPath) throws XenonException {
			super(uniqueID, name, location, entryPath, null);
		} 
		
		public CopyOperation create(String uniqueID, FileSystem sourceFS, Path source, 
				FileSystem destinationFS, Path destination, CopyMode mode, boolean recursive) { 
			return new CopyOperation(uniqueID, sourceFS, source, destinationFS, destination, CopyMode.IGNORE, recursive);
		}
		
	}
	
	
	@Test
	public void test_getUniqueID() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(ID, o.getUniqueID());
	}

	@Test
	public void test_getSourceFS() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(sourceFS, o.getSourceFileSystem());
	}
	
	@Test
	public void test_getSource() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(source, o.getSourcePath());
	}


	@Test
	public void test_getDestinationFS() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(destinationFS, o.getDestinatonFileSystem());
	}
	
	
	@Test
	public void test_getDestination() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(destination, o.getDestinationPath());
	}
	
	@Test
	public void test_getMode() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(mode, o.getMode());
	}
	
	@Test
	public void test_isRecursiveFalse() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = false;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(recursive, o.isRecursive());
	}
		
	

	@Test
	public void test_isRecursiveTrue() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = true;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertEquals(recursive, o.isRecursive());
	}

	@Test
	public void test_cancelFalse() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = true;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertFalse(o.isCancelled());
	}

	@Test
	public void test_cancelTrue() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = true;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		assertFalse(o.isCancelled());
	}

	
	@Test
	public void test_toString() throws XenonException { 
		
		String ID = "ID";
		
		Path entry = new Path("/test");
		Path source = new Path("/test");
		Path destination = new Path("/test");
		
		CopyMode mode = CopyMode.IGNORE;
		boolean recursive = true;
		
		FakeFileSystem sourceFS = new FakeFileSystem("0", "TEST0", "MEM", entry);
		FakeFileSystem destinationFS = new FakeFileSystem("1", "TEST1", "MEM", entry);
		
		CopyOperation o = sourceFS.create(ID, sourceFS, source, destinationFS, destination, mode, recursive);
		
		String expected = "CopyHandle [ID]";
		
		assertEquals(expected, o.toString());
	}
}

