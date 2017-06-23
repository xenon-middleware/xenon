package nl.esciencecenter.xenon.adaptors.file.s3;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import java.util.Set;

/**
 * Created by atze on 23-6-17.
 */
class S3FileAttributes implements FileAttributes {

    final S3ObjectSummary summary;

    S3FileAttributes(S3ObjectSummary summary){
        this.summary = summary;
    }


    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
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
        return summary.getLastModified().getTime();
    }

    @Override
    public long size() {
        return summary.getSize();
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
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        throw new AttributeNotSupportedException("s3","S3 does not have groups for files.");
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return summary.getOwner().getId();
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        throw new AttributeNotSupportedException("s3","S3 does not have posix permissions for files.");
    }
}
