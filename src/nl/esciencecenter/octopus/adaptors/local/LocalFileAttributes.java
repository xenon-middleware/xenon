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

/**
 * LocalFileAttributes implements a {@link FileAttributes} for local files. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
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
    
    @Override
    public String toString() {
        return "LocalFileAttributes [executable=" + executable + ", readable=" + readable
                + ", writable=" + writable + ", hidden=" + hidden + ", attributes=" + attributes + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + (executable ? 1231 : 1237);
        result = prime * result + (hidden ? 1231 : 1237);
        result = prime * result + (readable ? 1231 : 1237);
        result = prime * result + (writable ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        LocalFileAttributes other = (LocalFileAttributes) obj;
                
        if (executable != other.executable) { 
            return false;
        }
        
        if (hidden != other.hidden) { 
            return false;
        }
        
        if (readable != other.readable) { 
            return false;
        }
        
        if (writable != other.writable) { 
            return false;
        }
        
        if (attributes == null) { 
            if (other.attributes != null) { 
                return false;
            }
        } else if (!attributes.equals(other.attributes)) { 
            return false;
        }
        
        return true;
    }

    //    @Override
    //    public List<AclEntry> getAcl() throws AttributeNotSupportedException {
    //        throw new UnsupportedOperationException("Local adaptor cannot handle ACLs yet");
    //    }
    
    
}
