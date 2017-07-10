package nl.esciencecenter.xenon.filesystems;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidAdaptorException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.local.LocalFileAdaptor;

public class FileSystemTest {


	class FakeCopyHandle implements CopyHandle {

		@Override
		public FileSystem getSourceFileSystem() {
			return null;
		}

		@Override
		public Path getSourcePath() {
			return null;
		}

		@Override
		public FileSystem getDestinatonFileSystem() {
			return null;
		}

		@Override
		public Path getDestinationPath() {
			return null;
		}

		@Override
		public CopyMode getMode() {
			return null;
		}

		@Override
		public boolean isRecursive() {
			return false;
		}
	}

	class Callback implements FileSystem.CopyCallback {

		boolean initial = true;
		long bytesToCopy;
		long bytes;
		long maxBytes;

		Callback(boolean initial, long maxBytes){
			this.initial = initial;
			this.maxBytes = maxBytes;
		}

		Callback(long maxBytes){ 
			this.maxBytes = maxBytes;
		}

		@Override
		public void setBytesToCopy(long bytes) {
			this.bytesToCopy = bytes;
		}

		@Override
		public void setBytesCopied(long bytes) {
			this.bytes = bytes;  
		}

		@Override
		public boolean isCancelled() {
			return (bytes >= maxBytes);
		} 
		
		
	}

	class CountIgnoreOutputStream extends OutputStream {

		long bytes = 0;

		public CountIgnoreOutputStream(){
		}

		@Override
		public void write(int b) {
			bytes++;
		} 

		@Override
		public void write(byte[] b) { 
			bytes += b.length;
		}

		@Override
		public void write(byte[] b, int off, int len) {
			bytes += len;
		}		
	}

	class CountJunkInputStream extends InputStream {

		final long size; 
		long bytes = 0;

		public CountJunkInputStream(long size){
			this.size = size; 
		}

		@Override
		public int read() {

			if (bytes >= size) {
				return -1;
			}

			bytes++;
			return 42;
		} 

		@Override
		public int read(byte[] b) {

			long left = size - bytes;

			if (left <= 0){ 
				return -1;
			}

			int len = b.length;

			if (left < b.length) { 
				len = (int) left;
			}

			for (int i=0;i<len;i++) { 
				b[i] = 42;
			}

			bytes += len;
			return len;
		}

		@Override
		public int read(byte[] b, int off, int len) {

			long left = size - bytes;

			if (left <= 0){ 
				return -1;
			}

			if (left < len) { 
				len = (int) left;
			}

			for (int i=0;i<len;i++) { 
				b[off+i] = 42;
			}

			bytes += len;
			return len;
		}		
	}

	class DelayInputStream extends InputStream {

		final long deadline; 
		long bytes = 0;

		public DelayInputStream(long delay){
			this.deadline= System.currentTimeMillis() + delay;
		}

		@Override
		public int read() {

			if (System.currentTimeMillis() >= deadline){ 
				return -1;
			}

			bytes++;
			return 42;
		} 

		@Override
		public int read(byte[] b) {

			if (System.currentTimeMillis() >= deadline){ 
				return -1;
			}

			for (int i=0;i<b.length;i++) { 
				b[i] = 42;
			}

			bytes += b.length;
			return b.length;
		}

		@Override
		public int read(byte[] b, int off, int len) {

			if (System.currentTimeMillis() >= deadline){ 
				return -1;
			}

			for (int i=0;i<len;i++) { 
				b[off+i] = 42;
			}

			bytes += len;
			return len;
		}		
	}

	// Testing against actual FileSystem

	@Test
	public void test_names() throws XenonException {
		String [] tmp = FileSystem.getAdaptorNames();
		String [] expected = new String [] { "file", "ftp", "sftp", "webdav" };
		assertTrue(Arrays.equals(expected, tmp));
	}

	@Test
	public void test_adaptorDescription() throws XenonException {

		FileSystemAdaptorDescription d = FileSystem.getAdaptorDescription("file");

		LocalFileAdaptor l = new LocalFileAdaptor();

		assertEquals("file", l.getName());
		assertEquals(LocalFileAdaptor.ADAPTOR_DESCRIPTION, d.getDescription());
		assertArrayEquals(LocalFileAdaptor.ADAPTOR_LOCATIONS, d.getSupportedLocations());
		assertArrayEquals(LocalFileAdaptor.VALID_PROPERTIES, d.getSupportedProperties());
	}

	@Test(expected=InvalidAdaptorException.class)
	public void test_adaptorDescriptionFailsNull() throws XenonException {
		FileSystem.getAdaptorDescription(null);
	}

	@Test(expected=InvalidAdaptorException.class)
	public void test_adaptorDescriptionFailsEmpty() throws XenonException {
		FileSystem.getAdaptorDescription("");
	}

	@Test(expected=InvalidAdaptorException.class)
	public void test_adaptorDescriptionFailsUnknown() throws XenonException {
		FileSystem.getAdaptorDescription("aap");
	}

	@Test
	public void test_adaptorDescriptions() throws XenonException {

		String [] names = FileSystem.getAdaptorNames();
		FileSystemAdaptorDescription [] desc = FileSystem.getAdaptorDescriptions();

		assertEquals(names.length, desc.length);

		for (int i=0;i<names.length;i++) { 
			assertEquals(FileSystem.getAdaptorDescription(names[i]), desc[i]);
		}
	}

	@Test
	public void test_create() throws XenonException {
		FileSystem f = FileSystem.create("file");

		System.err.println("GOT ADAPTOR " + f.getAdaptorName());

		assertEquals("file", f.getAdaptorName());
		f.close();
	}

	@Test(expected=InvalidAdaptorException.class)
	public void test_createFailsNull() throws XenonException {
		FileSystem.create(null);
	}

	@Test(expected=InvalidAdaptorException.class)
	public void test_createFailsEmpty() throws XenonException {
		FileSystem.create("");
	}

	@Test(expected=InvalidAdaptorException.class)
	public void test_createFailsUnknown() throws XenonException {
		FileSystem.create("aap");
	}

	// Testing against Fake FileSystem

	@Test(expected=IllegalArgumentException.class)
	public void test_constructorIdNull() throws XenonException {
		new TestFileSystem(null, "TEST", "MEM", new Path("/test"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_constructorAdaptorNull() throws XenonException {
		new TestFileSystem("0", null, "MEM", new Path("/test"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_constructorLocationNull() throws XenonException {
		new TestFileSystem("0", "TEST", null, new Path("/test"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_constructorPathNull() throws XenonException {
		new TestFileSystem("0", "TEST", "MEM", null);
	}

	@Test
	public void test_name() throws XenonException {
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", new Path("/test"));
		assertEquals("TEST", f.getAdaptorName());
	}

	@Test
	public void test_location() throws XenonException {
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", new Path("/test"));
		assertEquals("MEM", f.getLocation());
	}

	@Test
	public void test_path() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		assertEquals(entry, f.getEntryPath());
	}

	@Test
	public void test_createDirectories() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		Path test = entry.resolve("aap/noot/mies");

		f.createDirectories(test);

		assertTrue(f.exists(new Path("/test/aap/noot/mies")));
	}

	@Test(expected=PathAlreadyExistsException.class)
	public void test_createDirectoriesDup() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		Path test = entry.resolve("aap/noot/mies");

		f.createDirectories(test);
		f.createDirectories(test);
	}

	@Test
	public void test_deleteOk() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		Path test = entry.resolve("aap");
		f.createFile(test);
		f.delete(test, false);
		assertFalse(f.exists(new Path("/test/aap")));
	}

	@Test(expected=DirectoryNotEmptyException.class)
	public void test_deleteNotEmpty() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		Path test = entry.resolve("aap");
		f.createDirectories(test);
		f.createFile(test.resolve("file0"));
		f.delete(new Path("/test/aap"), false);
	}

	@Test
	public void test_deleteRecursive() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		Path test = entry.resolve("aap");
		f.createDirectories(test);
		f.createFile(test.resolve("file0"));
		f.delete(new Path("/test/aap"), true);
		assertFalse(f.exists(new Path("/test/aap")));
	}

	@Test
	public void test_deleteDirectories() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		Path test = entry.resolve("aap/noot/mies");

		f.createDirectories(test);
		f.createFile(new Path("/test/aap/test0"));
		f.createFile(new Path("/test/aap/noot/test1"));
		f.createFile(new Path("/test/aap/noot/mies/test2"));

		f.delete(new Path("/test/aap/noot"), true);

		assertTrue(f.exists(new Path("/test/aap")));
		assertFalse(f.exists(new Path("/test/aap/noot")));
	}


	@Test
	public void test_deleteWithDotDot() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		f.createDirectories(new Path("/test/."));
		f.createDirectories(new Path("/test/.."));
		f.createDirectories(new Path("/test/aap"));
		f.createDirectories(new Path("/test/aap/."));
		f.createDirectories(new Path("/test/aap/.."));
		f.createDirectories(new Path("/test/aap/noot"));
		f.createDirectories(new Path("/test/aap/noot/."));
		f.createDirectories(new Path("/test/aap/noot/.."));

		f.createFile(new Path("/test/aap/noot/file0"));

		f.delete(new Path("/test/aap/noot"), true);

		assertTrue(f.exists(new Path("/test/aap")));
		assertFalse(f.exists(new Path("/test/aap/noot")));
	}

	private boolean remove(List<PathAttributes> list, String pathname) { 

		Path p = new Path(pathname);

		Iterator<PathAttributes> itt = list.iterator();

		while (itt.hasNext()) { 

			PathAttributes tmp = itt.next();

			if (tmp.getPath().equals(p)) {
				itt.remove();
				return true;
			}
		}

		return false;		  
	}

	@Test
	public void test_listWithDotDot() throws XenonException {
		Path entry = new Path("/test");
		TestFileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		f.createDirectories(new Path("/test/."));
		f.createDirectories(new Path("/test/.."));
		f.createDirectories(new Path("/test/aap"));
		f.createDirectories(new Path("/test/aap/."));
		f.createDirectories(new Path("/test/aap/.."));
		f.createDirectories(new Path("/test/aap/noot"));
		f.createDirectories(new Path("/test/aap/noot/."));
		f.createDirectories(new Path("/test/aap/noot/.."));

		f.createFile(new Path("/test/aap/noot/file0"));

		List<PathAttributes> list = f.list(new Path("/test"), true);

		assertTrue(remove(list, "/test/aap/noot/file0"));
		assertTrue(remove(list, "/test/aap/noot/.."));
		assertTrue(remove(list, "/test/aap/noot/."));
		assertTrue(remove(list, "/test/aap/noot"));
		assertTrue(remove(list, "/test/aap/.."));
		assertTrue(remove(list, "/test/aap/."));
		assertTrue(remove(list, "/test/aap"));
		assertTrue(remove(list, "/test/.."));
		assertTrue(remove(list, "/test/."));
		assertTrue(list.isEmpty());
	}

	// assertPathExists

	@Test
	public void test_assertPathExistsFile() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		f.createFile(new Path("/test/aap/file0"));

		// should not throw exception
		f.assertPathExists(new Path("/test/aap/file0"));
	}

	@Test
	public void test_assertPathExistsDir() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));

		// should not throw exception
		f.assertPathExists(new Path("/test/aap"));
	}

	@Test(expected=NoSuchPathException.class)
	public void test_assertPathExistsFailsNoPath() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertPathExists(new Path("/test/aap"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertPathExistsFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertPathExists(null);
	}

	// assertPathNotExists

	@Test
	public void test_assertPathNotExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should not throw exception
		f.assertPathNotExists(new Path("/test/aap"));
	}

	@Test(expected=PathAlreadyExistsException.class)
	public void test_assertPathNotExistsFailsExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		// should throw exception
		f.assertPathNotExists(new Path("/test/aap"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertPathNotExistsFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertPathNotExists(null);
	}

	// assertPathIsFile

	@Test
	public void test_assertPathIsFile() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		f.createFile(new Path("/test/aap/file0"));
		// should not throw exception
		f.assertPathIsFile(new Path("/test/aap/file0"));
	}

	@Test(expected=NoSuchPathException.class)
	public void test_assertPathIfFileFailsNotExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		// should throw exception
		f.assertPathIsFile(new Path("/test/aap/file0"));
	}

	@Test(expected=InvalidPathException.class)
	public void test_assertPathIfFileFailsIsDir() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		// should throw exception
		f.assertPathIsFile(new Path("/test/aap"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertPathIsFileFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertPathIsFile(null);
	}

	// assertPathIsDirectory

	@Test
	public void test_assertPathIsDir() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		// should not throw exception
		f.assertPathIsDirectory(new Path("/test/aap"));
	}

	@Test(expected=NoSuchPathException.class)
	public void test_assertPathIsDirFailsNotExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertPathIsDirectory(new Path("/test/noot"));
	}

	@Test(expected=InvalidPathException.class)
	public void test_assertPathIsDirFailesIsFile() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		f.createFile(new Path("/test/aap/file0"));
		// should throw exception
		f.assertPathIsDirectory(new Path("/test/aap/file0"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertPathIsDirFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertPathIsDirectory(null);
	}

	// assertFileExists

	@Test
	public void test_assertFileExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);


		f.createDirectories(new Path("/test/aap"));
		f.createFile(new Path("/test/aap/file0"));

		// should not throw exception
		f.assertFileExists(new Path("/test/aap/file0"));
	}

	@Test(expected=NoSuchPathException.class)
	public void test_assertFileExistsFailsNotExist() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		f.createDirectories(new Path("/test/aap"));

		// should throw exception
		f.assertFileExists(new Path("/test/aap/file0"));
	}

	@Test(expected=InvalidPathException.class)
	public void test_assertFileExistsFailsIsDir() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		f.createDirectories(new Path("/test/aap"));

		// should throw exception
		f.assertFileExists(new Path("/test/aap"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertFileExistsPathNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertFileExists(null);
	}

	// assertDirectoryExists

	@Test
	public void test_assertDirExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		// should not throw exception
		f.assertDirectoryExists(new Path("/test/aap"));
	}

	@Test(expected=NoSuchPathException.class)
	public void test_assertDirExistsFailsNotExist() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertFileExists(new Path("/test/aap"));
	}

	@Test(expected=InvalidPathException.class)
	public void test_assertDirExistsFailsIsFile() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);

		f.createDirectories(new Path("/test/aap"));
		f.createFile(new Path("/test/aap/file0"));

		// should throw exception
		f.assertDirectoryExists(new Path("/test/aap/file0"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertDirExistsPathNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertDirectoryExists(null);
	}

	// assertParentDirecotyExists

	@Test
	public void test_assertParentDirExists() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.createDirectories(new Path("/test/aap"));
		f.createDirectories(new Path("/test/aap/noot"));
		// should not throw exception
		f.assertParentDirectoryExists(new Path("/test/aap/noot"));
	}

	@Test(expected=InvalidPathException.class)
	public void test_assertParentDirExistsPathFailsNoParent() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertParentDirectoryExists(new Path(""));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_assertParentDirExistsPathFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.assertParentDirectoryExists(null);
	}

	// areSamePaths

	@Test(expected=IllegalArgumentException.class)
	public void test_areSamePathsFailsNullSource() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.areSamePaths(null, new Path("/test"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_areSamePathsFailsNullTarget() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		// should throw exception
		f.areSamePaths(new Path("/test"), null);
	}

	@Test
	public void test_areSamePathsTrue() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		assertTrue(f.areSamePaths(new Path("/test"), new Path("/test")));
	}

	@Test
	public void test_areSamePathsFalse() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		assertFalse(f.areSamePaths(new Path("/noot"), new Path("/test")));
	}

	// isDotDot

	@Test
	public void test_isDotDotFalse() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		assertFalse(f.isDotDot(new Path("/test")));
	}

	public void test_isDotDotTrueDot() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		assertFalse(f.isDotDot(new Path("/test/.")));
	}

	public void test_isDotDotTrueDotDot() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		assertFalse(f.isDotDot(new Path("/test/..")));
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_isDotDotFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST", "MEM", entry);
		f.isDotDot(null);
	}

	// copyFile

	@Test(expected=InvalidPathException.class)
	public void test_copyFileFailsSourceOther() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		f0.createFile(f);

		PathAttributes a = new PathAttributes();
		a.setPath(f);
		a.setOther(true);
		f0.addAttributes(f, a);

		// should fail
		f0.copyFile(f, f1, f, CopyMode.CREATE, new Callback(1024));
	}

	@Test(expected=InvalidPathException.class)
	public void test_copyFileFailsSourceDir() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		f0.createDirectory(f);

		// should fail
		f0.copyFile(f, f1, f, CopyMode.CREATE, new Callback(1024));
	}

	@Test(expected=PathAlreadyExistsException.class)
	public void test_copyFileFailsDestExists() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		f0.createFile(f);
		f1.createFile(f);

		// should fail
		f0.copyFile(f, f1, f, CopyMode.CREATE, new Callback(1024));
	}

	@Test
	public void test_copyFileReplaceOK() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");

		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		f0.addData(f, data);

		f1.createFile(f);
		byte [] data1 = new byte [] { 42,42 };
		f1.addData(f, data1);

		// should replace
		f0.copyFile(f, f1, f, CopyMode.REPLACE, new Callback(1024));

		assertTrue(Arrays.equals(data, f1.getData(f)));
	}

	@Test
	public void test_copyFileIgnoreOK() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");

		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		f0.addData(f, data);

		f1.createFile(f);
		byte [] data1 = new byte [] { 42,42 };
		f1.addData(f, data1);

		// should replace
		f0.copyFile(f, f1, f, CopyMode.IGNORE, new Callback(1024));

		assertTrue(Arrays.equals(data1, f1.getData(f)));
	}

	@Test(expected = XenonException.class)
	public void test_copyFileCancelOK() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");

		f0.createFile(f);
		f0.addInputStream(f, new CountJunkInputStream(1024*1024));

		// should cancel after 1 block ?
		f0.copyFile(f, f1, f, CopyMode.CREATE, new Callback(4*1024));
	}

	@Test(expected = XenonException.class)
	public void test_copyFileCancelImmediately() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");

		f0.createFile(f);
		f0.addInputStream(f, new CountJunkInputStream(1024*1024));

		// should cancel after 1 block ?
		f0.copyFile(f, f1, f, CopyMode.CREATE, new Callback(false, 4*1024));
	}

	// copy

	@Test(expected=IllegalArgumentException.class)
	public void test_copyFailsSourceNull() throws XenonException {
		Path entry = new Path("/test");

		FileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		FileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		// should throw exception
		f0.copy(null, f1, new Path("/aap"), CopyMode.REPLACE, false);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_copyFailsDestFSNull() throws XenonException {
		Path entry = new Path("/test");

		FileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);

		// should throw exception
		f0.copy(new Path("/aap"), null, new Path("/aap"), CopyMode.REPLACE, false);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_copyFailsDestNull() throws XenonException {
		Path entry = new Path("/test");

		FileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		FileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		// should throw exception
		f0.copy(new Path("/aap"), f1, null, CopyMode.REPLACE, false);
	}

	@Test
	public void test_copyFileOK() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		f0.addData(f, data);

		CopyHandle h = f0.copy(f, f1, f, CopyMode.CREATE, false);
		f0.waitUntilDone(h, 5*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertFalse(s.hasException());
		assertTrue(f1.exists(f));
		assertTrue(Arrays.equals(data, f1.getData(f)));
	}

	/*
	@Test
	public void test_copyLinkOK() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		Path l = new Path("/test/link");

		
		f0.createSymbolicLink(l, p);
		PathAttributes a = new PathAttributes();
		a.setPath(f);
		a.setSymbolicLink(true);

		f0.addAttributes(f, a);
		f0.addData(f, data);

		CopyHandle h = f0.copy(f, f1, f, CopyMode.CREATE, false);
		f0.waitUntilDone(h, 5*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());

		System.out.println("Copy link " + s.getException());

		assertFalse(s.hasException());
		assertTrue(f1.exists(f));
		assertTrue(Arrays.equals(data, f1.getData(f)));
	}
	 */

	@Test
	public void test_copyFailsDestExists() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		f0.addData(f, data);

		f1.createFile(f);

		CopyHandle h = f0.copy(f, f1, f, CopyMode.CREATE, false);
		f0.waitUntilDone(h, 60*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertTrue(s.hasException());
		assertTrue(s.getException() instanceof PathAlreadyExistsException);
	}

	@Test
	public void test_copyOKreplace() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		f0.addData(f, data);

		byte [] data1 = new byte [] { 42, 42 };

		f1.createFile(f);
		f1.addData(f, data1);

		CopyHandle h = f0.copy(f, f1, f, CopyMode.REPLACE, false);
		f0.waitUntilDone(h, 60*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertFalse(s.hasException());
		assertTrue(Arrays.equals(data, f1.getData(f)));
	}

	@Test
	public void test_copyOKignore() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		byte [] data = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.createFile(f);
		f0.addData(f, data);

		byte [] data1 = new byte [] { 42, 42 };

		f1.createFile(f);
		f1.addData(f, data1);

		CopyHandle h = f0.copy(f, f1, f, CopyMode.IGNORE, false);
		f0.waitUntilDone(h, 60*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertFalse(s.hasException());
		assertTrue(Arrays.equals(data1, f1.getData(f)));
	}



	@Test
	public void test_copyFailsSourceNotExists() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		CopyHandle h = f0.copy(new Path("/test/aap"), f1, new Path("/test/aap"), CopyMode.CREATE, false);
		f0.waitUntilDone(h, 60*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertTrue(s.hasException());
		assertTrue(s.getException() instanceof NoSuchPathException);
	}

	@Test
	public void test_copyFailsSourceDir() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		f0.createDirectory(new Path("/test/aap"));

		CopyHandle h = f0.copy(new Path("/test/aap"), f1, new Path("/test/aap"), CopyMode.CREATE, false);
		f0.waitUntilDone(h, 60*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertTrue(s.hasException());
		assertTrue(s.getException() instanceof InvalidPathException);
	}

	@Test
	public void test_copyFailsSourceOther() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");
		f0.createFile(f);

		PathAttributes a = new PathAttributes();
		a.setPath(f);
		a.setOther(true);
		f0.addAttributes(f, a);

		CopyHandle h = f0.copy(new Path("/test/aap"), f1, new Path("/test/aap"), CopyMode.CREATE, false);
		f0.waitUntilDone(h, 60*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertTrue(s.hasException());
		assertTrue(s.getException() instanceof InvalidPathException);
	}

	@Test
	public void test_copyDirOK() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		f0.createDirectory(new Path("/test/aap"));
		f0.createFile(new Path("/test/aap/file0"));
		f0.createDirectory(new Path("/test/aap/noot"));
		f0.createFile(new Path("/test/aap/noot/file1"));

		byte [] data0 = new byte [] { 42, 42 };
		byte [] data1 = new byte [] { 0, 1, 2, 3, 4, 5, 6, 7 };

		f0.addData(new Path("/test/aap/file0"), data0);
		f0.addData(new Path("/test/aap/noot/file1"), data1);

		CopyHandle h = f0.copy(new Path("/test/aap"), f1, new Path("/test/aap"), CopyMode.CREATE, true);
		f0.waitUntilDone(h, 5*1000);

		CopyStatus s = f0.getStatus(h);
		assertTrue(s.isDone());
		assertFalse(s.hasException());

		System.out.println("COPYDIR: " + s.getException());

		assertTrue(f1.exists(new Path("/test/aap")));
		assertTrue(f1.exists(new Path("/test/aap/file0")));
		assertTrue(f1.exists(new Path("/test/aap/noot")));
		assertTrue(f1.exists(new Path("/test/aap/noot/file1")));
		assertTrue(Arrays.equals(data0, f1.getData(new Path("/test/aap/file0"))));
		assertTrue(Arrays.equals(data1, f1.getData(new Path("/test/aap/noot/file1"))));
	}

	// getStatus
	@Test(expected=IllegalArgumentException.class)
	public void test_getStatusFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST0", "MEM", entry);
		f.getStatus(null);
	}

	@Test(expected=NoSuchCopyException.class)
	public void test_getStatusFailsWrongType() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST0", "MEM", entry);
		f.getStatus(new FakeCopyHandle());
	}

	// cancel

	@Test(expected=IllegalArgumentException.class)
	public void test_cancelFailsNull() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST0", "MEM", entry);
		f.cancel(null);
	}

	@Test(expected=NoSuchCopyException.class)
	public void test_cancelFailsWrongType() throws XenonException {
		Path entry = new Path("/test");
		FileSystem f = new TestFileSystem("0", "TEST0", "MEM", entry);
		f.cancel(new FakeCopyHandle());
	}

	@Test
	public void test_cancelImmediately() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path file1 = new Path("/test/aap");
		Path file2 = new Path("/test/noot");

		f0.createFile(file1);
		f0.createFile(file2);

		f0.addInputStream(file1, new DelayInputStream(1000));
		f0.addInputStream(file2, new DelayInputStream(1000));

		f1.createFile(file1);
		f1.createFile(file2);

		f1.addOutputStream(file1, new CountIgnoreOutputStream());
		f1.addOutputStream(file2, new CountIgnoreOutputStream());

		CopyHandle h1 = f0.copy(file1, f1, file1, CopyMode.REPLACE, false);
		CopyHandle h2 = f0.copy(file2, f1, file2, CopyMode.REPLACE, false);

		// cancel h2 immediately -- the coopy should not have a chance to start
		CopyStatus s2 = f0.cancel(h2);

		CopyStatus s1 = f0.waitUntilDone(h1, 5*1000);

		assertTrue(s1.isDone());
		assertFalse(s1.hasException());

		assertTrue(s2.isDone());
		assertTrue(s2.hasException());
		assertTrue(s2.getException() instanceof XenonException);
	}

	private void sleep(long delay) { 
		try {
			Thread.sleep(delay);
		} catch (Exception e) {
			// ignore
		}
	}

	@Test
	public void test_cancelAfterDelay() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");

		f0.createFile(f);
		f0.addInputStream(f, new DelayInputStream(10000));

		f1.createFile(f);
		f1.addOutputStream(f, new CountIgnoreOutputStream());

		CopyHandle h = f0.copy(f, f1, f, CopyMode.REPLACE, false);

		sleep(1000);

		// cancel -- the copy should have started already, but should need 9 seconds more
		CopyStatus s = f0.cancel(h);

		assertTrue(s.isDone());
		assertTrue(s.hasException());
		assertTrue(s.getException() instanceof XenonException);
	}

	@Test
	public void test_cancelAfterDone() throws XenonException {
		Path entry = new Path("/test");

		TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", entry);
		TestFileSystem f1 = new TestFileSystem("1", "TEST1", "MEM", entry);

		Path f = new Path("/test/aap");

		byte [] data = new byte [] { 0, 1,2, 3, 4, 5, 6, 7, 8, 9 };

		f0.createFile(f);
		f0.addData(f, data);

		CopyHandle h = f0.copy(f, f1, f, CopyMode.CREATE, false);

		CopyStatus s = f0.waitUntilDone(h, 1000);

		System.out.println("BLA " + s.hasException() + " " + s.getException());
		
		assertTrue(s.isDone());
		assertFalse(s.hasException());

		// cancel -- the copy is already done 
		CopyStatus s2 = f0.cancel(h);

		assertTrue(s2.isDone());
		assertFalse(s2.hasException());
	}


	@Test
	public void test_properties() throws Exception {
		HashMap<String,String> p = new HashMap<>(); 
		p.put("aap", "noot");

		XenonPropertyDescription d = new XenonPropertyDescription("aap", Type.STRING, "empty", "test");
		XenonProperties prop = new XenonProperties(new XenonPropertyDescription [] { d }, p);

		TestFileSystem f = new TestFileSystem("0", "TEST0", "MEM", new Path("/test"), prop);
		assertEquals(p, f.getProperties());

	}
	
	
	  @Test
	  public void test_equalsTrueSelf() throws Exception {
		  TestFileSystem f = new TestFileSystem("0", "TEST0", "MEM", new Path("/test"));
		  assertTrue(f.equals(f));
	  }

	  @Test
	  public void test_equalsTrueSameID() throws Exception {
		  TestFileSystem f0 = new TestFileSystem("0", "TEST0", "MEM", new Path("/test"));
		  TestFileSystem f1 = new TestFileSystem("0", "TEST0", "MEM", new Path("/test"));
		  assertTrue(f0.equals(f1));
	  }

	  @Test
	  public void test_equalsFalseNull() throws Exception {
		  TestFileSystem f = new TestFileSystem("0", "TEST0", "MEM", new Path("/test"));
		  assertFalse(f.equals(null));
	  }

	  @Test
	  public void test_equalsFalseWrongType() throws Exception {
		  TestFileSystem f = new TestFileSystem("0", "TEST0", "MEM", new Path("/test"));
		  assertFalse(f.equals("hello"));
	  }

}