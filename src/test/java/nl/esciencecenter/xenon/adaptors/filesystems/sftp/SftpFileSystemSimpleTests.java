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
package nl.esciencecenter.xenon.adaptors.filesystems.sftp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;

import org.apache.sshd.common.subsystem.sftp.SftpConstants;
import org.apache.sshd.common.subsystem.sftp.SftpException;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.filesystems.EndOfFileException;
import nl.esciencecenter.xenon.adaptors.filesystems.NoSpaceException;
import nl.esciencecenter.xenon.adaptors.filesystems.PermissionDeniedException;
import nl.esciencecenter.xenon.filesystems.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAlreadyExistsException;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class SftpFileSystemSimpleTests {

    private IOException generateSftpException(int status) {
        return new SftpException(status, "This is a test");
    }

    @Test
    public void test_exception_eof() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_EOF), "");
        assertTrue(e instanceof EndOfFileException);
    }

    @Test
    public void test_exception_noSuchFile() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NO_SUCH_FILE), "");
        assertTrue(e instanceof NoSuchPathException);
    }

    @Test
    public void test_exception_permissionDenied() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_PERMISSION_DENIED), "");
        assertTrue(e instanceof PermissionDeniedException);
    }

    @Test
    public void test_exception_failure() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_FAILURE), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_badMessage() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_BAD_MESSAGE), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_noConnection() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NO_CONNECTION), "");
        assertTrue(e instanceof NotConnectedException);
    }

    @Test
    public void test_exception_connectionLost() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_CONNECTION_LOST), "");
        assertTrue(e instanceof NotConnectedException);
    }

    @Test
    public void test_exception_opUnsupported() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_OP_UNSUPPORTED), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_invalidHandle() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_INVALID_HANDLE), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_noSuchPath() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NO_SUCH_PATH), "");
        assertTrue(e instanceof NoSuchPathException);
    }

    @Test
    public void test_exception_fileAreadyExists() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_FILE_ALREADY_EXISTS), "");
        assertTrue(e instanceof PathAlreadyExistsException);
    }

    @Test
    public void test_exception_writeProtect() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_WRITE_PROTECT), "");
        assertTrue(e instanceof PermissionDeniedException);
    }

    @Test
    public void test_exception_noMedia() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NO_MEDIA), "");
        assertTrue(e instanceof NoSpaceException);
    }

    @Test
    public void test_exception_noSpace() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NO_SPACE_ON_FILESYSTEM), "");
        assertTrue(e instanceof NoSpaceException);
    }

    @Test
    public void test_exception_quotaExceeded() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_QUOTA_EXCEEDED), "");
        assertTrue(e instanceof NoSpaceException);
    }

    @Test
    public void test_exception_unknownPricipal() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_UNKNOWN_PRINCIPAL), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_localConflict() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_LOCK_CONFLICT), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_dirNotEmpty() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_DIR_NOT_EMPTY), "");
        assertTrue(e instanceof DirectoryNotEmptyException);
    }

    @Test
    public void test_exception_notADirectory() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NOT_A_DIRECTORY), "");
        assertTrue(e instanceof InvalidPathException);
    }

    @Test
    public void test_exception_invalidFileName() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_INVALID_FILENAME), "");
        assertTrue(e instanceof InvalidPathException);
    }

    @Test
    public void test_exception_linkLoop() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_LINK_LOOP), "");
        assertTrue(e instanceof InvalidPathException);
    }

    @Test
    public void test_exception_cannotDelete() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_CANNOT_DELETE), "");
        assertTrue(e instanceof PermissionDeniedException);
    }

    @Test
    public void test_exception_invalidParameter() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_INVALID_PARAMETER), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_fileIsADirectory() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_FILE_IS_A_DIRECTORY), "");
        assertTrue(e instanceof InvalidPathException);
    }

    @Test
    public void test_exception_byteRangeLockConflict() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_BYTE_RANGE_LOCK_CONFLICT), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_byteRangeLockRefused() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_BYTE_RANGE_LOCK_REFUSED), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_deletePending() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_DELETE_PENDING), "");
        assertTrue(e instanceof PermissionDeniedException);
    }

    @Test
    public void test_exception_fileCorrupt() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_FILE_CORRUPT), "");
        assertTrue(e instanceof InvalidPathException);
    }

    @Test
    public void test_exception_ownerInvalid() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_OWNER_INVALID), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_groupInvalid() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_GROUP_INVALID), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_noMatchingByteRangeLock() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(SftpConstants.SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_errorCodeOutOfRange() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(generateSftpException(Integer.MAX_VALUE), "");
        assertTrue(e instanceof XenonException);
    }

    @Test
    public void test_exception_checkMessage() {
        XenonException e = SftpFileSystem.sftpExceptionToXenonException(new IOException("aap noot client is close mies"), "");
        assertTrue(e instanceof NotConnectedException);
    }

    @Test(expected = XenonException.class)
    public void test_mock_appendToFile() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.appendToFile(new Path("/home/xenon/file"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_close() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.close();
    }

    @Test(expected = XenonException.class)
    public void test_mock_createDirectory() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.createDirectory(new Path("/home/xenon/file"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_createSymlink() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.createSymbolicLink(new Path("/home/xenon/link"), new Path("/home/xenon/file"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_deleteDirectory() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.deleteDirectory(new Path("/home/xenon/dir"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_deleteFile() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.deleteDirectory(new Path("/home/xenon/file"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_listDirectory() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.listDirectory(new Path("/home/xenon/dir"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_readFromFile() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.readFromFile(new Path("/home/xenon/file"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_readSymbolicLink() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.readSymbolicLink(new Path("/home/xenon/link"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_rename() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.rename(new Path("/home/xenon/file0"), new Path("/home/xenon/file1"));
    }

    @Test(expected = XenonException.class)
    public void test_mock_setPosixPermissions() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.setPosixFilePermissions(new Path("/home/xenon/file"), new HashSet<PosixFilePermission>());
    }

    @Test(expected = XenonException.class)
    public void test_mock_writeToFile() throws XenonException {
        MockSftpFileSystem f = new MockSftpFileSystem();
        f.writeToFile(new Path("/home/xenon/file"));
    }

}
