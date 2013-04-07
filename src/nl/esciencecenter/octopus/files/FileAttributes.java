package nl.esciencecenter.octopus.files;

import java.util.List;
import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;

public interface FileAttributes {

    boolean isDirectory() throws AttributeNotSupportedException;

    boolean isOther() throws AttributeNotSupportedException;

    boolean isRegularFile() throws AttributeNotSupportedException;

    boolean isSymbolicLink() throws AttributeNotSupportedException;

    long creationTime() throws AttributeNotSupportedException;
    
    long lastAccessTime() throws AttributeNotSupportedException;

    long lastModifiedTime() throws AttributeNotSupportedException;

    long size() throws AttributeNotSupportedException;

    // posix

    String group() throws AttributeNotSupportedException;

    String owner() throws AttributeNotSupportedException;

    Set<PosixFilePermission> permissions() throws AttributeNotSupportedException;

    /**
     * Reads the access control list.
     */
    List<AclEntry> getAcl() throws AttributeNotSupportedException;
    
    // non-java-nio attributes

    boolean isExecutable() throws AttributeNotSupportedException;

    boolean isHidden() throws AttributeNotSupportedException;

    boolean isReadable() throws AttributeNotSupportedException;

    boolean isWritable() throws AttributeNotSupportedException;
}
