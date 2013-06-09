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
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public abstract class AbstractFileTest {

    protected Octopus octopus;
    protected Files files;
   
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
        
        String uniqueID = UUID.randomUUID().toString();
        
        AbsolutePath dir = root.resolve(new RelativePath(uniqueID));

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
    private AbsolutePath createTestDir(FileSystem fs) throws Exception { 
        return createTestDir(fs.getEntryPath());
    }

    // Depends on: AbsolutePath.resolve, RelativePath, exists 
    private AbsolutePath createNewTestFileName(AbsolutePath root) throws Exception { 
        
        AbsolutePath file = root.resolve(new RelativePath("test" + counter));
        counter++;

        if (files.exists(file)) { 
            throw new Exception("Generated NEW test file already exists! " + file.getPath());
        }

        return file;
    }
 
    // Depends on: newOutputStream
    private void writeData(AbsolutePath testFile, byte [] data) throws Exception { 
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.WRITE);
        if (data != null) { 
            out.write(data);
        }
        out.close();        
    }
    
    // Depends on: [createNewTestFileName], createFile, [writeData]
    private AbsolutePath createTestFile(AbsolutePath root, byte [] data) throws Exception { 
        
        AbsolutePath file = createNewTestFileName(root);
        
        files.createFile(file);
        writeData(file, data);

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
    
    private void test_newFileSystem(URI uri, Credential c, Properties p, boolean mustFail) throws Exception {
        
        try { 
            FileSystem fs = files.newFileSystem(uri, c, p);
            files.close(fs);
        } catch (Exception e) { 
            if (mustFail) { 
                // exception was expected.
                return;
            } 

            // exception was not expected
            throwUnexpected("test_newFileSystem", e);
        }
        
        if (mustFail) {
            // expected an exception!
            throwExpected("test_newFileSystem");
        }
    } 
    
    @org.junit.Test
    public void test_newFileSystem() throws Exception { 
        
        prepare();
        
        // test with correct URI with default credential and without properties
        test_newFileSystem(getCorrectURI(), getDefaultCredential(), null, false);
 
        // test with wrong URI user with default credential and without properties
        if (supportURIUser()) { 
            test_newFileSystem(getURIWrongUser(), getDefaultCredential(), null, true);
        }
        
        // test with wrong URI location with default credential and without properties
        if (supportURILocation()) { 
            test_newFileSystem(getURIWrongLocation(), getDefaultCredential(), null, true);
        }
        
        // test with wrong URI path with default credential and without properties
        test_newFileSystem(getURIWrongPath(), getDefaultCredential(), null, true);
        
        // test with correct URI without credential and without properties
        boolean allowNull = supportNullCredential();
        test_newFileSystem(getCorrectURI(), null, null, !allowNull);
       
        // test with correct URI with non-default credential and without properties
        if (supportNonDefaultCredential()) { 
            test_newFileSystem(getCorrectURI(), getNonDefaultCredential(), null, false);
        }
        
        // test with correct URI with default credential and with empty properties
        test_newFileSystem(getCorrectURI(), getDefaultCredential(), new Properties(), false);
    
        // test with correct URI with default credential and with correct properties
        if (supportProperties()) { 
            test_newFileSystem(getCorrectURI(), getDefaultCredential(), getCorrectProperties(), false);
        
            // test with correct URI with default credential and with wrong properties
            test_newFileSystem(getCorrectURI(), getDefaultCredential(), getIncorrectProperties(), true);
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
 
    private void test_newPath(FileSystem fs, RelativePath path, String expected, boolean mustFail) throws Exception { 
        
        String result = null;
        
        try { 
            result = files.newPath(fs, path).getPath();
        } catch (Exception e) { 
            if (mustFail) { 
                // expected exception
                return;
            } 
            
            throwUnexpected("test_newPath", e);
        }
        
        if (mustFail) { 
            throwExpected("test_newPath");
        }
        
        if (!result.equals(expected)) {
            throwWrong("test_newPath", expected, result);
        }
    }
    
    @org.junit.Test
    public void test_newPath() throws Exception { 
        
        prepare();
        
        FileSystem fs = getTestFileSystem();
        String root = "/";
        
        // test with null filesystem and null relative path 
        test_newPath(null, null, null, true);
        
        // test with correct filesystem and null relative path 
        test_newPath(fs, null, null, true);
        
        // test with correct filesystem and empty relative path 
        test_newPath(fs, new RelativePath(), root, false);
        
        // test with correct filesystem and relativepath with value
        test_newPath(fs, new RelativePath("test"), root + "test", false);
        
        files.close(fs);
        
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

    private void test_close(FileSystem fs, boolean mustFail) throws Exception { 
        
        try { 
            files.close(fs);
        } catch (Exception e) { 
            if (mustFail) { 
                // expected
                return;
            }
            throwUnexpected("test_close", e);
        }
        
        if (mustFail) { 
            throwExpected("test_close");
        }
    }
    
    @org.junit.Test
    public void test_close() throws Exception { 

        prepare();
        
        // test with null filesystem 
        test_close(null, true);
        
        if (supportsClose()) { 

            FileSystem fs = getTestFileSystem();

            // test with correct open filesystem
            test_close(fs, false);

            // test with correct closed filesystem
            test_close(fs, true);
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

    private void test_isOpen(FileSystem fs, boolean expected, boolean mustFail) throws Exception { 
        
        boolean result = false;
        
        try { 
            result = files.isOpen(fs);
        } catch (Exception e) { 
            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("isOpen", e);
        }
        
        if (mustFail) { 
            throwExpected("isOpen");
        }
        
        if (result != expected) { 
            throwWrong("isOpen", "" + expected, "" + result);
        }
    }
    
    @org.junit.Test
    public void test_isOpen() throws Exception { 

        prepare();
        
        // test with null filesystem 
        test_isOpen(null, false, true);
        
        FileSystem fs = getTestFileSystem();

        // test with correct open filesystem
        test_isOpen(fs, true, false);

        if (supportsClose()) { 
            files.close(fs);
            
            // test with correct closed filesystem
            test_isOpen(fs, false, false);
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
    
    private void test_size(AbsolutePath path, long expected, boolean mustFail) throws Exception { 
        
        long result = -1;
        
        try { 
            result = files.size(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("size", e);
        }
        
        if (mustFail) { 
            throwExpected("size");
        }
        
        if (result != expected) { 
            throwWrong("size", "" + expected, "" + result);
        }
    }
    
    @org.junit.Test
    public void test_size() throws Exception { 
    
        prepare();
        
        // test with null parameter 
        test_size(null, -1, true);
        
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs);
        
        // test with non existing file
        AbsolutePath file1 = createNewTestFileName(root);        
        test_size(file1, -1, true);
        
        // test with existing empty file
        AbsolutePath file2 = createTestFile(root, new byte[0]);
        test_size(file2, 0, false);
        deleteTestFile(file2);        
        
        // test with existing filled file
        AbsolutePath file3 = createTestFile(root, new byte[13]);        
        test_size(file3, 13, false);
        deleteTestFile(file3);        

        // test with closed filesystem
        if (supportsClose()) { 
            closeTestFileSystem(fs);
            test_size(file1, 0, false);               
        }
        
        deleteTestDir(root);
        
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
 
    private void test_exists(AbsolutePath path, boolean expected, boolean mustFail) throws Exception { 
 
        boolean result = false;
        
        try { 
            result = files.exists(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("exists", e);
        }
        
        if (mustFail) { 
            throwExpected("exists");
        }
        
        if (result != expected) { 
            throwWrong("exists", "" + expected, "" + result);
        }
    }
    
    @org.junit.Test
    public void test_exists() throws Exception { 
    
        prepare();
        
        // prepare
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs);
        
        // test with null
        test_exists(null, false, true);
        
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(root);        
        test_exists(file0, false, false);
        
        // test with existing file
        AbsolutePath file1 = createTestFile(root, null);        
        test_exists(file1, true, false);
        deleteTestFile(file1);
        
        // cleanup
        deleteTestDir(root);
        closeTestFileSystem(fs);
        
        cleanup();
    }
    
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
    
    private void test_isDirectory(AbsolutePath path, boolean expected, boolean mustFail) throws Exception { 
        
        boolean result = false;
        
        try { 
            result = files.isDirectory(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("isDirectory", e);
        }
        
        if (mustFail) { 
            throwExpected("isDirectory");
        }
        
        if (result != expected) { 
            throwWrong("isDirectory", "" + expected, "" + result);
        }
    }

    @org.junit.Test
    public void test_isDirectory() throws Exception { 

        prepare();
        
        // prepare
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs);
    
        // test with null        
        test_isDirectory(null, false, true);
                
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(root);        
        test_isDirectory(file0, false, true);
        
        // test with existing file
        AbsolutePath file1 = createTestFile(root, null);        
        test_isDirectory(file1, false, false);
        deleteTestFile(file1);
        
        // test with existing dir
        test_isDirectory(root, true, false);
        
        // cleanup        
        deleteTestDir(root);
        closeTestFileSystem(fs);      
        
        if (supportsClose()) { 
            // test with closed filesystem
            test_isDirectory(root, true, true);
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

    private void test_createFile(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.createFile(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("createFile", e);
        }
        
        if (mustFail) { 
            throwExpected("createFile");
        }
    }
    
    @org.junit.Test
    public void test_createFile() throws Exception { 

        prepare();
        
        // prepare
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs);
    
        // test with null        
        test_createFile(null, true);
                
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(root);        
        test_createFile(file0, false);
        
        // test with existing file
        test_createFile(file0, true);
        
        // test with existing dir
        test_createFile(root, true);
        
        AbsolutePath tmp = createNewTestDirName(root);
        AbsolutePath file1 = createNewTestFileName(tmp);
        
        // test with non-existing parent
        test_createFile(file1, true);

        // cleanup 
        files.delete(file0);
        deleteTestDir(root);
        closeTestFileSystem(fs);      
        
        if (supportsClose()) { 
            // test with closed filesystem
            test_createFile(file0, true);
        }        
        
        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: delete
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing file / existing file / existing empty dir / existing non-empty dir / closed filesystem    
    // 
    // Total combinations : 6
    // 
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], delete, [deleteTestFile], [deleteTestDir] 
    //             [closeTestFileSystem]
        
    private void test_delete(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.delete(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("delete", e);
        }
        
        if (mustFail) { 
            throwExpected("delete");
        }
    }
    
    @org.junit.Test
    public void test_delete() throws Exception { 

        prepare();

        // test with null
        test_delete(null, true);
        
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs);

        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(root);        
        test_delete(file0, true);

        // test with existing file
        AbsolutePath file1 = createTestFile(root, null);        
        test_delete(file1, false);

        // test with existing empty dir 
        AbsolutePath dir0 = createTestDir(root);        
        test_delete(dir0, false);

        // test with existing non-empty dir
        AbsolutePath dir1 = createTestDir(root);
        AbsolutePath file2 = createTestFile(dir1, null);                
        test_delete(dir1, true);

        // cleanup
        deleteTestFile(file2);
        deleteTestDir(dir1);
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_delete(root, true);
        }

        cleanup();
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createDirectory
    // 
    // Possible parameters:
    //
    // AbsolutePath null / non-existing dir / existing dir / existing file / closed filesystem    
    // 
    // Total combinations : 5
    // 
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], [createTestFile], 
    //             createDirectory, [deleteTestDir], [deleteTestFile], [closeTestFileSystem]

    private void test_createDirectory(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.createDirectory(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("createDirectory", e);
        }
        
        if (mustFail) { 
            throwExpected("createDirectory");
        }
    }
    
    @org.junit.Test
    public void test_createDirectory() throws Exception { 

        prepare();

        // test with null
        test_createDirectory(null, true);
        
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createNewTestDirName(fs.getEntryPath());

        // test with non-existing dir
        test_createDirectory(root, false);

        // test with existing dir
        test_createDirectory(root, true);

        // test with existing file 
        AbsolutePath file0 = createTestFile(root, null);
        test_createDirectory(file0, true);
        deleteTestFile(file0);
        
        // cleanup 
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_createDirectory(root, true);
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

    private void test_createDirectories(AbsolutePath path, boolean mustFail) throws Exception { 
        
        try { 
            files.createDirectories(path);
            
            assert(files.exists(path));
            assert(files.isDirectory(path));
            
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("createDirectory", e);
        }
        
        if (mustFail) { 
            throwExpected("createDirectory");
        }
    }
    
    @org.junit.Test
    public void test_createDirectories() throws Exception { 

        prepare();

        // test with null
        test_createDirectories(null, true);
        
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createNewTestDirName(fs.getEntryPath());

        // test with non-existing dir
        test_createDirectories(root, false);

        // test with existing dir
        test_createDirectories(root, true);

        // dir with existing parents 
        AbsolutePath dir0 = createNewTestDirName(root);
        test_createDirectories(dir0, false);
        deleteTestDir(dir0);
        
        // dir with non-existing parents 
        AbsolutePath dir1 = createNewTestDirName(dir0);
        test_createDirectories(dir1, false);
        
        // dir where last parent is file 
        AbsolutePath file0 = createTestFile(dir0, null);
        AbsolutePath dir2 = createNewTestDirName(file0);
        test_createDirectories(dir2, true);

        // cleanup 
        deleteTestDir(dir1);
        deleteTestFile(file0);        
        deleteTestDir(dir0);
        deleteTestDir(root);

        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_createDirectory(root, true);
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
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories, 
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]
    
    
    public void test_newInputStream(AbsolutePath file, byte [] expected, boolean mustFail) throws Exception { 
        
        InputStream in = null;
        
        try { 
            in = files.newInputStream(file);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("newInputStream", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("newInputStream");
        }
        
        byte [] data = readFully(in);
        
        if (expected == null) { 
            if (data.length != 0) {
                throwWrong("newInputStream", "zero bytes", data.length + " bytes");
            }  
            return;
        }
        
        if (expected.length != data.length) { 
            throwWrong("newInputStream", expected.length + " bytes", data.length + " bytes");            
        }
        
        if (!Arrays.equals(expected, data)) { 
            throwWrong("newInputStream", Arrays.toString(expected), Arrays.toString(data));
        }
    }
     
    @org.junit.Test
    public void test_newInputStream() throws Exception { 

        byte [] data = "Hello World".getBytes();
        
        prepare();

        // test with null
        test_newInputStream(null, null, true);
        
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs.getEntryPath());

        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(root);
        test_newInputStream(file0, null, true);
        
        // test with existing empty file
        AbsolutePath file1 = createTestFile(root, null);
        test_newInputStream(file1, null, false);
        
        // test with existing non-empty file
        AbsolutePath file2 = createTestFile(root, data);
        test_newInputStream(file2, data, false);

        // test with existing dir 
        AbsolutePath dir0 = createTestDir(root);
        test_newInputStream(dir0, null, true);

        // cleanup
        deleteTestFile(file1);
        deleteTestFile(file2);
        deleteTestDir(dir0);
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_newInputStream(file2, data, true);
        }

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

    public void test_newDirectoryStream(AbsolutePath root, Set<AbsolutePath> expected, boolean mustFail) throws Exception { 
        
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
            
            throwUnexpected("newDirectoryStream", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("newDirectoryStream");
        }
        
        for (AbsolutePath p : in) { 
            
            if (tmp.contains(p)) { 
                tmp.remove(p);
            } else { 
                close(in);
                throwUnexpectedElement("newDirectoryStream", p.getPath());
            }
        }
        
        close(in);
        
        if (tmp.size() > 0) { 
            throwMissingElements("newDirectoryStream", tmp);
        }
    }

    @org.junit.Test
    public void test_newDirectoryStream() throws Exception { 
    
        prepare();

        // test with null
        test_newDirectoryStream(null, null, true);
       
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs.getEntryPath());
               
        // test with empty dir 
        test_newDirectoryStream(root, null, false);
         
        // test with non-existing dir
        AbsolutePath dir0 = createNewTestDirName(root);
        test_newDirectoryStream(dir0, null, true);
        
        // test with exising file
        AbsolutePath file0 = createTestFile(root, null);
        test_newDirectoryStream(file0, null, true);
        
        // test with non-empty dir
        AbsolutePath file1 = createTestFile(root, null);
        AbsolutePath file2 = createTestFile(root, null);
        AbsolutePath file3 = createTestFile(root, null);
        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        tmp.add(file3);
        
        test_newDirectoryStream(root, tmp, false);
        
        // test with subdirs 
        AbsolutePath dir1 = createTestDir(root);
        AbsolutePath file4 = createTestFile(dir1, null);
         
        tmp.add(dir1);
        
        test_newDirectoryStream(root, tmp, false);
        
        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_newDirectoryStream(root, null, true);
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

    public void test_newDirectoryStream(AbsolutePath root, DirectoryStream.Filter filter, Set<AbsolutePath> expected, 
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
            
            throwUnexpected("newDirectoryStream_with_filter", e);
        }
        
        if (mustFail) {             
            close(in);            
            throwExpected("newDirectoryStream_with_filter");
        }
        
        for (AbsolutePath p : in) { 
            
            if (tmp.contains(p)) { 
                tmp.remove(p);
            } else { 
                close(in);
                throwUnexpectedElement("newDirectoryStream_with_filter", p.getPath());
            }
        }
        
        close(in);
        
        if (tmp.size() > 0) { 
            throwMissingElements("newDirectoryStream_with_filter", tmp);
        }
    }

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
    
    @org.junit.Test
    public void test_newDirectoryStream_with_filter() throws Exception { 
        
        prepare();
        
        // test with null 
        test_newDirectoryStream(null, null, null, true);
       
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs.getEntryPath());
               
        // test with empty dir + null filter 
        test_newDirectoryStream(root, null, null, true);
        
        // test with empty dir + true filter 
        test_newDirectoryStream(root, new AllTrue(), null, false);
        
        // test with empty dir + false filter 
        test_newDirectoryStream(root, new AllTrue(), null, false);
        
        // test with non-existing dir
        AbsolutePath dir0 = createNewTestDirName(root);
        test_newDirectoryStream(dir0, new AllTrue(), null, true);
        
        // test with existing file
        AbsolutePath file0 = createTestFile(root, null);
        test_newDirectoryStream(file0, new AllTrue(), null, true);
        
        // test with non-empty dir and allTrue
        AbsolutePath file1 = createTestFile(root, null);
        AbsolutePath file2 = createTestFile(root, null);
        AbsolutePath file3 = createTestFile(root, null);
        
        Set<AbsolutePath> tmp = new HashSet<AbsolutePath>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        tmp.add(file3);
        
        test_newDirectoryStream(root, new AllTrue(), tmp, false);
 
        // test with non-empty dir and allFalse
        test_newDirectoryStream(root, new AllFalse(), null, false);
        
        tmp.remove(file3);
        
        // test with non-empty dir and select        
        test_newDirectoryStream(root, new Select(tmp), tmp, false);
        
        // test with subdirs 
        AbsolutePath dir1 = createTestDir(root);
        AbsolutePath file4 = createTestFile(dir1, null);
        
        test_newDirectoryStream(root, new Select(tmp), tmp, false);
        
        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_newDirectoryStream(root, new AllTrue(), null, true);
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
    
    private void test_getAttributes(AbsolutePath path, boolean isDirectory, long size, boolean mustFail) throws Exception { 

        FileAttributes result = null;
        
        try { 
            result = files.getAttributes(path);
        } catch (Exception e) { 

            if (mustFail) { 
                // expected
                return;
            } 
            
            throwUnexpected("getFileAttributes", e);
        }
        
        if (mustFail) {             
            throwExpected("getFileAttributes");
        }
        
        if (result.isDirectory() && !isDirectory) { 
            throwWrong("getfileAttributes", "<not directory>", "<directory>");
        }
        
        if (result.size() != size) { 
            throwWrong("getfileAttributes", "size=" + size, "size=" + result.size());
        }        
    }
    
    @org.junit.Test
    public void test_getAttributes() throws Exception { 
   
        prepare();
        
        // test with null
        test_getAttributes(null, false, -1, true);
        
        FileSystem fs = getTestFileSystem();
        AbsolutePath root = createTestDir(fs.getEntryPath());
        
        // test with non-existing file
        AbsolutePath file0 = createNewTestFileName(root);
        test_getAttributes(file0, false, -1, true);
        
        // test with existing empty file
        AbsolutePath file1 = createTestFile(root, null);
        test_getAttributes(file1, false, 0, false);
        
        // test with existing non-empty file
        AbsolutePath file2 = createTestFile(root, new byte [] { 1, 2, 3 });
        test_getAttributes(file2, false, 3, false);
        
        // test with existing dir 
        AbsolutePath dir0 = createTestDir(root);
        test_getAttributes(dir0, true, 0, false);
        
        // TODO: test with link!

        deleteTestDir(dir0);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestDir(root);
        
        if (supportsClose()) { 
            // test with closed fs
            closeTestFileSystem(fs);
            test_getAttributes(root, false, -1, true);
        }
    }
    
    /*    

 
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException;

    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException;

 
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException;

    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException;


    
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException;
    
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException;


    
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException;

    public boolean isSymbolicLink(AbsolutePath path) throws OctopusIOException;


    




    public Copy copy(AbsolutePath source, AbsolutePath target, CopyOption... options) 
            throws UnsupportedOperationException, OctopusIOException;

    public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException;
    
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException, OctopusIOException;
    
    public CopyStatus cancelCopy(Copy copy) throws OctopusException, OctopusIOException;

    
*/
    
    
}
