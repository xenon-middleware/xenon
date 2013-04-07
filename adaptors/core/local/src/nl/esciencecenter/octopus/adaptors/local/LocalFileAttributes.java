package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AclEntry;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PosixFilePermission;

public class LocalFileAttributes implements FileAttributes {

    private final PosixFileAttributes attributes;
    private final boolean executable;
    private final boolean readable;
    private final boolean writable;
    private final boolean hidden;

    public LocalFileAttributes(Path path) throws OctopusException {
        try {
            java.nio.file.Path javaPath = LocalUtils.javaPath(path);

            attributes = Files.readAttributes(javaPath, PosixFileAttributes.class);

            executable = Files.isExecutable(javaPath);
            readable = Files.isReadable(javaPath);
            writable = Files.isWritable(javaPath);
            hidden = Files.isHidden(javaPath);
            
        } catch (IOException e) {
            throw new OctopusException("Cannot read attributes", e, null, null);
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
        return LocalUtils.gatPermissions(attributes.permissions());
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

    @Override
    public List<AclEntry> getAcl() throws AttributeNotSupportedException {
        throw new UnsupportedOperationException("Local adaptor cannot handle ACLs yet");
    }
}
