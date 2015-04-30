package nl.esciencecenter.xenon.adaptors.ftp;

import static org.junit.Assert.assertEquals;

import java.util.GregorianCalendar;

import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;

public class FtpFileAttributesTest {
    private final int[] permissionTypes = { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION };
    private final int[] userTypes = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };
    private FTPFile testFtpFile;

    @Test
    public void permissions_none_containsZeroPermissions() throws AttributeNotSupportedException {
        // Act
        FtpFileAttributes ftpFileAttributes = new FtpFileAttributes(testFtpFile);

        // Assert
        assertEquals(0, ftpFileAttributes.permissions().size());
    }

    @Test
    public void permissions_all_contains9Permissions() throws AttributeNotSupportedException {
        // Arrange
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
    public void permissions_worldRead_containsOthersRead() throws AttributeNotSupportedException {
        // Arrange
        testFtpFile.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, true);

        // Act
        FtpFileAttributes ftpFileAttributes = new FtpFileAttributes(testFtpFile);

        // Assert
        assertEquals(PosixFilePermission.OTHERS_READ, ftpFileAttributes.permissions().toArray()[0]);
    }

    @Before
    public void setUp() {
        testFtpFile = new FTPFile();
        testFtpFile.setTimestamp(new GregorianCalendar(2010, 4, 1));
    }
}
