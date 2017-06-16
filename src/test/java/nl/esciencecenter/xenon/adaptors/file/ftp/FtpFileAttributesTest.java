/**
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
package nl.esciencecenter.xenon.adaptors.file.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.file.ftp.FtpFileAttributes;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;

public class FtpFileAttributesTest {
    private final int[] permissionTypes = { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION };
    private final int[] userTypes = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };
    private FtpFileAttributes defaultTestFtpFileAttributes;
    private final GregorianCalendar defaultCalendar = new GregorianCalendar(2010, 4, 1);

    @Test(expected = XenonException.class)
    public void construct_null_throwException() throws XenonException {
        new FtpFileAttributes(null);
    }

    @Test
    public void permissions_none_containsZeroPermissions() throws XenonException {
        // Arrange
        FTPFile testFtpFile = getNewDefaultFTPFile();

        // Act
        FtpFileAttributes ftpFileAttributes = new FtpFileAttributes(testFtpFile);

        // Assert
        assertEquals(0, ftpFileAttributes.permissions().size());
    }

    @Test
    public void permissions_all_contains9Permissions() throws XenonException {
        // Arrange
        FTPFile testFtpFile = getNewDefaultFTPFile();
        for (int permissionType : permissionTypes) {
            for (int userType : userTypes) {
                testFtpFile.setPermission(userType, permissionType, true);
            }
        }

        // Act
        FtpFileAttributes ftpFileAttributes = new FtpFileAttributes(testFtpFile);

        // Assert
        assertEquals(9, ftpFileAttributes.permissions().size());
    }

    @Test
    public void permissions_worldRead_containsOthersRead() throws XenonException {
        // Arrange
        FTPFile testFtpFile = getNewDefaultFTPFile();
        testFtpFile.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, true);

        // Act
        FtpFileAttributes ftpFileAttributes = new FtpFileAttributes(testFtpFile);

        // Assert
        assertEquals(PosixFilePermission.OTHERS_READ, ftpFileAttributes.permissions().toArray()[0]);
    }

    @Test
    public void equals_itself_true() {
        assertTrue(defaultTestFtpFileAttributes.equals(defaultTestFtpFileAttributes));
    }

    @Test
    public void equals_null_false() {
        assertFalse(defaultTestFtpFileAttributes.equals(null));
    }

    @Test
    public void equals_string_false() {
        assertFalse(defaultTestFtpFileAttributes.equals("test"));
    }

    @Test
    public void equals_otherDate_false() throws XenonException {
        // Arrange
        FTPFile otherFtpFile = getNewDefaultFTPFile();
        GregorianCalendar otherDate = (GregorianCalendar) defaultCalendar.clone();
        otherDate.add(GregorianCalendar.MONTH, 2);
        otherFtpFile.setTimestamp(otherDate);
        FtpFileAttributes otherAttributes = new FtpFileAttributes(otherFtpFile);

        // Act & Assert
        assertFalse(defaultTestFtpFileAttributes.equals(otherAttributes));
    }

    @Test
    public void equals_otherWithSameData_true() throws XenonException {
        // Arrange
        FTPFile otherFtpFile = getNewDefaultFTPFile();
        FtpFileAttributes otherAttributes = new FtpFileAttributes(otherFtpFile);

        // Act & Assert
        assertTrue(defaultTestFtpFileAttributes.equals(otherAttributes));
    }

    @Test
    public void equals_otherOwner_false() throws XenonException {
        // Arrange
        FTPFile otherFtpFile = getNewDefaultFTPFile();
        otherFtpFile.setUser("otheruser");
        FtpFileAttributes otherAttributes = new FtpFileAttributes(otherFtpFile);

        // Act & Assert
        assertFalse(defaultTestFtpFileAttributes.equals(otherAttributes));
    }

    @Test
    public void equals_otherGroup_false() throws XenonException {
        // Arrange
        FTPFile otherFtpFile = getNewDefaultFTPFile();
        otherFtpFile.setGroup("othergroup");
        FtpFileAttributes otherAttributes = new FtpFileAttributes(otherFtpFile);

        // Act & Assert
        assertFalse(defaultTestFtpFileAttributes.equals(otherAttributes));
    }

    @Test
    public void equals_otherSize_false() throws XenonException {
        // Arrange
        FTPFile otherFtpFile = getNewDefaultFTPFile();
        otherFtpFile.setSize(1);
        FtpFileAttributes otherAttributes = new FtpFileAttributes(otherFtpFile);

        // Act & Assert
        assertFalse(defaultTestFtpFileAttributes.equals(otherAttributes));
    }

    @Test
    public void equals_otherPermissions_false() throws XenonException {
        // Arrange
        FTPFile otherFtpFile = getNewDefaultFTPFile();
        otherFtpFile.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, true);
        FtpFileAttributes otherAttributes = new FtpFileAttributes(otherFtpFile);

        // Act & Assert
        assertFalse(defaultTestFtpFileAttributes.equals(otherAttributes));
    }

    @Test
    public void getHash_sameAttributes_sameHashes() throws XenonException {
        // Arrange
        FtpFileAttributes otherAttributes = new FtpFileAttributes(getNewDefaultFTPFile());

        // Act & Assert
        assertEquals(defaultTestFtpFileAttributes.hashCode(), otherAttributes.hashCode());
    }

    @Before
    public void setUp() throws XenonException {
        defaultTestFtpFileAttributes = new FtpFileAttributes(getNewDefaultFTPFile());
    }

    private FTPFile getNewDefaultFTPFile() {
        FTPFile defaultFTPFile = new FTPFile();
        defaultFTPFile.setUser("defaultuser");
        defaultFTPFile.setGroup("defaultgroup");
        defaultFTPFile.setTimestamp(defaultCalendar);
        defaultFTPFile.setSize(0);
        return defaultFTPFile;
    }
}
