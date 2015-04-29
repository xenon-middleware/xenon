package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.apache.commons.net.ftp.FTPFile;

public class FtpFileAttributes implements FileAttributes {
    private static final int[] permissionTypes = { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION };
    private static final int[] userTypes = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };

    private boolean isDirectory;
    private boolean isSymbolicLink;
    private boolean isRegularFile;
    private boolean isOther;
    private long size;
    private long lastModifiedTime;
    private String group;
    private String user;
    private Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();

    public FtpFileAttributes(FTPFile listFile) {
        isDirectory = listFile.isDirectory();
        isSymbolicLink = listFile.isSymbolicLink();
        isOther = listFile.isUnknown();
        isRegularFile = listFile.isFile();
        size = listFile.getSize();
        lastModifiedTime = listFile.getTimestamp().getTimeInMillis();
        storePermissions(listFile);
        user = listFile.getUser();
        group = listFile.getGroup();

    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean isOther() {
        return isOther;
    }

    @Override
    public boolean isRegularFile() {
        return isRegularFile;
    }

    @Override
    public boolean isSymbolicLink() {
        return isSymbolicLink;
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
        return lastModifiedTime;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean isExecutable() {
        return permissions.contains(PosixFilePermission.OWNER_EXECUTE);
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return permissions.contains(PosixFilePermission.OWNER_READ);
    }

    @Override
    public boolean isWritable() {
        return permissions.contains(PosixFilePermission.OWNER_WRITE);
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        return group;
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return user;
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        return permissions;
    }

    private void storePermissions(FTPFile listFile) {
        for (int userType : userTypes) {
            for (int permissionType : permissionTypes) {
                if (listFile.hasPermission(userType, permissionType)) {
                    permissions.add(getPosixFilePermission(userType, permissionType));
                }
            }
        }
    }

    static private PosixFilePermission getPosixFilePermission(int userType, int permissionType) {
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
}