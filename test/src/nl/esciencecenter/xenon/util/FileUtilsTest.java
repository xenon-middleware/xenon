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
package nl.esciencecenter.xenon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.InvalidCopyOptionsException;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.Test;

public class FileUtilsTest {

    @Test
    public void testRecursiveCopy_SingleFile_CopiedFile() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
        Files files = mock(Files.class);
        Path srcFile = mock(Path.class);
        Path dstFile = mock(Path.class);

        FileAttributes attributes = mock(FileAttributes.class);
        when(attributes.isDirectory()).thenReturn(false);
        when(files.getAttributes(srcFile)).thenReturn(attributes);
        when(files.getAttributes(dstFile)).thenReturn(attributes);

        //        when(files.isDirectory(srcFile)).thenReturn(false);
        //        when(files.isDirectory(dstFile)).thenReturn(false);
        when(files.exists(srcFile)).thenReturn(true);
        when(files.exists(dstFile)).thenReturn(false);

        Utils.recursiveCopy(files, srcFile, dstFile);

        verify(files).copy(srcFile, dstFile);
    }

    @Test
    public void testRecursiveCopy_SingleDirectory_MkdirTarget() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
        Files files = mock(Files.class);
        Path srcDir = mock(Path.class);
        Path dstDir = mock(Path.class);

        FileAttributes attributes = mock(FileAttributes.class);
        when(attributes.isDirectory()).thenReturn(true);
        when(files.getAttributes(srcDir)).thenReturn(attributes);
        when(files.getAttributes(dstDir)).thenReturn(attributes);

        //        when(files.isDirectory(srcDir)).thenReturn(true);
        //        when(files.isDirectory(dstDir)).thenReturn(true);
        when(files.exists(srcDir)).thenReturn(true);
        when(files.exists(dstDir)).thenReturn(false);
        @SuppressWarnings("unchecked")
        DirectoryStream<Path> listing = mock(DirectoryStream.class);
        @SuppressWarnings("unchecked")
        Iterator<Path> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        when(files.newDirectoryStream(srcDir)).thenReturn(listing);

        Utils.recursiveCopy(files, srcDir, dstDir);

        verify(files).createDirectories(dstDir);
    }

    @Test
    public void testRecursiveCopy_DirectoryWithAFile_MkdirAndCopy() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
// FIXME: 
//        
//        Files files = mock(Files.class);
//        
//        Path srcDir = mock(Path.class); // foo
//        Path srcFile = mock(Path.class); // foo/myfile        
//        when(srcFile.getFileName()).thenReturn("myfile");
//        Path dstDir = mock(Path.class); // bar
//        Path dstFile = mock(Path.class); // bar/myfile        
//        RelativePathn relSrcFile = new RelativePath("myfile");
//        when(dstDir.resolve(relSrcFile)).thenReturn(dstFile);
//
//        FileAttributes attributes = mock(FileAttributes.class);
//        when(attributes.isDirectory()).thenReturn(true);
//        when(files.getAttributes(srcDir)).thenReturn(attributes);
//        when(files.getAttributes(dstDir)).thenReturn(attributes);
//
//        FileAttributes attributes2 = mock(FileAttributes.class);
//        when(attributes2.isDirectory()).thenReturn(false);
//        when(files.getAttributes(srcFile)).thenReturn(attributes2);
//        when(files.getAttributes(dstFile)).thenReturn(attributes2);
//
//        //        when(files.isDirectory(srcDir)).thenReturn(true);
//        //        when(files.isDirectory(dstDir)).thenReturn(true);
//        when(files.exists(srcDir)).thenReturn(true);
//        when(files.exists(dstDir)).thenReturn(false);
//        when(files.exists(srcFile)).thenReturn(true);
//        when(files.exists(dstFile)).thenReturn(false);
//        @SuppressWarnings("unchecked")
//        DirectoryStream<Path> listing = mock(DirectoryStream.class);
//        @SuppressWarnings("unchecked")
//        Iterator<Path> iterator = mock(Iterator.class);
//        when(listing.iterator()).thenReturn(iterator);
//        when(iterator.hasNext()).thenReturn(true, false);
//        when(iterator.next()).thenReturn(srcFile);
//        when(files.newDirectoryStream(srcDir)).thenReturn(listing);
//
//        FileUtils.recursiveCopy(files, srcDir, dstDir);
//
//        verify(files).createDirectories(dstDir);
//        verify(files).copy(srcFile, dstFile);
    }

    @Test
    public void testRecursiveCopy_SingleFileExists_FileAlreadyExistsException() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
// FIXME:        
//        
//        Files files = mock(Files.class);
//        Path srcFile = mock(Path.class);
//        Path dstFile = mock(Path.class);
//
//        FileAttributes attributes = mock(FileAttributes.class);
//        when(attributes.isDirectory()).thenReturn(false);
//        when(files.getAttributes(srcFile)).thenReturn(attributes);
//        when(files.getAttributes(dstFile)).thenReturn(attributes);
//
//        //        when(files.isDirectory(srcFile)).thenReturn(false);
//        //        when(files.isDirectory(dstFile)).thenReturn(false);
//        when(files.exists(srcFile)).thenReturn(true);
//        when(files.exists(dstFile)).thenReturn(true);
//        when(dstFile.getPath()).thenReturn("foo");
//        FileSystem dstFs = mock(FileSystem.class);
//        when(dstFile.getFileSystem()).thenReturn(dstFs);
//        when(dstFs.getAdaptorName()).thenReturn("ssh");
//
//        try {
//            FileUtils.recursiveCopy(files, srcFile, dstFile);
//            fail("FileAlreadyExistsException not thrown");
//        } catch (FileAlreadyExistsException e) {
//            assertEquals(e.getMessage(), "ssh adaptor: Target foo already exists!");
//        }
    }

    @Test
    public void testRecursiveCopy_SingleDirectoryExists_FileAlreadyExistsException() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
// FIXME:        
//        
//        
//        Files files = mock(Files.class);
//        Path srcDir = mock(Path.class);
//        Path dstDir = mock(Path.class);
//
//        FileAttributes attributes = mock(FileAttributes.class);
//        when(attributes.isDirectory()).thenReturn(true);
//        when(files.getAttributes(srcDir)).thenReturn(attributes);
//        when(files.getAttributes(dstDir)).thenReturn(attributes);
//
//        //        when(files.isDirectory(srcDir)).thenReturn(true);
//        //        when(files.isDirectory(dstDir)).thenReturn(true);
//        when(files.exists(srcDir)).thenReturn(true);
//        when(files.exists(dstDir)).thenReturn(true);
//        @SuppressWarnings("unchecked")
//        DirectoryStream<Path> listing = mock(DirectoryStream.class);
//        @SuppressWarnings("unchecked")
//        Iterator<Path> iterator = mock(Iterator.class);
//        when(listing.iterator()).thenReturn(iterator);
//        when(iterator.hasNext()).thenReturn(false);
//        when(files.newDirectoryStream(srcDir)).thenReturn(listing);
//        when(dstDir.getPath()).thenReturn("foo");
//        FileSystem dstFs = mock(FileSystem.class);
//        when(dstDir.getFileSystem()).thenReturn(dstFs);
//        when(dstFs.getAdaptorName()).thenReturn("ssh");
//
//        try {
//            FileUtils.recursiveCopy(files, srcDir, dstDir);
//            fail("FileAlreadyExistsException not thrown");
//        } catch (FileAlreadyExistsException e) {
//            assertEquals(e.getMessage(), "ssh adaptor: Target foo already exists!");
//        }
    }

    @Test
    public void testRecursiveCopy_DirectoryWithAFileExists_FileAlreadyExistsException() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
//        Files files = mock(Files.class);
//        Path srcDir = mock(Path.class); // foo
//        Path srcFile = mock(Path.class); // foo/myfile
//        when(srcFile.getFileName()).thenReturn("myfile");
//        Path dstDir = mock(Path.class); // bar
//        Path dstFile = mock(Path.class); // bar/myfile
//        RelativePath relSrcFile = new RelativePath("myfile");
//        when(dstDir.resolve(relSrcFile)).thenReturn(dstFile);
//
//        FileAttributes attributes = mock(FileAttributes.class);
//        when(attributes.isDirectory()).thenReturn(true);
//        when(files.getAttributes(srcDir)).thenReturn(attributes);
//        when(files.getAttributes(dstDir)).thenReturn(attributes);
//
//        FileAttributes attributes2 = mock(FileAttributes.class);
//        when(attributes2.isDirectory()).thenReturn(true);
//        when(files.getAttributes(srcFile)).thenReturn(attributes2);
//        when(files.getAttributes(dstFile)).thenReturn(attributes2);
//
//        //        when(files.isDirectory(srcDir)).thenReturn(true);
//        //        when(files.isDirectory(dstDir)).thenReturn(true);
//        when(files.exists(srcDir)).thenReturn(true);
//        when(files.exists(dstDir)).thenReturn(false);
//        when(files.exists(srcFile)).thenReturn(true);
//        when(files.exists(dstFile)).thenReturn(true);
//        @SuppressWarnings("unchecked")
//        DirectoryStream<Path> listing = mock(DirectoryStream.class);
//        @SuppressWarnings("unchecked")
//        Iterator<Path> iterator = mock(Iterator.class);
//        when(listing.iterator()).thenReturn(iterator);
//        when(iterator.hasNext()).thenReturn(true, false);
//        when(iterator.next()).thenReturn(srcFile);
//        when(files.newDirectoryStream(srcDir)).thenReturn(listing);
//        when(dstFile.getPath()).thenReturn("myfile");
//        FileSystem dstFs = mock(FileSystem.class);
//        when(dstFile.getFileSystem()).thenReturn(dstFs);
//        when(dstFs.getAdaptorName()).thenReturn("ssh");
//
//        try {
//            FileUtils.recursiveCopy(files, srcDir, dstDir);
//            verify(files).createDirectories(dstDir);
//            fail("FileAlreadyExistsException not thrown");
//        } catch (FileAlreadyExistsException e) {
//            assertEquals(e.getMessage(), "ssh adaptor: Target myfile already exists!");
//        }
    }

    @Test
    public void recursiveCopy_IgnoreDir_DirNotCopied() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
// FIXME:        
//        
//        Files files = mock(Files.class);
//        Path srcDir = mock(Path.class);
//        Path dstDir = mock(Path.class);
//
//        FileAttributes attributes = mock(FileAttributes.class);
//        when(attributes.isDirectory()).thenReturn(true);
//        when(files.getAttributes(srcDir)).thenReturn(attributes);
//        when(files.getAttributes(dstDir)).thenReturn(attributes);
//
//        //        when(files.isDirectory(srcDir)).thenReturn(true);
//        //        when(files.isDirectory(dstDir)).thenReturn(true);
//        when(files.exists(srcDir)).thenReturn(true);
//        when(files.exists(dstDir)).thenReturn(true);
//        when(dstDir.getPath()).thenReturn("foo");
//        FileSystem dstFs = mock(FileSystem.class);
//        when(dstDir.getFileSystem()).thenReturn(dstFs);
//        when(dstFs.getAdaptorName()).thenReturn("ssh");
//        @SuppressWarnings("unchecked")
//        DirectoryStream<Path> listing = mock(DirectoryStream.class);
//        @SuppressWarnings("unchecked")
//        Iterator<Path> iterator = mock(Iterator.class);
//        when(listing.iterator()).thenReturn(iterator);
//        when(iterator.hasNext()).thenReturn(false);
//        when(files.newDirectoryStream(srcDir)).thenReturn(listing);
//
//        FileUtils.recursiveCopy(files, srcDir, dstDir, CopyOption.IGNORE);
//
//        verify(files, never()).copy(srcDir, dstDir, CopyOption.IGNORE);
    }

    @Test
    public void recursiveCopy_IgnoreFile_FileNotCopied() throws XenonException,
            nl.esciencecenter.xenon.files.InvalidCopyOptionsException {
// FIXME:        
//        
//        Files files = mock(Files.class);
//        Path srcFile = mock(Path.class);
//        Path dstFile = mock(Path.class);
//
//        FileAttributes attributes = mock(FileAttributes.class);
//        when(attributes.isDirectory()).thenReturn(false);
//        when(files.getAttributes(srcFile)).thenReturn(attributes);
//        when(files.getAttributes(dstFile)).thenReturn(attributes);
//
//        //        when(files.isDirectory(srcFile)).thenReturn(false);
//        //        when(files.isDirectory(dstFile)).thenReturn(false);
//        when(files.exists(srcFile)).thenReturn(true);
//        when(files.exists(dstFile)).thenReturn(true);
//        when(dstFile.getPath()).thenReturn("foo");
//        FileSystem dstFs = mock(FileSystem.class);
//        when(dstFile.getFileSystem()).thenReturn(dstFs);
//        when(dstFs.getAdaptorName()).thenReturn("ssh");
//
//        FileUtils.recursiveCopy(files, srcFile, dstFile, CopyOption.IGNORE);
//
//        verify(files, never()).copy(srcFile, dstFile, CopyOption.IGNORE);
    }

    @Test
    public void recursiveCopy_IgnoreAndReplace_UnsupporterOperationException() throws XenonException {
        Files files = mock(Files.class);
        Path srcDir = mock(Path.class); // foo
        Path dstDir = mock(Path.class); // bar

        try {
            Utils.recursiveCopy(files, srcDir, dstDir, CopyOption.IGNORE, CopyOption.REPLACE);
            fail("UnsupportedOperationException not thrown");
        } catch (InvalidCopyOptionsException e) {
            assertEquals(e.getMessage(), "FileUtils adaptor: Can not replace and ignore existing files at the same time");
        }
    }

    @Test
    public void recursiveCopy_SingleFileReplace_CopyWithReplace() throws XenonException, InvalidCopyOptionsException {
        Files files = mock(Files.class);
        Path srcFile = mock(Path.class);
        Path dstFile = mock(Path.class);

        FileAttributes attributes = mock(FileAttributes.class);
        when(attributes.isDirectory()).thenReturn(false);
        when(files.getAttributes(srcFile)).thenReturn(attributes);
        when(files.getAttributes(dstFile)).thenReturn(attributes);

        //        when(files.isDirectory(srcFile)).thenReturn(false);
        //        when(files.isDirectory(dstFile)).thenReturn(false);
        when(files.exists(srcFile)).thenReturn(true);
        when(files.exists(dstFile)).thenReturn(true);

        Utils.recursiveCopy(files, srcFile, dstFile, CopyOption.REPLACE);

        verify(files).copy(srcFile, dstFile, CopyOption.REPLACE);
    }

    @Test
    public void testRecursiveDelete_SingleDirectory_DeletedDirectory() throws XenonException {
        Files files = mock(Files.class);
        Path directory = mock(Path.class);

        FileAttributes attributes = mock(FileAttributes.class);
        when(attributes.isDirectory()).thenReturn(true);
        when(files.getAttributes(directory)).thenReturn(attributes);

        //        when(files.isDirectory(directory)).thenReturn(true);
        @SuppressWarnings("unchecked")
        DirectoryStream<Path> listing = mock(DirectoryStream.class);
        @SuppressWarnings("unchecked")
        Iterator<Path> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        when(files.newDirectoryStream(directory)).thenReturn(listing);

        Utils.recursiveDelete(files, directory);

        verify(files).delete(directory);
    }

    @Test
    public void testRecursiveDelete_DirectoryWithFile_DeletedDirectoryAndFile() throws XenonException {
        Files files = mock(Files.class);

        Path directory = mock(Path.class);
        Path myfile = mock(Path.class);

        FileAttributes attributes = mock(FileAttributes.class);
        when(attributes.isDirectory()).thenReturn(true);
        when(files.getAttributes(directory)).thenReturn(attributes);

        FileAttributes attributes2 = mock(FileAttributes.class);
        when(attributes2.isDirectory()).thenReturn(false);
        when(files.getAttributes(myfile)).thenReturn(attributes2);

        //        when(files.isDirectory(directory)).thenReturn(true);
        @SuppressWarnings("unchecked")
        DirectoryStream<Path> listing = mock(DirectoryStream.class);
        @SuppressWarnings("unchecked")
        Iterator<Path> iterator = mock(Iterator.class);
        when(listing.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(myfile);
        when(files.newDirectoryStream(directory)).thenReturn(listing);

        Utils.recursiveDelete(files, directory);

        verify(files).delete(directory);
        verify(files).delete(myfile);
    }

    @Test
    public void testRecursiveDelete_SingleFile_DeletedFile() throws XenonException {
        Files files = mock(Files.class);
        Path myfile = mock(Path.class);

        FileAttributes attributes = mock(FileAttributes.class);
        when(attributes.isDirectory()).thenReturn(false);
        when(files.getAttributes(myfile)).thenReturn(attributes);

        //        when(files.isDirectory(myfile)).thenReturn(false);

        Utils.recursiveDelete(files, myfile);

        verify(files).delete(myfile);
    }

    //    @Test
    //    public void testRecursiveWipe() {
    //        fail("Not yet implemented");
    //    }
}
