package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.apache.commons.net.ftp.FTPFile;

public class FtpFileAttributes implements FileAttributes {
    private static final int[] permissionTypes = { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION };
    private static final int[] userTypes = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };

    private FTPFile attributes;

    public FtpFileAttributes(FTPFile ftpFile) throws XenonException {
        if (ftpFile == null) {
            throw new XenonException(FtpAdaptor.ADAPTOR_NAME, "Cannot create ftp file attributes based on null");
        }
        attributes = ftpFile;
    }

    @Override
    public boolean isDirectory() {
        return attributes.isDirectory();
    }

    @Override
    public boolean isOther() {
        return attributes.isUnknown();
    }

    @Override
    public boolean isRegularFile() {
        return attributes.isFile();
    }

    @Override
    public boolean isSymbolicLink() {
        return attributes.isSymbolicLink();
    }

    @Override
    public long creationTime() {
        return lastModifiedTime();
    }

    @Override
    public long lastAccessTime() {
        return lastModifiedTime();
    }

    @Override
    public long lastModifiedTime() {
        return attributes.getTimestamp().getTimeInMillis();
    }

    @Override
    public long size() {
        return attributes.getSize();
    }

    @Override
    public boolean isExecutable() {
        return getPermissions().contains(PosixFilePermission.OWNER_EXECUTE);
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return getPermissions().contains(PosixFilePermission.OWNER_READ);
    }

    @Override
    public boolean isWritable() {
        return getPermissions().contains(PosixFilePermission.OWNER_WRITE);
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        return attributes.getGroup();
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return attributes.getUser();
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        return getPermissions();
    }

    private HashSet<PosixFilePermission> getPermissions() {
        HashSet<PosixFilePermission> permissions = new HashSet<>();
        for (int userType : userTypes) {
            for (int permissionType : permissionTypes) {
                if (attributes.hasPermission(userType, permissionType)) {
                    permissions.add(getPosixFilePermission(userType, permissionType));
                }
            }
        }
        return permissions;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private static PosixFilePermission getPosixFilePermission(int userType, int permissionType) {
        PosixFilePermission permission = null;
        if (userType == FTPFile.USER_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.OWNER_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.OWNER_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.OWNER_READ;
            }
        }
        if (userType == FTPFile.GROUP_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.GROUP_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.GROUP_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.GROUP_READ;
            }
        }
        if (userType == FTPFile.WORLD_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.OTHERS_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.OTHERS_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.OTHERS_READ;
            }
        }
        return permission;
    }

    /**
     * It was necessary to overwrite hashCode() because equals() is overridden also.
     */
    @Override
    public int hashCode() {
        // Hash code is not designed because it is not planned to be used.
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FtpFileAttributes) {
            FtpFileAttributes other = (FtpFileAttributes) obj;

            if (areFtpFilesIdentical(attributes, other.attributes)) {
                return true;
            }
        }

        return false;
    }

    private boolean areFtpFilesIdentical(FTPFile a, FTPFile b) {
        if (haveDifferentTimestamps(a, b)) {
            return false;
        }

        if (haveDifferentGroups(a, b)) {
            return false;
        }

        if (haveDifferentUsers(a, b)) {
            return false;
        }

        if (a.getSize() != b.getSize()) {
            return false;
        }

        for (int userType : userTypes) {
            for (int permissionType : permissionTypes) {
                if (a.hasPermission(userType, permissionType) != b.hasPermission(userType, permissionType)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean haveDifferentTimestamps(FTPFile a, FTPFile b) {
        return !a.getTimestamp().equals(b.getTimestamp());
    }

    private boolean haveDifferentGroups(FTPFile a, FTPFile b) {
        return !a.getGroup().equals(b.getGroup());
    }

    private boolean haveDifferentUsers(FTPFile a, FTPFile b) {
        return !a.getUser().equals(b.getUser());
    }

}