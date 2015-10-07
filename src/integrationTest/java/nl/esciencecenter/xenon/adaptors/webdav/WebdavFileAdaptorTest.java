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

package nl.esciencecenter.xenon.adaptors.webdav;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.GenericFileAdaptorTestParent;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Christiaan Meijer <C.Meijer@esciencecenter.nl>
 *
 */
@Ignore("Webdav adaptor is still work in process. See webdav feature branch.")
public class WebdavFileAdaptorTest extends GenericFileAdaptorTestParent {

    private static final String NONEXISTENTPARENT_NEWDIR_PATH = "/public/nonexistentparent/newdir";
    private static final String NEWFILE_PATH = "/public/newfile";
    private static final String NEWDIR_PATH = "/public/xenonnewdir";
    private static final String NONEXISTENT_PATH = "/public/nonexistent.txt";
    private static final String DIR_PATH = "/public/sub";
    private static final String FILE_PATH = "/public/bla5.txt";

    @BeforeClass
    public static void prepareFTPFileAdaptorTest() throws Exception {
        GenericFileAdaptorTestParent.prepareClass(new WebdavFileTestConfig(null));
    }

    @AfterClass
    public static void cleanupFTPFileAdaptorTest() throws Exception {
        GenericFileAdaptorTestParent.cleanupClass();
    }

    @Test
    public void getAttributes_dir_noThrow() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(DIR_PATH));
        files.getAttributes(path);
    }

    @Test
    public void getAttributes_dir_isDir() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(DIR_PATH));
        FileAttributes attributes = files.getAttributes(path);
        assertTrue(attributes.isDirectory());
    }

    @Test
    public void getAttributes_file_isFile() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(FILE_PATH));
        FileAttributes attributes = files.getAttributes(path);
        assertTrue(attributes.isRegularFile());
    }

    @Test
    public void getAttributes_file_noThrow() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(FILE_PATH));
        files.getAttributes(path);
    }

    @Test(expected = XenonException.class)
    public void getAttributes_nonExistent_throw() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NONEXISTENT_PATH));
        files.getAttributes(path);
    }

    @Test
    public void exists_nonExistent_returnFalse() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NONEXISTENT_PATH));
        assertFalse(files.exists(path));
    }

    @Test
    public void exists_filePath_returnTrue() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(FILE_PATH));
        assertTrue(files.exists(path));
    }

    @Test
    public void exists_dirPath_returnTrue() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(DIR_PATH));
        assertTrue(files.exists(path));
    }

    @Test
    public void createFile_newFile_fileExists() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NEWFILE_PATH));
        files.createFile(path);
        assertTrue(files.exists(path));
        files.delete(path);
    }

    @Test
    public void delete_newFile_noFileExists() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NEWFILE_PATH));
        files.createFile(path);
        files.delete(path);
        assertFalse(files.exists(path));
    }

    @Test
    public void createDir_newDir_fileExists() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NEWDIR_PATH));
        files.createDirectory(path);
        assertTrue(files.exists(path));
        files.delete(path);
    }

    @Test
    public void delete_newDir_noDirectoryExists() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NEWDIR_PATH));
        files.createDirectory(path);
        files.delete(path);
        assertFalse(files.exists(path));
    }

    @Test(expected = XenonException.class)
    public void delete_nonExistent_throw() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NONEXISTENT_PATH));
        files.delete(path);
    }

    @Test
    public void createDir_newDirExistingParent_fileExists() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NEWDIR_PATH));
        files.createDirectories(path);
        assertTrue(files.exists(path));
        files.delete(path);
    }

    @Test
    public void createDir_newDirNonExistingParent_fileExists() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        Path path = files.newPath(fs, new RelativePath(NONEXISTENTPARENT_NEWDIR_PATH));
        files.createDirectories(path);
        assertTrue(files.exists(path));
        files.delete(path);
    }
}
