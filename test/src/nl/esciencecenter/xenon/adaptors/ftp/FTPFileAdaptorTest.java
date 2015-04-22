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

package nl.esciencecenter.xenon.adaptors.ftp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.GenericFileAdaptorTestParent;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Christiaan Meijer <C.Meijer@esciencecenter.nl>
 *
 */
public class FTPFileAdaptorTest extends GenericFileAdaptorTestParent {

    private String testDirectoryPath = "test123";
    private String testFilePath = "somefile";
    private String testLinkPath = "somelink";

    @BeforeClass
    public static void prepareFTPFileAdaptorTest() throws Exception {
        GenericFileAdaptorTestParent.prepareClass(new FTPFileTestConfig(null));
    }

    @AfterClass
    public static void cleanupFTPFileAdaptorTest() throws Exception {
        GenericFileAdaptorTestParent.cleanupClass();
    }

    @Test
    public void exists_existingDirectory_true() throws Exception {
        assertTrue(ftpClientCallExists("test123"));
    }

    @Test
    public void exists_existingFile_true() throws Exception {
        assertTrue(ftpClientCallExists("somefile"));
    }

    @Test
    public void exists_nonExistingPath_false() throws Exception {
        assertFalse(ftpClientCallExists("nosuchfile48566393"));
    }

    private boolean ftpClientCallExists(String relativePath) throws Exception, XenonException {
        prepare();
        Path root = config.getWorkingDir(files, credentials);
        Path path = resolve(root, relativePath);
        boolean exists = files.exists(path);
        cleanup();
        return exists;
    }

    @Test
    public void getAttributes_ofFile_isFile() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testFilePath);
        assertTrue(attributes.isRegularFile());
    }

    @Test
    public void getAttributes_ofFile_isNotDirectory() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testFilePath);
        assertFalse(attributes.isDirectory());
    }

    @Test
    public void getAttributes_ofFile_isNotLink() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testFilePath);
        assertFalse(attributes.isSymbolicLink());
    }

    @Test
    public void getAttributes_ofDirectory_isDirectory() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testDirectoryPath);
        assertTrue(attributes.isDirectory());
    }

    @Test
    public void getAttributes_ofDirectory_isNotFile() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testDirectoryPath);
        assertFalse(attributes.isRegularFile());
    }

    @Test
    public void getAttributes_ofDirectory_isNotLink() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testDirectoryPath);
        assertFalse(attributes.isSymbolicLink());
    }

    @Test(expected = XenonException.class)
    public void getAttributes_ofRoot_isDirectory() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes("/");
        assertTrue(attributes.isDirectory());
    }

    @Test
    public void getAttributes_ofLink_isNotDirectory() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testLinkPath);
        assertFalse(attributes.isDirectory());
    }

    @Test
    public void getAttributes_ofLink_isNotFile() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testLinkPath);
        assertFalse(attributes.isRegularFile());
    }

    @Test
    public void getAttributes_ofLink_isLink() throws Exception {
        FileAttributes attributes = ftpClientGetAttributes(testLinkPath);
        assertTrue(attributes.isSymbolicLink());
    }

    private FileAttributes ftpClientGetAttributes(String relativePath) throws Exception {
        prepare();
        Path root = config.getWorkingDir(files, credentials);
        Path path = resolve(root, relativePath);
        FileAttributes attributes = files.getAttributes(path);
        cleanup();
        return attributes;
    }

    @Test
    public void createAndDeleteDir_doesNotExist() throws Exception {
        prepare();
        String relativePath = "newlycreated";
        Path root = config.getWorkingDir(files, credentials);
        Path path = resolve(root, relativePath);
        files.createDirectory(path);
        files.delete(path);
        boolean exists = files.exists(path);
        cleanup();
        assertFalse(exists);
    }

    @Test
    public void createDirs_1dir() throws Exception {
        // Arrange
        prepare();
        String nestedDir = "nesteddir";
        String parentDir = "parentdir";
        String relativePath = Paths.get(parentDir).resolve(nestedDir).toString();
        Path root = config.getWorkingDir(files, credentials);
        Path nestedPath = resolve(root, relativePath);

        // Act
        files.createDirectories(nestedPath);
        boolean exists = files.exists(nestedPath);

        // Cleanup
        Path parentPath = resolve(root, parentDir);
        files.delete(nestedPath);
        files.delete(parentPath);
        cleanup();

        // Assert
        assertTrue(exists);
    }

    @Test
    public void newDirectoryStream_root_returnList() throws Exception {
        prepare();
        Path dir = config.getWorkingDir(files, credentials);
        DirectoryStream<Path> newDirectoryStream = files.newDirectoryStream(dir);
        assertTrue(newDirectoryStream != null);
        cleanup();
    }
}
