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

import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.files.AbsolutePath;
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

    // Depends on: AbsolutePath.resolve, RelativePath, exists
    private AbsolutePath createNewTestDirName(AbsolutePath root) throws Exception { 
        
        String uniqueID = UUID.randomUUID().toString();
        
        AbsolutePath dir = root.resolve(new RelativePath(uniqueID));

        if (files.exists(dir)) { 
            throw new Exception("Generated test dir already exists! " + dir.getPath());
        }
        
        return dir;
    }
    
    // Depends on: [createNewTestDirName], FileSystem.getEntryPath, createDirectory, exists
    private AbsolutePath createTestDir(FileSystem fs) throws Exception { 
        
        AbsolutePath dir = createNewTestDirName(fs.getEntryPath());
        
        files.createDirectory(dir);
        
        if (!files.exists(dir)) { 
            throw new Exception("Failed to generate test dir! " + dir.getPath());
        }
        
        return dir;
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
    
    
    /*    

    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException;


    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException;

    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException;

    public void delete(AbsolutePath path) throws OctopusIOException;

    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException;

    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException;

    public boolean isSymbolicLink(AbsolutePath path) throws OctopusIOException;

    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException;


    
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException;

    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException;

    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException;

    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException;



    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException;

    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException;

    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException;




    public Copy copy(AbsolutePath source, AbsolutePath target, CopyOption... options) 
            throws UnsupportedOperationException, OctopusIOException;

    public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException;
    
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException, OctopusIOException;
    
    public CopyStatus cancelCopy(Copy copy) throws OctopusException, OctopusIOException;

    
*/
    
    
}
