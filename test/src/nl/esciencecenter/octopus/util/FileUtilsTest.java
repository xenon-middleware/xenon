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
package nl.esciencecenter.octopus.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;

import org.junit.Test;

public class FileUtilsTest {

    @Test
    public void testCopyOctopusInputStreamAbsolutePathCopyOptionArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testCopyOctopusAbsolutePathOutputStream() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewBufferedReader() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewBufferedWriter() {
        fail("Not yet implemented");
    }

    @Test
    public void testReadAllBytes() {
        fail("Not yet implemented");
    }

    @Test
    public void testReadAllLines() {
        fail("Not yet implemented");
    }

    @Test
    public void testWriteOctopusAbsolutePathByteArrayOpenOptionArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testWriteOctopusAbsolutePathIterableOfQextendsCharSequenceCharsetOpenOptionArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testWalkFileTreeOctopusAbsolutePathFileVisitor() {
        fail("Not yet implemented");
    }

    @Test
    public void testWalkFileTreeOctopusAbsolutePathBooleanIntFileVisitor() {
        fail("Not yet implemented");
    }

    @Test
    public void testRecursiveCopy_SingleFile_CopiedFile() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath srcFile = mock(AbsolutePath.class);
        AbsolutePath dstFile = mock(AbsolutePath.class);
        when(files.isDirectory(srcFile)).thenReturn(false);
        when(files.isDirectory(dstFile)).thenReturn(false);
        when(files.exists(srcFile)).thenReturn(true);
        when(files.exists(dstFile)).thenReturn(false);

        FileUtils.recursiveCopy(octopus, srcFile, dstFile);

        verify(files).copy(srcFile, dstFile);
    }

    @Test
    public void testRecursiveCopy_SingleDirectory_MkdirTarget() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath srcDir = mock(AbsolutePath.class);
        AbsolutePath dstDir = mock(AbsolutePath.class);
        when(files.isDirectory(srcDir)).thenReturn(true);
        when(files.isDirectory(dstDir)).thenReturn(true);
        when(files.exists(srcDir)).thenReturn(true);
        when(files.exists(dstDir)).thenReturn(false);
        DirectoryStream<AbsolutePath> listing = mock(DirectoryStream.class);
        Iterator<AbsolutePath> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        when(files.newDirectoryStream(srcDir)).thenReturn(listing);

        FileUtils.recursiveCopy(octopus, srcDir, dstDir);

        verify(files).createDirectories(dstDir);
    }

    @Test
    public void testRecursiveCopy_DirectoryWithAFile_MkdirAndCopy() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath srcDir = mock(AbsolutePath.class); // foo
        AbsolutePath srcFile = mock(AbsolutePath.class); // foo/myfile
        when(srcFile.getFileName()).thenReturn("myfile");
        AbsolutePath dstDir = mock(AbsolutePath.class); // bar
        AbsolutePath dstFile = mock(AbsolutePath.class); // bar/myfile
        RelativePath relSrcFile = new RelativePath("myfile");
        when(dstDir.resolve(relSrcFile)).thenReturn(dstFile);
        when(files.isDirectory(srcDir)).thenReturn(true);
        when(files.isDirectory(dstDir)).thenReturn(true);
        when(files.exists(srcDir)).thenReturn(true);
        when(files.exists(dstDir)).thenReturn(false);
        when(files.exists(srcFile)).thenReturn(true);
        when(files.exists(dstFile)).thenReturn(false);
        DirectoryStream<AbsolutePath> listing = mock(DirectoryStream.class);
        Iterator<AbsolutePath> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(srcFile);
        when(files.newDirectoryStream(srcDir)).thenReturn(listing);

        FileUtils.recursiveCopy(octopus, srcDir, dstDir);

        verify(files).createDirectories(dstDir);
        verify(files).copy(srcFile, dstFile);
    }

    @Test
    public void testRecursiveCopy_SingleFileExists_FileAlreadyExistsException() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath srcFile = mock(AbsolutePath.class);
        AbsolutePath dstFile = mock(AbsolutePath.class);
        when(files.isDirectory(srcFile)).thenReturn(false);
        when(files.isDirectory(dstFile)).thenReturn(false);
        when(files.exists(srcFile)).thenReturn(true);
        when(files.exists(dstFile)).thenReturn(true);
        when(dstFile.getPath()).thenReturn("foo");
        FileSystem dstFs = mock(FileSystem.class);
        when(dstFile.getFileSystem()).thenReturn(dstFs);
        when(dstFs.getAdaptorName()).thenReturn("ssh");

        try {
            FileUtils.recursiveCopy(octopus, srcFile, dstFile);
            fail("FileAlreadyExistsException not thrown");
        } catch (FileAlreadyExistsException e) {
            assertThat(e.getMessage(), is("ssh adaptor: Target foo already exists!"));
        }
    }

    @Test
    public void testRecursiveCopy_SingleDirectoryExists_FileAlreadyExistsException() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath srcDir = mock(AbsolutePath.class);
        AbsolutePath dstDir = mock(AbsolutePath.class);
        when(files.isDirectory(srcDir)).thenReturn(true);
        when(files.isDirectory(dstDir)).thenReturn(true);
        when(files.exists(srcDir)).thenReturn(true);
        when(files.exists(dstDir)).thenReturn(true);
        DirectoryStream<AbsolutePath> listing = mock(DirectoryStream.class);
        Iterator<AbsolutePath> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        when(files.newDirectoryStream(srcDir)).thenReturn(listing);
        when(dstDir.getPath()).thenReturn("foo");
        FileSystem dstFs = mock(FileSystem.class);
        when(dstDir.getFileSystem()).thenReturn(dstFs);
        when(dstFs.getAdaptorName()).thenReturn("ssh");

        try {
            FileUtils.recursiveCopy(octopus, srcDir, dstDir);
            fail("FileAlreadyExistsException not thrown");
        } catch (FileAlreadyExistsException e) {
            assertThat(e.getMessage(), is("ssh adaptor: Target foo already exists!"));
        }
    }

    @Test
    public void testRecursiveCopy_DirectoryWithAFileExists_FileAlreadyExistsException() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath srcDir = mock(AbsolutePath.class); // foo
        AbsolutePath srcFile = mock(AbsolutePath.class); // foo/myfile
        when(srcFile.getFileName()).thenReturn("myfile");
        AbsolutePath dstDir = mock(AbsolutePath.class); // bar
        AbsolutePath dstFile = mock(AbsolutePath.class); // bar/myfile
        RelativePath relSrcFile = new RelativePath("myfile");
        when(dstDir.resolve(relSrcFile)).thenReturn(dstFile);
        when(files.isDirectory(srcDir)).thenReturn(true);
        when(files.isDirectory(dstDir)).thenReturn(true);
        when(files.exists(srcDir)).thenReturn(true);
        when(files.exists(dstDir)).thenReturn(false);
        when(files.exists(srcFile)).thenReturn(true);
        when(files.exists(dstFile)).thenReturn(true);
        DirectoryStream<AbsolutePath> listing = mock(DirectoryStream.class);
        Iterator<AbsolutePath> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(srcFile);
        when(files.newDirectoryStream(srcDir)).thenReturn(listing);
        when(dstFile.getPath()).thenReturn("myfile");
        FileSystem dstFs = mock(FileSystem.class);
        when(dstFile.getFileSystem()).thenReturn(dstFs);
        when(dstFs.getAdaptorName()).thenReturn("ssh");

        try {
            FileUtils.recursiveCopy(octopus, srcDir, dstDir);
            verify(files).createDirectories(dstDir);
            fail("FileAlreadyExistsException not thrown");
        } catch (FileAlreadyExistsException e) {
            assertThat(e.getMessage(), is("ssh adaptor: Target myfile already exists!"));
        }
    }

    @Test
    public void testRecursiveDelete_SingleDirectory_DeletedDirectory() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath directory = mock(AbsolutePath.class);
        when(files.isDirectory(directory)).thenReturn(true);
        DirectoryStream<AbsolutePath> listing = mock(DirectoryStream.class);
        Iterator<AbsolutePath> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        when(files.newDirectoryStream(directory)).thenReturn(listing);

        FileUtils.recursiveDelete(octopus, directory);

        verify(files).delete(directory);
    }

    @Test
    public void testRecursiveDelete_DirectoryWithFile_DeletedDirectoryAndFile() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath directory = mock(AbsolutePath.class);
        AbsolutePath myfile = mock(AbsolutePath.class);
        when(files.isDirectory(directory)).thenReturn(true);
        DirectoryStream<AbsolutePath> listing = mock(DirectoryStream.class);
        Iterator<AbsolutePath> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(myfile);
        when(files.newDirectoryStream(directory)).thenReturn(listing);

        FileUtils.recursiveDelete(octopus, directory);

        verify(files).delete(directory);
        verify(files).delete(myfile);
    }

    @Test
    public void testRecursiveDelete_SingleFile_DeletedFile() throws OctopusIOException {
        Files files = mock(Files.class);
        Octopus octopus = mock(Octopus.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath myfile = mock(AbsolutePath.class);
        when(files.isDirectory(myfile)).thenReturn(false);

        FileUtils.recursiveDelete(octopus, myfile);

        verify(files).delete(myfile);
    }

    @Test
    public void testRecursiveWipe() {
        fail("Not yet implemented");
    }

}
