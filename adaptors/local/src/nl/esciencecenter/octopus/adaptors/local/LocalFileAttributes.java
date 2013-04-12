package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PosixFilePermission;

public class LocalFileAttributes implements FileAttributes {

    /** The file attributes from the underlying java.nio implementation */
    private final PosixFileAttributes attributes;
    
    /** Is the file executable ? */
    private final boolean executable;
    
    /** Is the file readable ? */
    private final boolean readable;

    /** Is the file writable ? */
    private final boolean writable;
    
    /** Is the file hidden ? */
    private final boolean hidden;

    public LocalFileAttributes(AbsolutePath path) throws OctopusIOException {
        try {
            java.nio.file.Path javaPath = LocalUtils.javaPath(path);

            attributes = Files.readAttributes(javaPath, PosixFileAttributes.class);

            executable = Files.isExecutable(javaPath);
            readable = Files.isReadable(javaPath);
            writable = Files.isWritable(javaPath);
            hidden = Files.isHidden(javaPath);

        } catch (IOException e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Cannot read attributes.", e);
        }
    }

    @Override
    public boolean isDirectory() throws AttributeNotSupportedException {
        return attributes.isDirectory();
    }

    @Override
    public boolean isOther() throws AttributeNotSupportedException {
        return attributes.isOther();
    }

    @Override
    public boolean isRegularFile() throws AttributeNotSupportedException {
        return attributes.isRegularFile();
    }

    @Override
    public boolean isSymbolicLink() throws AttributeNotSupportedException {
        return attributes.isSymbolicLink();
    }

    @Override
    public long creationTime() throws AttributeNotSupportedException {
        return attributes.creationTime().toMillis();
    }

    @Override
    public long lastAccessTime() throws AttributeNotSupportedException {
        return attributes.lastAccessTime().toMillis();
    }

    @Override
    public long lastModifiedTime() throws AttributeNotSupportedException {
        return attributes.lastModifiedTime().toMillis();
    }

    @Override
    public long size() throws AttributeNotSupportedException {
        return attributes.size();
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        return attributes.group().getName();
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return attributes.owner().getName();
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        return LocalUtils.octopusPermissions(attributes.permissions());
    }

    @Override
    public boolean isExecutable() throws AttributeNotSupportedException {
        return executable;
    }

    @Override
    public boolean isHidden() throws AttributeNotSupportedException {
        return hidden;
    }

    @Override
    public boolean isReadable() throws AttributeNotSupportedException {
        return readable;
    }

    @Override
    public boolean isWritable() throws AttributeNotSupportedException {
        return writable;
    }

//    @Override
//    public List<AclEntry> getAcl() throws AttributeNotSupportedException {
//        throw new UnsupportedOperationException("Local adaptor cannot handle ACLs yet");
//    }
}
