package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.Set;

import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.apache.commons.net.ftp.FTPFile;

public class FtpFileAttributes implements FileAttributes {

    private boolean isDirectory;
    private boolean isSymbolicLink;
    private boolean isRegularFile;
    private boolean isOther;

    public FtpFileAttributes(FTPFile listFile) {
        isDirectory = listFile.isDirectory();
        isSymbolicLink = listFile.isSymbolicLink();
        isOther = listFile.isUnknown();
        isRegularFile = listFile.isFile();
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
        return 0;
    }

    @Override
    public long lastAccessTime() {
        return 0;
    }

    @Override
    public long lastModifiedTime() {
        return 0;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        return null;
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return null;
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        return null;
    }

}
