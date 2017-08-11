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

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.Path;

public class MockSftpFileSystem extends SftpFileSystem {

    public MockSftpFileSystem() {
        super("ID", "TEST", "localhost", new Path("/home/xenon"), new MockSftpClient(), null);
    }

    protected void assertNotNull(Path path) {
    }

    protected void assertPathExists(Path path) throws XenonException {
    }

    protected void assertPathNotExists(Path path) throws XenonException {
    }

    protected void assertPathIsNotDirectory(Path path) throws XenonException {
    }

    protected void assertPathIsFile(Path path) throws XenonException {
    }

    protected void assertPathIsDirectory(Path path) throws XenonException {
    }

    protected void assertFileExists(Path file) throws XenonException {
    }

    protected void assertDirectoryExists(Path dir) throws XenonException {
    }

    protected void assertParentDirectoryExists(Path path) throws XenonException {
    }

    protected void assertFileIsSymbolicLink(Path link) throws XenonException {
    }

    protected void assertIsOpen() throws XenonException {
    }

}
