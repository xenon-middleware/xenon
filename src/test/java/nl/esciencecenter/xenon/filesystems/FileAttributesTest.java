package nl.esciencecenter.xenon.filesystems;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNull;

public class FileAttributesTest {

	@Test
	public void test_creationTime() {
		PathAttributes a = new PathAttributes();
		a.setCreationTime(42);
		assertEquals(42, a.getCreationTime());
	}
	
	@Test
	public void test_lastModifiedTime() {
		PathAttributes a = new PathAttributes();
		a.setLastModifiedTime(42);
		assertEquals(42, a.getLastModifiedTime());
	}
	
	@Test
	public void test_accessTime() {
		PathAttributes a = new PathAttributes();
		a.setLastAccessTime(42);
		assertEquals(42, a.getLastAccessTime());
	}

	@Test
	public void test_size() {
		PathAttributes a = new PathAttributes();
		a.setSize(4200);
		assertEquals(4200, a.getSize());
	}

	@Test
	public void test_owner() throws Exception {
		PathAttributes a = new PathAttributes();
		a.setOwner("aap");
		assertEquals("aap", a.getOwner());
	}

	@Test
	public void test_group() throws Exception {
		PathAttributes a = new PathAttributes();
		a.setGroup("noot");
		assertEquals("noot", a.getGroup());
	}

	@Test
	public void test_permissions1() throws Exception {
		PathAttributes a = new PathAttributes();
		a.setPermissions(null);
		assertNull(a.getPermissions());
	}
	
	@Test
	public void test_permissions2() throws Exception {
		PathAttributes a = new PathAttributes();
		
		Set<PosixFilePermission> p = new HashSet<>();
		p.add(PosixFilePermission.GROUP_EXECUTE);
		p.add(PosixFilePermission.OWNER_READ);
		p.add(PosixFilePermission.OTHERS_WRITE);
		a.setPermissions(p);

		assertEquals(p, a.getPermissions());
	}
	
	@Test
	public void test_directory1() {
		PathAttributes a = new PathAttributes();
		a.setDirectory(true);
		assertTrue(a.isDirectory());
	}
	
	@Test
	public void test_directory2() {
		PathAttributes a = new PathAttributes();
		a.setDirectory(false);
		assertFalse(a.isDirectory());
	}
	
	@Test
	public void test_regular1() {
		PathAttributes a = new PathAttributes();
		a.setRegular(true);
		assertTrue(a.isRegular());
	}
	
	@Test
	public void test_regular2() {
		PathAttributes a = new PathAttributes();
		a.setRegular(false);
		assertFalse(a.isRegular());
	}
	
	@Test
	public void test_symlink1() {
		PathAttributes a = new PathAttributes();
		a.setSymbolicLink(true);
		assertTrue(a.isSymbolicLink());
	}
	
	@Test
	public void test_symlink2() {
		PathAttributes a = new PathAttributes();
		a.setSymbolicLink(false);
		assertFalse(a.isSymbolicLink());
	}

	@Test
	public void test_other1() {
		PathAttributes a = new PathAttributes();
		a.setOther(true);
		assertTrue(a.isOther());
	}
	
	@Test
	public void test_other2() {
		PathAttributes a = new PathAttributes();
		a.setOther(false);
		assertFalse(a.isOther());
	}

	@Test
	public void test_exec1() {
		PathAttributes a = new PathAttributes();
		a.setExecutable(true);
		assertTrue(a.isExecutable());
	}
	
	@Test
	public void test_exec2() {
		PathAttributes a = new PathAttributes();
		a.setExecutable(false);
		assertFalse(a.isExecutable());
	}

	@Test
	public void test_readable1() {
		PathAttributes a = new PathAttributes();
		a.setReadable(true);
		assertTrue(a.isReadable());
	}
	
	@Test
	public void test_readable2() {
		PathAttributes a = new PathAttributes();
		a.setReadable(false);
		assertFalse(a.isReadable());
	}
	
	@Test
	public void test_writable1() {
		PathAttributes a = new PathAttributes();
		a.setWritable(true);
		assertTrue(a.isWritable());
	}
	
	@Test
	public void test_writable2() {
		PathAttributes a = new PathAttributes();
		a.setWritable(false);
		assertFalse(a.isWritable());
	}

	@Test
	public void test_hidden1() {
		PathAttributes a = new PathAttributes();
		a.setHidden(true);
		assertTrue(a.isHidden());
	}
	
	@Test
	public void test_hidden2() {
		PathAttributes a = new PathAttributes();
		a.setHidden(false);
		assertFalse(a.isHidden());
	}

	
	
	
	
	
}



