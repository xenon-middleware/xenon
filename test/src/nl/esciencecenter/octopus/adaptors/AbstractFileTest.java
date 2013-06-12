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

package nl.esciencecenter.octopus.adaptors;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public abstract class AbstractFileTest {

    protected static final String ROOT = "octopus_test";
    protected static String TEST_ID = UUID.randomUUID().toString();
    
    protected Octopus octopus;
    protected Files files;
    protected AbsolutePath testDir;
    
    private long counter = 0;
    
    protected void prepare() throws Exception { 
        octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();
    }

    protected void cleanup() throws Exception { 
        octopus.end();
        
        files = null;
        octopus = null;
    }
    
    public abstract URI getCorrectURI() throws Exception;
    public abstract URI getURIWrongUser() throws Exception;
    public abstract URI getURIWrongLocation() throws Exception;
    public abstract URI getURIWrongPath() throws Exception;
   
    public abstract boolean supportURIUser();
    public abstract boolean supportURILocation();
    
    public abstract Credential getDefaultCredential() throws Exception;
    public abstract Credential getNonDefaultCredential() throws Exception;
    
    public abstract boolean supportNonDefaultCredential();
    public abstract boolean supportNullCredential();
    
    public abstract Properties getCorrectProperties() throws Exception;
    public abstract Properties getIncorrectProperties() throws Exception;
    
    public abstract boolean supportProperties();

    public abstract FileSystem getTestFileSystem() throws Exception;
    public abstract void closeTestFileSystem(FileSystem fs) throws Exception;
    
    public abstract boolean supportsClose();
    
    // Various util functions ------------------------------------------------------------

    class AllTrue implements DirectoryStream.Filter { 
        @Override
        public boolean accept(AbsolutePath entry) {
            return true;
        }
    }
    
    class AllFalse implements DirectoryStream.Filter { 
        @Override
        public boolean accept(AbsolutePath entry) {
            return false;
        }
    }
    
    class Select implements DirectoryStream.Filter { 
        
        private Set<AbsolutePath> set; 
        
        public Select(Set<AbsolutePath> set) { 
            this.set = set;
        }
        
        @Override
        public boolean accept(AbsolutePath entry) {
            return set.contains(entry);
        }
    }

    private void throwUnexpected(String name, Exception e) throws Exception { 
        throw new Exception(name + " throws unexpected Exception!", e);        
    }
    
    private void throwExpected(String name) throws Exception { 
        throw new Exception(name + " did NOT throw Exception which was expected!");        
    }
    
    private void throwWrong(String name, String expected, String result) throws Exception { 
        throw new Exception(name + " produced wrong result! Expected: " + expected + " but got: " + result);        
    }

    private void throwUnexpectedElement(String name, String element) throws Exception { 
        throw new Exception(name + " produced unexpected element: " + element);        
    }
    
    private void throwMissingElement(String name, String element) throws Exception { 
        throw new Exception(name + " did NOT produce element: " + element);        
    }
    
    private void throwMissingElements(String name, Collection elements) throws Exception { 
        throw new Exception(name + " did NOT produce elements: " + elements);        
    }
    
    private void close(Closeable c) { 

        if (c == null) { 
            return;
        }
        try {
            c.close();
        } catch (Exception e)  {
            // ignore
        }
    }
    
    // Depends on: AbsolutePath.resolve, RelativePath, exists
    private AbsolutePath createNewTestDirName(AbsolutePath root) throws Exception { 
        
        AbsolutePath dir = root.resolve(new RelativePath("dir" + counter));
        counter++;
        
        if (files.exists(dir)) { 
            throw new Exception("Generated test dir already exists! " + dir.getPath());
        }
        
        return dir;
    }

    // Depends on: [createNewTestDirName], createDirectory, exists
    private AbsolutePath createTestDir(AbsolutePath root) throws Exception { 
        
        AbsolutePath dir = createNewTestDirName(root);
        
        files.createDirectory(dir);
        
        if (!files.exists(dir)) { 
            throw new Exception("Failed to generate test dir! " + dir.getPath());
        }
        
        return dir;
    }
    
    // Depends on: [createTestDir]
    private void prepareTestDir(FileSystem fs, String testName) throws Exception {
        
        if (testDir != null) { 
            return;
        }

        AbsolutePath entry = fs.getEntryPath();
        testDir = entry.resolve(new RelativePath(new String [] { ROOT, TEST_ID, testName }));
        
        if (!files.exists(testDir)) { 
            files.createDirectories(testDir);
        }
    }

    
    // Depends on: AbsolutePath.resolve, RelativePath, exists 
    private AbsolutePath createNewTestFileName(AbsolutePath root) throws Exception { 
        
        AbsolutePath file = root.resolve(new RelativePath("file" + counter));
        counter++;

        if (files.exists(file)) { 
            throw new Exception("Generated NEW test file already exists! " + file.getPath());
        }

        return file;
    }
 
    // Depends on: newOutputStream
    private void writeData(AbsolutePath testFile, byte [] data) throws Exception { 
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.OPEN, OpenOption.TRUNCATE, OpenOption.WRITE);
        if (data != null) { 
            out.write(data);
        }
        out.close();        
    }
    
    // Depends on: [createNewTestFileName], createFile, [writeData]
    private AbsolutePath createTestFile(AbsolutePath root, byte [] data) throws Exception { 
        
        AbsolutePath file = createNewTestFileName(root);
        
        files.createFile(file);
        
        if (data != null && data.length > 0) { 
            writeData(file, data);
        }

        return file;
    }

    // Depends on: exists, isDirectory, delete
    private void deleteTestFile(AbsolutePath file) throws Exception { 

        if (!files.exists(file)) { 
            throw new Exception("Cannot delete non-existing file: " + file);
        }
        
        if (files.isDirectory(file)) {
            throw new Exception("Cannot delete directory: " + file);
        }
            
        files.delete(file);
    }

    // Depends on: exists, isDirectory, delete
    private void deleteTestDir(AbsolutePath dir) throws Exception { 

        if (!files.exists(dir)) { 
            throw new Exception("Cannot delete non-existing dir: " + dir);
        }
        
        if (!files.isDirectory(dir)) {
            throw new Exception("Cannot delete file: " + dir);
        }
            
        files.delete(dir);
    }

    private byte [] readFully(InputStream in) throws Exception { 
        
        byte [] buffer = new byte[1024];
        
        int offset = 0;
        int read = in.read(buffer, offset, buffer.length-offset);
        
        while (read != -1) { 
            
            offset += read;

            if (offset == buffer.length) { 
                buffer = Arrays.copyOf(buffer, buffer.length*2);
            }
            
            read = in.read(buffer, offset, buffer.length-offset);
        }
        
        return Arrays.copyOf(buffer, offset);
    }
    
    // The test start here.
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST newFileSystem 
    //
    // Possible parameters: 
    //   URI         - correct URI / wrong user / wrong location / wrong path
    //   Credentials - default / null / value
    //   Properties  - null / empty / set right / set wrong
    // 
    // Total combinations: 4 + 2 + 3 = 9
    // 
    // Depends on: newFileSystem, close
    
    private void test00_newFileSystem(URI uri, Credential c, Properties p, boolean mustFail) throws Exception {
        
        try { 
            FileSystem fs = files.newFileSystem(uri, c, p);
            files.close(fs);
        } catch (Exception e) { 
            if (mustFail) { 
                // exception was expected.
                return;
            } 

            // exception was not expected
            throwUnexpected("test00_newFileSystem", e);
        }
        
        if (mustFail) {
            // expected an exception!
            throwExpected("test00_newFileSystem");
        }
    } 
    
    @org.junit.Test
    public void test00_newFileSystem() throws Exception { 
        
        prepare();
        
        // test with correct URI with default credential and without properties
        test00_newFileSystem(getCorrectURI(), getDefaultCredential(), null, false);
 
        // test with wrong URI user with default credential and without properties
        if (supportURIUser()) { 
            test00_newFileSystem(getURIWrongUser(), getDefaultCredential(), null, true);
        }
        
        // test with wrong URI location with default credential and without properties
        if (supportURILocation()) { 
            test00_newFileSystem(getURIWrongLocation(), getDefaultCredential(), null, true);
        }
        
        // test with wrong URI path with default credential and without properties
        test00_newFileSystem(getURIWrongPath(), getDefaultCredential(), null, true);
        
        // test with correct URI without credential and without properties
        boolean allowNull = supportNullCredential();
        test00_newFileSystem(getCorrectURI(), null, null, !allowNull);
       
        // test with correct URI with non-default credential and without properties
        if (supportNonDefaultCredential()) { 
            test00_newFileSystem(getCorrectURI(), getNonDefaultCredential(), null, false);
        }
        
        // test with correct URI with default credential and with empty properties
        test00_newFileSystem(getCorrectURI(), getDefaultCredential(), new Properties(), false);
    
        // test with correct URI with default credential and with correct properties
        if (supportProperties()) { 
            test00_newFileSystem(getCorrectURI(), getDefaultCredential(), getCorrectProperties(), false);
        
            // test with correct URI with default credential and with wrong properties
            test00_newFileSystem(getCorrectURI(), getDefaultCredential(), getIncorrectProperties(), true);
        }
        
        cleanup();
    }
 
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST isOpen
    // 
    // Possible parameters: 
    // 
    // FileSystem - null / open FS / closed FS
    // 
    // Total combinations : 3
    // 
    // Depends on: [getTestFileSystem], close, isOpen

    private void test01_isOpen(FileSystem fs, boolean expected, boolean mustFail) throws Exception { 
        
        boolean result = false;
        
        try { 
            result = files.isOpen(fs);
        } catch (Exception e) { 
            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test01_isOpen", e);
        }
        
        if (mustFail) { 
            throwExpected("test01_isOpen");
        }
        
        if (result != expected) { 
            throwWrong("test01_isOpen", "" + expected, "" + result);
        }
    }
    
    @org.junit.Test
    public void test_isOpen() throws Exception { 

        prepare();
        
        // test with null filesystem 
        test01_isOpen(null, false, true);
        
        FileSystem fs = getTestFileSystem();

        // test with correct open filesystem
        test01_isOpen(fs, true, false);

        if (supportsClose()) { 
            files.close(fs);
            
            // test with correct closed filesystem
            test01_isOpen(fs, false, false);
        }   
        
        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST close
    // 
    // Possible parameters: 
    // 
    // FileSystem - null / open FS / closed FS
    // 
    // Total combinations : 3
    // 
    // Depends on: [getTestFileSystem], close

    private void test02_close(FileSystem fs, boolean mustFail) throws Exception { 
        
        try { 
            files.close(fs);
        } catch (Exception e) { 
            if (mustFail) { 
                // expected
                return;
            }
            throwUnexpected("test02_close", e);
        }
        
        if (mustFail) { 
            throwExpected("test02_close");
        }
    }
    
    @org.junit.Test
    public void test_close() throws Exception { 

        prepare();
        
        // test with null filesystem 
        test02_close(null, true);
        
        if (supportsClose()) { 

            FileSystem fs = getTestFileSystem();

            // test with correct open filesystem
            test02_close(fs, false);

            // test with correct closed filesystem
            test02_close(fs, true);
        }
        
        cleanup();
    }
    
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST newPath
    // 
    // Possible parameters: 
    //
    // FileSystem - null / correct 
    // RelativePath - null / empty / value
    //
    // Total combinations : 2
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), AbsolutePath.getPath(), RelativePath, close
 
    private void test03_newPath(FileSystem fs, RelativePath path, String expected, boolean mustFail) throws Exception { 
        
        String result = null;
        
        try { 
            result = files.newPath(fs, path).getPath();
        } catch (Exception e) { 
            if (mustFail) { 
                // expected exception
                return;
            } 
            
            throwUnexpected("test03_newPath", e);
        }
        
        if (mustFail) { 
            throwExpected("test03_newPath");
        }
        
        if (!result.equals(expected)) {
            throwWrong("test03_newPath", expected, result);
        }
    }
    
    @org.junit.Test
    public void test03_newPath() throws Exception { 
        
        prepare();
        
        FileSystem fs = getTestFileSystem();
        String root = "/";
        
        // test with null filesystem and null relative path 
        test03_newPath(null, null, null, true);
        
        // test with correct filesystem and null relative path 
        test03_newPath(fs, null, null, true);
        
        // test with correct filesystem and empty relative path 
        test03_newPath(fs, new RelativePath(), root, false);
        
        // test with correct filesystem and relativepath with value
        test03_newPath(fs, new RelativePath("test"), root + "test", false);
        
        files.close(fs);
        
        cleanup();
    }    
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createDirectory
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing dir / existing file / non-exising parent / closed filesystem    
    // 
    // Total combinations : 5
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], [createTestFile], 
    //             createDirectory, [deleteTestDir], [deleteTestFile], [closeTestFileSystem]

    private void test04_createDirectory(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.createDirectory(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test04_createDirectory", e);
        }
        
        if (mustFail) { 
            throwExpected("test04_createDirectory");
        }
    }
    
    @org.junit.Test
    public void test04_createDirectory() throws Exception { 

        prepare();

        // test with null
        test04_createDirectory(null, true);
        
        FileSystem fs = getTestFileSystem();      
        
        AbsolutePath entry = fs.getEntryPath();
        AbsolutePath root = entry.resolve(new RelativePath(new String [] { ROOT }));

        // test with non-existing dir
        test04_createDirectory(root, false);

        // test with existing dir
        test04_createDirectory(root, true);

        // test with existing file 
        AbsolutePath file0 = createTestFile(root, null);
        test04_createDirectory(file0, true);
        deleteTestFile(file0);
        
        // test with non-existent parent dir
        AbsolutePath parent = createNewTestDirName(root);
        AbsolutePath dir0 = createNewTestDirName(parent);
        test04_createDirectory(dir0, true);
        
        // cleanup 
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test04_createDirectory(root, true);
        }

        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createDirectories
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing dir / dir with existing parents / dir with non existing parents / 
    //               dir where last parent is file / closed filesystem    
    // 
    // Total combinations : 7
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories, 
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test05_createDirectories(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.createDirectories(path);
            
            assert(files.exists(path));
            assert(files.isDirectory(path));
            
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test05_createDirectories", e);
        }
        
        if (mustFail) { 
            throwExpected("createDirectory");
        }
    }
    
    @org.junit.Test
    public void test05_createDirectories() throws Exception { 

        prepare();

        // test with null
        test05_createDirectories(null, true);
        
        FileSystem fs = getTestFileSystem();
        
        AbsolutePath entry = fs.getEntryPath();
        AbsolutePath root = entry.resolve(new RelativePath(new String [] { ROOT, TEST_ID, "test05_createDirectories" }));
               
        // test with non-existing dir
        test05_createDirectories(root, false);

        // test with existing dir
        test05_createDirectories(root, true);

        // dir with existing parents 
        AbsolutePath dir0 = createNewTestDirName(root);
        test05_createDirectories(dir0, false);
        deleteTestDir(dir0);
        
        // dir with non-existing parents 
        AbsolutePath dir1 = createNewTestDirName(dir0);
        test05_createDirectories(dir1, false);
        
        // dir where last parent is file 
        AbsolutePath file0 = createTestFile(dir0, null);
        AbsolutePath dir2 = createNewTestDirName(file0);
        test05_createDirectories(dir2, true);

        // cleanup 
        deleteTestDir(dir1);
        deleteTestFile(file0);        
        deleteTestDir(dir0);
        deleteTestDir(root);

        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test05_createDirectories(root, true);
        }

        cleanup();
    }
    
    // From this point on we can use prepareTestDir 
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: isDirectory
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file / existing dir / closed filesystem
    // 
    // Total combinations : 4
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], [createTestFile], [deleteTestFile] 
    //             [closeTestFileSystem]
    
    private void test06_isDirectory(AbsolutePath path, boolean expected, boolean mustFail) throws Exception { 
        
        boolean result = false;
        
        try { 
            result = files.isDirectory(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test06_isDirectory", e);
        }
        
        if (mustFail) { 
            throwExpected("test06_isDirectory");
        }
        
        if (result != expected) { 
            throwWrong("test06_isDirectory", "" + expected, "" + result);
        }
    }

    @org.junit.Test
    public void test06_isDirectory() throws Exception { 

        prepare();
        
        // prepare
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test06_isDirectory");
        
        // test with null        
        test06_isDirectory(null, false, true);
                
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(testDir);        
        test06_isDirectory(file0, false, true);
        
        // test with existing file
        AbsolutePath file1 = createTestFile(testDir, null);        
        test06_isDirectory(file1, false, false);
        deleteTestFile(file1);
        
        // test with existing dir
        test06_isDirectory(testDir, true, false);
        
        // cleanup        
        deleteTestDir(testDir);
        closeTestFileSystem(fs);      
        
        if (supportsClose()) { 
            // test with closed filesystem
            test06_isDirectory(testDir, true, true);
        }        
        
        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createFile
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file / existing dir / non-existing parent / closed filesystem    
    // 
    // Total combinations : 6
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], createFile, delete, [deleteTestDir] 
    //             [closeTestFileSystem]

    private void test07_createFile(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.createFile(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test07_createFile", e);
        }
        
        if (mustFail) { 
            throwExpected("test07_createFile");
        }
    }
    
    @org.junit.Test
    public void test07_createFile() throws Exception { 

        prepare();
        
        // prepare
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test07_createFile");
        
        // test with null        
        test07_createFile(null, true);
                
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(testDir);        
        test07_createFile(file0, false);
        
        // test with existing file
        test07_createFile(file0, true);
        
        // test with existing dir
        test07_createFile(testDir, true);
        
        AbsolutePath tmp = createNewTestDirName(testDir);
        AbsolutePath file1 = createNewTestFileName(tmp);
        
        // test with non-existing parent
        test07_createFile(file1, true);

        // cleanup 
        files.delete(file0);
        deleteTestDir(testDir);
        closeTestFileSystem(fs);      
        
        if (supportsClose()) { 
            // test with closed filesystem
            test07_createFile(file0, true);
        }        
        
        cleanup();
    }
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: exists
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file   
    // 
    // Total combinations : 3 
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], [createTestFile], [deleteTestFile], 
    //             [closeTestFileSystem], exists  
 
    private void test08_exists(AbsolutePath path, boolean expected, boolean mustFail) throws Exception { 
 
        boolean result = false;
        
        try { 
            result = files.exists(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test08_exists", e);
        }
        
        if (mustFail) { 
            throwExpected("test08_exists");
        }
        
        if (result != expected) { 
            throwWrong("test08_exists", "" + expected, "" + result);
        }
    }
    
    @org.junit.Test
    public void test08_exists() throws Exception { 
    
        prepare();
        
        // prepare
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test08_exists");
        
        // test with null
        test08_exists(null, false, true);
        
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(testDir);        
        test08_exists(file0, false, false);
        
        // test with existing file
        AbsolutePath file1 = createTestFile(testDir, null);        
        test08_exists(file1, true, false);
        deleteTestFile(file1);
        
        // cleanup
        deleteTestDir(testDir);
        closeTestFileSystem(fs);
        
        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: delete
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file / existing empty dir / existing non-empty dir / 
    //              existing non-writable file / closed filesystem    
    // 
    // Total combinations : 7
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], delete, [deleteTestFile], [deleteTestDir] 
    //             [closeTestFileSystem]
        
    private void test09_delete(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.delete(path);                       
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test09_delete", e);
        }
        
        if (files.exists(path)) { 
            throwWrong("test09_delete", "no file", "a file");
        }
        
        if (mustFail) { 
            throwExpected("test09_delete");
        }
    }
    
    @org.junit.Test
    public void test09_delete() throws Exception { 

        prepare();

        // test with null
        test09_delete(null, true);
        
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test09_delete");        

        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(testDir);        
        test09_delete(file0, true);

        // test with existing file
        AbsolutePath file1 = createTestFile(testDir, null);        
        test09_delete(file1, false);

        // test with existing empty dir 
        AbsolutePath dir0 = createTestDir(testDir);        
        test09_delete(dir0, false);

        // test with existing non-empty dir
        AbsolutePath dir1 = createTestDir(testDir);
        AbsolutePath file2 = createTestFile(dir1, null);                
        test09_delete(dir1, true);

        // test with non-writable file 
//        AbsolutePath file3 = createTestFile(testDir, null);
//        files.setPosixFilePermissions(file3, new HashSet<PosixFilePermission>());
        
//      System.err.println("Attempting to delete: " + file3.getPath() + " " + files.getAttributes(file3));
        
//        test09_delete(file3, true);
        
        // cleanup
        deleteTestFile(file2);
        deleteTestDir(dir1);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test09_delete(testDir, true);
        }

        cleanup();
    }
   
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: size
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file size 0 / existing file size N / file from closed FS  
    // 
    // Total combinations : 5
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], [createTestFile], [deleteTestFile], 
    //             [deleteTestDir], [closeTestFileSystem], size, close  
    
    private void test10_size(AbsolutePath path, long expected, boolean mustFail) throws Exception { 
        
        long result = -1;
        
        try { 
            result = files.size(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test10_size", e);
        }
        
        if (mustFail) { 
            throwExpected("test10_size");
        }
        
        if (result != expected) { 
            throwWrong("test10_size", "" + expected, "" + result);
        }
    }
    
    @org.junit.Test
    public void test10_size() throws Exception { 
    
        prepare();
        
        // test with null parameter 
        test10_size(null, -1, true);
        
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test10_size");
        
        // test with non existing file
        AbsolutePath file1 = createNewTestFileName(testDir);        
        test10_size(file1, -1, true);
        
        // test with existing empty file
        AbsolutePath file2 = createTestFile(testDir, new byte[0]);
        test10_size(file2, 0, false);
        deleteTestFile(file2);        
        
        // test with existing filled file
        AbsolutePath file3 = createTestFile(testDir, new byte[13]);        
        test10_size(file3, 13, false);
        deleteTestFile(file3);        

        // test with closed filesystem
        if (supportsClose()) { 
            closeTestFileSystem(fs);
            test10_size(file1, 0, false);               
        }
        
        deleteTestDir(testDir);
        
        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newDirectoryStream 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs / 
    //              existing file / closed filesystem    
    // 
    // Total combinations : 7
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestDirName], [createTestFile], newDirectoryStream,   
    //             [deleteTestDir], , [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test11_newDirectoryStream(AbsolutePath root, Set<AbsolutePath> expected, boolean mustFail) throws Exception { 
        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        
        if (expected != null) { 
            tmp.addAll(expected);
        }
        
        DirectoryStream<AbsolutePath> in = null;
        
        try { 
            in = files.newDirectoryStream(root);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test11_newDirectoryStream", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("test11_newDirectoryStream");
        }
        
        for (AbsolutePath p : in) { 
            
            if (tmp.contains(p)) { 
                tmp.remove(p);
            } else { 
                close(in);
                throwUnexpectedElement("test11_newDirectoryStream", p.getPath());
            }
        }
        
        close(in);
        
        if (tmp.size() > 0) { 
            throwMissingElements("test11_newDirectoryStream", tmp);
        }
    }

    @org.junit.Test
    public void test11_newDirectoryStream() throws Exception { 
    
        prepare();

        // test with null
        test11_newDirectoryStream(null, null, true);
       
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test11_newDirectoryStream");
               
        // test with empty dir 
        test11_newDirectoryStream(testDir, null, false);
         
        // test with non-existing dir
        AbsolutePath dir0 = createNewTestDirName(testDir);
        test11_newDirectoryStream(dir0, null, true);
        
        // test with exising file
        AbsolutePath file0 = createTestFile(testDir, null);
        test11_newDirectoryStream(file0, null, true);
        
        // test with non-empty dir
        AbsolutePath file1 = createTestFile(testDir, null);
        AbsolutePath file2 = createTestFile(testDir, null);
        AbsolutePath file3 = createTestFile(testDir, null);
        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        tmp.add(file3);
        
        test11_newDirectoryStream(testDir, tmp, false);
        
        // test with subdirs 
        AbsolutePath dir1 = createTestDir(testDir);
        AbsolutePath file4 = createTestFile(dir1, null);
         
        tmp.add(dir1);
        
        test11_newDirectoryStream(testDir, tmp, false);
        
        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test11_newDirectoryStream(testDir, null, true);
        }

        cleanup();
    }
 
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newDirectoryStream with filter 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs / 
    //              existing file / closed filesystem    
    // 
    // directoryStreams.Filter null / filter returns all / filter returns none / filter selects one. 
    
    // Total combinations : 7 + 8
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories, 
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    public void test12_newDirectoryStream(AbsolutePath root, DirectoryStream.Filter filter, Set<AbsolutePath> expected, 
            boolean mustFail) throws Exception { 
        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        
        if (expected != null) { 
            tmp.addAll(expected);
        }
        
        DirectoryStream<AbsolutePath> in = null;
        
        try { 
            in = files.newDirectoryStream(root, filter);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test12_newDirectoryStream_with_filter", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("test12_newDirectoryStream_with_filter");
        }
        
        for (AbsolutePath p : in) { 
            
            if (tmp.contains(p)) { 
                tmp.remove(p);
            } else { 
                close(in);
                throwUnexpectedElement("test12_newDirectoryStream_with_filter", p.getPath());
            }
        }
        
        close(in);
        
        if (tmp.size() > 0) { 
            throwMissingElements("test12_newDirectoryStream_with_filter", tmp);
        }
    }

    @org.junit.Test
    public void test12_newDirectoryStream_with_filter() throws Exception { 
        
        prepare();
        
        // test with null 
        test12_newDirectoryStream(null, null, null, true);
       
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test12_newDirectoryStream_with_filter");
               
        // test with empty dir + null filter 
        test12_newDirectoryStream(testDir, null, null, true);
        
        // test with empty dir + true filter 
        test12_newDirectoryStream(testDir, new AllTrue(), null, false);
        
        // test with empty dir + false filter 
        test12_newDirectoryStream(testDir, new AllTrue(), null, false);
        
        // test with non-existing dir
        AbsolutePath dir0 = createNewTestDirName(testDir);
        test12_newDirectoryStream(dir0, new AllTrue(), null, true);
        
        // test with existing file
        AbsolutePath file0 = createTestFile(testDir, null);
        test12_newDirectoryStream(file0, new AllTrue(), null, true);
        
        // test with non-empty dir and allTrue
        AbsolutePath file1 = createTestFile(testDir, null);
        AbsolutePath file2 = createTestFile(testDir, null);
        AbsolutePath file3 = createTestFile(testDir, null);
        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        tmp.add(file3);
        
        test12_newDirectoryStream(testDir, new AllTrue(), tmp, false);
 
        // test with non-empty dir and allFalse
        test12_newDirectoryStream(testDir, new AllFalse(), null, false);
        
        tmp.remove(file3);
        
        // test with non-empty dir and select        
        test12_newDirectoryStream(testDir, new Select(tmp), tmp, false);
        
        // test with subdirs 
        AbsolutePath dir1 = createTestDir(testDir);
        AbsolutePath file4 = createTestFile(dir1, null);
        
        test12_newDirectoryStream(testDir, new Select(tmp), tmp, false);
        
        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test12_newDirectoryStream(testDir, new AllTrue(), null, true);
        }

        cleanup();
    }
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: getAttributes 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing empty file / existing non-empty file / existing dir / existing link (!) 
    //              closed filesystem    
    // 
    // Total combinations : 7
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories, 
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]
    
    private void test13_getAttributes(AbsolutePath path, boolean isDirectory, long size, boolean mustFail) throws Exception { 

        FileAttributes result = null;
        
        try { 
            result = files.getAttributes(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test13_getFileAttributes", e);
        }
        
        if (mustFail) {             
            throwExpected("test13_getFileAttributes");
        }
        
        if (result.isDirectory() && !isDirectory) { 
            throwWrong("test13_getfileAttributes", "<not directory>", "<directory>");
        }
        
        if (size >= 0 && result.size() != size) { 
            throwWrong("test13_getfileAttributes", "size=" + size, "size=" + result.size());
        }        
    }
    
    @org.junit.Test
    public void test13_getAttributes() throws Exception { 
   
        prepare();
        
        // test with null
        test13_getAttributes(null, false, -1, true);
        
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test13_getAttributes");
        
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(testDir);
        test13_getAttributes(file0, false, -1, true);
        
        // test with existing empty file
        AbsolutePath file1 = createTestFile(testDir, null);
        test13_getAttributes(file1, false, 0, false);
        
        // test with existing non-empty file
        AbsolutePath file2 = createTestFile(testDir, new byte [] { 1, 2, 3 });
        test13_getAttributes(file2, false, 3, false);
        
        // test with existing dir 
        AbsolutePath dir0 = createTestDir(testDir);
        test13_getAttributes(dir0, true, -1, false);
        
        // TODO: test with link!

        deleteTestDir(dir0);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test13_getAttributes(testDir, false, -1, true);
        }
    }
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: setPosixFilePermissions 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file / existing dir / existing link (!) / closed filesystem
    // Set<PosixFilePermission> null / empty set / [various correct set] 
    // 
    // Total combinations : N
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories, 
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]
    
    private void test14_setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions, boolean mustFail) 
            throws Exception { 
        
        try { 
            files.setPosixFilePermissions(path, permissions);
        } catch (Exception e) { 
            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test14_setPosixFilePermissions", e);
        }
        
        if (mustFail) {             
            throwExpected("test14_setPosixFilePermissions");
        }   
        
        // Check result
        FileAttributes attributes = files.getAttributes(path);
        Set<PosixFilePermission> tmp = attributes.permissions();

        if (!permissions.equals(tmp)) { 
            throwWrong("test14_setPosixFilePermissions", permissions.toString(), tmp.toString());
        }
    }
    
    @org.junit.Test
    public void test14_setPosixFilePermissions() throws Exception { 
        
        prepare();
        
        // test with null, null
        test14_setPosixFilePermissions(null, null, true);
        
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test14_setPosixFilePermissions");
        
        // test with existing file, null set
        AbsolutePath file0 = createTestFile(testDir, null);
        test14_setPosixFilePermissions(file0, null, true);
        
        // test with existing file, empty set 
        Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();
        test14_setPosixFilePermissions(file0, permissions, false);
        
        // test with existing file, non-empty set 
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        test14_setPosixFilePermissions(file0, permissions, false);
        
        permissions.add(PosixFilePermission.OTHERS_READ);
        test14_setPosixFilePermissions(file0, permissions, false);
        
        permissions.add(PosixFilePermission.GROUP_READ);
        test14_setPosixFilePermissions(file0, permissions, false);
        
        // test with non-existing file
        AbsolutePath file1 = createNewTestFileName(testDir);
        test14_setPosixFilePermissions(file1, permissions, true);
        
        // test with existing dir 
        AbsolutePath dir0 = createTestDir(testDir);
        
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        test14_setPosixFilePermissions(dir0, permissions, false);
        
        permissions.add(PosixFilePermission.OTHERS_READ);
        test14_setPosixFilePermissions(dir0, permissions, false);
        
        permissions.add(PosixFilePermission.GROUP_READ);
        test14_setPosixFilePermissions(dir0, permissions, false);
                
        deleteTestDir(dir0);
        deleteTestFile(file0);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test14_setPosixFilePermissions(file0, permissions, true);
        }
    }
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newAttributesDirectoryStream 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs / 
    //              existing file / closed filesystem    
    // 
    // Total combinations : 7
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestDirName], [createTestFile], newDirectoryStream,   
    //             [deleteTestDir], , [deleteTestFile], [deleteTestDir], [closeTestFileSystem]
    
    private void test15_newAttributesDirectoryStream(AbsolutePath root, Set<PathAttributesPair> expected, boolean mustFail) 
            throws Exception { 
        
        Set<PathAttributesPair> tmp = new HashSet<PathAttributesPair>();
        
        if (expected != null) { 
            tmp.addAll(expected);
        }
        
        DirectoryStream<PathAttributesPair> in = null;
        
        try { 
            in = files.newAttributesDirectoryStream(root);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test15_newAttributesDirectoryStream", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("test15_newAttributesDirectoryStream");
        }
        
        System.err.println("Comparing PathAttributesPairs!");
        
        for (PathAttributesPair p : in) { 
            
            System.err.println("Got " + p.path().getPath());
                
            PathAttributesPair found = null;
            
            for (PathAttributesPair x : tmp) { 
                
                System.err.println("Comparing " + x.path().getPath() + " " + x.attributes());

                if (x.path().equals(p.path()) && x.attributes().equals(p.attributes())) { 
                    System.err.println("Found!");
                    found = x;
                    break;
                }
            }

            if (found != null) { 
                tmp.remove(found);
            } else { 
                System.err.println("NOT Found!");
                close(in);
                throwUnexpectedElement("test15_newAttributesDirectoryStream", p.path().getPath());
            
            }
                        
//            if (tmp.contains(p)) {
//                System.err.println("Found!");
//                tmp.remove(p);
//            } else {
//                System.err.println("NOT Found!");
//                
//                close(in);
//                throwUnexpectedElement("newAttributesDirectoryStream", p.path().getPath());
//            }
        }
        
        close(in);
        
        if (tmp.size() > 0) { 
            throwMissingElements("test15_newAttributesDirectoryStream", tmp);
        }
    }

    @org.junit.Test
    public void test15_newAttrributesDirectoryStream() throws Exception { 
    
        prepare();

        // test with null
        test15_newAttributesDirectoryStream(null, null, true);
       
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test15_newAttrributesDirectoryStream");
        
        // test with empty dir 
        test15_newAttributesDirectoryStream(testDir, null, false);               
        
        // test with non-existing dir
        AbsolutePath dir0 = createNewTestDirName(testDir);
        test15_newAttributesDirectoryStream(dir0, null, true);
        
        // test with exising file
        AbsolutePath file0 = createTestFile(testDir, null);
        test15_newAttributesDirectoryStream(file0, null, true);
        
        // test with non-empty dir
        AbsolutePath file1 = createTestFile(testDir, null);
        AbsolutePath file2 = createTestFile(testDir, null);
        AbsolutePath file3 = createTestFile(testDir, null);
        
        Set<PathAttributesPair> result = new HashSet<PathAttributesPair>();
        result.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        result.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        result.add(new PathAttributesPairImplementation(file2, files.getAttributes(file2)));
        result.add(new PathAttributesPairImplementation(file3, files.getAttributes(file3)));
        
        test15_newAttributesDirectoryStream(testDir, result, false);
        
        // test with subdirs 
        AbsolutePath dir1 = createTestDir(testDir);
        AbsolutePath file4 = createTestFile(dir1, null);
         
        result.add(new PathAttributesPairImplementation(dir1, files.getAttributes(dir1)));
        
        test15_newAttributesDirectoryStream(testDir, result, false);
        
        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test15_newAttributesDirectoryStream(testDir, null, true);
        }

        cleanup();
    }
 
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newAttributesDirectoryStream with filter 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs / 
    //              existing file / closed filesystem    
    // 
    // directoryStreams.Filter null / filter returns all / filter returns none / filter selects one. 
    
    // Total combinations : 7 + 8
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories, 
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    public void test16_newAttributesDirectoryStream(AbsolutePath root, DirectoryStream.Filter filter, 
            Set<PathAttributesPair> expected, boolean mustFail) throws Exception { 
        
        Set<PathAttributesPair> tmp = new HashSet<PathAttributesPair>();
        
        if (expected != null) { 
            tmp.addAll(expected);
        }
        
        DirectoryStream<PathAttributesPair> in = null;
        
        try { 
            in = files.newAttributesDirectoryStream(root, filter);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test16_newAttributesDirectoryDirectoryStream_with_filter", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("test16_newAttributesDirectoryDirectoryStream_with_filter");
        }
        
        for (PathAttributesPair p : in) { 
            
            if (tmp.contains(p)) { 
                tmp.remove(p);
            } else { 
                close(in);
                throwUnexpectedElement("test16_newAttributesDirectoryDirectoryStream_with_filter", p.path().getPath());
            }
        }
        
        close(in);
        
        if (tmp.size() > 0) { 
            throwMissingElements("test16_newAttributesDirectoryDirectoryStream_with_filter", tmp);
        }
    }
    
    @org.junit.Test
    public void test15_newAttributesDirectoryStream_with_filter() throws Exception { 
        
        prepare();
        
        // test with null 
        test16_newAttributesDirectoryStream(null, null, null, true);
       
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test15_newAttributesDirectoryStream_with_filter");
        
        // test with empty dir + null filter 
        test16_newAttributesDirectoryStream(testDir, null, null, true);
        
        // test with empty dir + true filter 
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), null, false);
        
        // test with empty dir + false filter 
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), null, false);
        
        // test with non-existing dir
        AbsolutePath dir0 = createNewTestDirName(testDir);
        test16_newAttributesDirectoryStream(dir0, new AllTrue(), null, true);
        
        // test with existing file
        AbsolutePath file0 = createTestFile(testDir, null);
        test16_newAttributesDirectoryStream(file0, new AllTrue(), null, true);
                
        // test with non-empty dir and allTrue
        AbsolutePath file1 = createTestFile(testDir, null);
        AbsolutePath file2 = createTestFile(testDir, null);
        AbsolutePath file3 = createTestFile(testDir, null);
        
        Set<PathAttributesPair> result = new HashSet<PathAttributesPair>();
        result.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        result.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        result.add(new PathAttributesPairImplementation(file2, files.getAttributes(file2)));
        result.add(new PathAttributesPairImplementation(file3, files.getAttributes(file3)));
        
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), result, false);
 
        // test with non-empty dir and allFalse
        test16_newAttributesDirectoryStream(testDir, new AllFalse(), null, false);

        // test with subdirs  
        AbsolutePath dir1 = createTestDir(testDir);
        AbsolutePath file4 = createTestFile(dir1, null);
        
        result.add(new PathAttributesPairImplementation(dir1, files.getAttributes(dir1)));        
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), result, false);
        
        // test with non-empty dir and select        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        
        result = new HashSet<PathAttributesPair>();
        result.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        result.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        result.add(new PathAttributesPairImplementation(file2, files.getAttributes(file2)));
        
        test16_newAttributesDirectoryStream(testDir, new Select(tmp), result, false);
        
        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test16_newAttributesDirectoryStream(testDir, new AllTrue(), null, true);
        }

        cleanup();
    }

    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newInputStream 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem    
    // 
    // Total combinations : 6
    // 
    // Depends on: 
    
    public void test20_newInputStream(AbsolutePath file, byte [] expected, boolean mustFail) throws Exception { 
        
        InputStream in = null;
        
        try { 
            in = files.newInputStream(file);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test20_newInputStream", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("test20_newInputStream");
        }
        
        byte [] data = readFully(in);
        
        if (expected == null) { 
            if (data.length != 0) {
                throwWrong("test20_newInputStream", "zero bytes", data.length + " bytes");
            }  
            return;
        }
        
        if (expected.length != data.length) { 
            throwWrong("test20_newInputStream", expected.length + " bytes", data.length + " bytes");            
        }
        
        if (!Arrays.equals(expected, data)) { 
            throwWrong("test20_newInputStream", Arrays.toString(expected), Arrays.toString(data));
        }
    }
     
    @org.junit.Test
    public void test20_newInputStream() throws Exception { 

        byte [] data = "Hello World".getBytes();
        
        prepare();

        // test with null
        test20_newInputStream(null, null, true);
        
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test20_newInputStream");
        
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(testDir);
        test20_newInputStream(file0, null, true);
        
        // test with existing empty file
        AbsolutePath file1 = createTestFile(testDir, null);
        test20_newInputStream(file1, null, false);
        
        // test with existing non-empty file
        AbsolutePath file2 = createTestFile(testDir, data);
        test20_newInputStream(file2, data, false);

        // test with existing dir 
        AbsolutePath dir0 = createTestDir(testDir);
        test20_newInputStream(dir0, null, true);

        // cleanup
        deleteTestFile(file1);
        deleteTestFile(file2);
        deleteTestDir(dir0);
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test20_newInputStream(file2, data, true);
        }

        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newOuputStream 
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    // OpenOption null / CREATE / OPEN / OPEN_OR_CREATE / READ / TRUNCATE / READ / WRITE + combinations
    // 
    // Total combinations : N
    // 
    // Depends on: 

    public void test21_newOutputStream(AbsolutePath path, OpenOption [] options, byte [] data, byte [] expected, boolean mustFail) throws Exception {

        OutputStream in = null;
        
        try { 
            in = files.newOutputStream(path, options);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("test21_newOutputStream", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("test21_newOutputStream");
        }
        
        // TODO: write + check DATA!!!!!
        
    }
    
    @org.junit.Test
    public void test21_newOutputStream() throws Exception { 

        byte [] data = "Hello World".getBytes();
        byte [] data2 = "Hello WorldHello World".getBytes();
        
        prepare();

        // test with null
        test21_newOutputStream(null, null, null, null, true);
        
        FileSystem fs = getTestFileSystem();
        prepareTestDir(fs, "test21_newOuputStream");
        
        // test with existing file and null options
        AbsolutePath file0 = createTestFile(testDir, null);
        test21_newOutputStream(file0, null, null, null, true);
        
        // test with existing file and empty options
        test21_newOutputStream(file0, new OpenOption[0],  null, null, true);

        // test with existing file and CREATE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.CREATE }, null, null, true);

        // test with existing file and OPEN option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN }, null, null, true);

        // test with existing file and OPEN_OR_CREATE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN_OR_CREATE }, null, null, true);

        // test with existing file and APPEND option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.APPEND }, null, null, true);

        // test with existing file and TRUNCATE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.TRUNCATE }, null, null, true);
        
        // test with existing file and READ option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.READ }, null, null, true);
        
        // test with existing file and WRITE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.WRITE }, null, null, true);
        
        // test with existing file and CREATE + APPEND option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.CREATE, OpenOption.APPEND }, null, null, true);

        // test with existing file and CREATE + APPEND + READ option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.CREATE, OpenOption.APPEND, OpenOption.READ }, null, null, true);
        
        // test with existing file and OPEN_OR_CREATE + APPEND option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, data, data, false);
        
        // test with existing file and OPEN + APPEND option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN, OpenOption.APPEND }, data, data2, false);

        // test with existing file and OPEN_OR_CREATE + APPEND + WRITE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN, OpenOption.TRUNCATE, OpenOption.WRITE }, data, data, false);
        
        // test with existing file and CREATE + TRUNCATE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.CREATE, OpenOption.TRUNCATE }, null, null, true);

        // test with existing file and OPEN_OR_CREATE + TRUNCATE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.TRUNCATE }, data, data, false);

        // test with existing file and OPEN + TRUNCATE option
        test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN, OpenOption.TRUNCATE }, data, data, false);
        
        deleteTestFile(file0);
         
        // test with non-existing and CREATE + APPEND option
        AbsolutePath file1 = createNewTestFileName(testDir);
        test21_newOutputStream(file1, new OpenOption [] { OpenOption.CREATE, OpenOption.APPEND }, data, data, false);
        deleteTestFile(file1);
        
        // test with non-existing and OPEN_OR_CREATE + APPEND option
        AbsolutePath file2 = createNewTestFileName(testDir);
        test21_newOutputStream(file2, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, data, data, false);
        deleteTestFile(file2);
        
        // test with non-existing and OPEN + APPEND option
        AbsolutePath file3 = createNewTestFileName(testDir);
        test21_newOutputStream(file2, new OpenOption [] { OpenOption.OPEN, OpenOption.APPEND }, null, null, true);
        deleteTestFile(file3);
        
        // test with exising dir
        AbsolutePath dir0 = createTestDir(testDir);
        
        test21_newOutputStream(dir0, new OpenOption [] { OpenOption.CREATE, OpenOption.APPEND }, null, null, true);
        test21_newOutputStream(dir0, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, null, null, true);
        test21_newOutputStream(dir0, new OpenOption [] { OpenOption.OPEN, OpenOption.APPEND }, null, null, true);

        deleteTestDir(dir0);
        
        // test with conflicting options
        AbsolutePath file4 = createTestFile(testDir, null);

        test21_newOutputStream(file4, new OpenOption [] { OpenOption.CREATE, OpenOption.OPEN, OpenOption.APPEND }, null, null, true);
        test21_newOutputStream(file4, new OpenOption [] { OpenOption.OPEN, OpenOption.TRUNCATE, OpenOption.APPEND }, null, null, true);
        test21_newOutputStream(file4, new OpenOption [] { OpenOption.OPEN, OpenOption.APPEND, OpenOption.READ }, null, null, true);

        deleteTestFile(file4);
 
        // test with non-existing and CREATE option
        AbsolutePath file5 = createNewTestFileName(testDir);
        test21_newOutputStream(file5, new OpenOption [] { OpenOption.CREATE }, null, null, false);
        
        
        deleteTestDir(testDir);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test21_newOutputStream(file0, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, null, null, true);             
        }

        cleanup();
    }
    
    
    
    /*    
     
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException;
 


    
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException;

    public boolean isSymbolicLink(AbsolutePath path) throws OctopusIOException;



    




    public Copy copy(AbsolutePath source, AbsolutePath target, CopyOption... options) 
            throws UnsupportedOperationException, OctopusIOException;

    public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException;
    
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException, OctopusIOException;
    
    public CopyStatus cancelCopy(Copy copy) throws OctopusException, OctopusIOException;

    
*/
    
    
}
