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
package nl.esciencecenter.xenon.adaptors.filesystems;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

import static org.junit.Assert.*;

public class PathAttributesTest {

	@Test
	public void test_creationTime() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setCreationTime(42);
		assertEquals(42, a.getCreationTime());
	}
	
	@Test
	public void test_lastModifiedTime() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setLastModifiedTime(42);
		assertEquals(42, a.getLastModifiedTime());
	}
	
	@Test
	public void test_accessTime() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setLastAccessTime(42);
		assertEquals(42, a.getLastAccessTime());
	}

	@Test
	public void test_size() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setSize(4200);
		assertEquals(4200, a.getSize());
	}

	@Test
	public void test_owner() throws Exception {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setOwner("aap");
		assertEquals("aap", a.getOwner());
	}

	@Test
	public void test_group() throws Exception {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setGroup("noot");
		assertEquals("noot", a.getGroup());
	}

	@Test
	public void test_permissions1() throws Exception {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setPermissions(null);
		assertNull(a.getPermissions());
	}
	
	@Test
	public void test_permissions2() throws Exception {
		PathAttributesImplementation a = new PathAttributesImplementation();
		
		Set<PosixFilePermission> p = new HashSet<>();
		p.add(PosixFilePermission.GROUP_EXECUTE);
		p.add(PosixFilePermission.OWNER_READ);
		p.add(PosixFilePermission.OTHERS_WRITE);
		a.setPermissions(p);

		assertEquals(p, a.getPermissions());
	}
	
	@Test
	public void test_directory1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setDirectory(true);
		assertTrue(a.isDirectory());
	}
	
	@Test
	public void test_directory2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setDirectory(false);
		assertFalse(a.isDirectory());
	}
	
	@Test
	public void test_regular1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setRegular(true);
		assertTrue(a.isRegular());
	}
	
	@Test
	public void test_regular2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setRegular(false);
		assertFalse(a.isRegular());
	}
	
	@Test
	public void test_symlink1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setSymbolicLink(true);
		assertTrue(a.isSymbolicLink());
	}
	
	@Test
	public void test_symlink2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setSymbolicLink(false);
		assertFalse(a.isSymbolicLink());
	}

	@Test
	public void test_other1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setOther(true);
		assertTrue(a.isOther());
	}
	
	@Test
	public void test_other2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setOther(false);
		assertFalse(a.isOther());
	}

	@Test
	public void test_exec1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setExecutable(true);
		assertTrue(a.isExecutable());
	}
	
	@Test
	public void test_exec2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setExecutable(false);
		assertFalse(a.isExecutable());
	}

	@Test
	public void test_readable1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setReadable(true);
		assertTrue(a.isReadable());
	}
	
	@Test
	public void test_readable2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setReadable(false);
		assertFalse(a.isReadable());
	}
	
	@Test
	public void test_writable1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setWritable(true);
		assertTrue(a.isWritable());
	}
	
	@Test
	public void test_writable2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setWritable(false);
		assertFalse(a.isWritable());
	}

	@Test
	public void test_hidden1() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setHidden(true);
		assertTrue(a.isHidden());
	}
	
	@Test
	public void test_hidden2() {
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setHidden(false);
		assertFalse(a.isHidden());
	}

    @Test
    public void test_hashcode() {
        PathAttributesImplementation a = new PathAttributesImplementation();
        PathAttributesImplementation b = new PathAttributesImplementation();
        assertEquals(a.hashCode(), b.hashCode());
    }
	
	@Test
    public void test_equals() {
        PathAttributesImplementation a = new PathAttributesImplementation();
        PathAttributesImplementation b = new PathAttributesImplementation();
        assertTrue(a.equals(b));
    }
	
	@Test
    public void test_equals_sameobj() {
        PathAttributesImplementation a = new PathAttributesImplementation();
        assertTrue(a.equals(a));
    }

    @Test
    public void test_equals_diffclass() {
        PathAttributesImplementation a = new PathAttributesImplementation();
        String b = "different class";
        assertFalse(a.equals(b));
    }

    @Test
    public void test_equals_null() {
        PathAttributesImplementation a = new PathAttributesImplementation();
        assertFalse(a.equals(null));
    }
}



