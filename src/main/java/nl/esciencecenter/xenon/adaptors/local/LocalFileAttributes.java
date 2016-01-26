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
package nl.esciencecenter.xenon.adaptors.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.util.Utils;

/**
 * LocalFileAttributes implements a {@link FileAttributes} for local files.
 * 
 * @version 1.0
 * @since 1.0
 */
public class LocalFileAttributes implements FileAttributes {
          
    /** Is this a directory ? */
    private final boolean isDirectory;

    /** Is this a regular file ? */
    private final boolean isRegular;

    /** Is this a symbolic link ? */
    private final boolean isSymbolicLink;
    
    /** Is this an other type of file ? */
    private final boolean isOther;
    
    /** Is the file executable ? */
    private final boolean executable;

    /** Is the file readable ? */
    private final boolean readable;

    /** Is the file writable ? */
    private final boolean writable;

    /** Is the file hidden ? */
    private final boolean hidden;

    /** The creation time of this file */
    private final long creationTime;
    
    /** The last access time of this file */
    private final long lastAccessTime;
    
    /** The last modified time of this file */
    private final long lastModifiedTime;

    /** The size of this file */
    private final long size;
    
    /** The owner of this file */
    private final String owner;
    
    /** The group of this file */
    private final String group;
    
    /** The permissions of this file (POSIX only) */
    private final Set<PosixFilePermission> permissions;
    
    /** Is this a windows file ? */
    private final boolean isWindows;
    
    
    public LocalFileAttributes(Path path) throws XenonException {
        try {
            java.nio.file.Path javaPath = LocalUtils.javaPath(path);

            executable = Files.isExecutable(javaPath);
            readable = Files.isReadable(javaPath);
            writable = Files.isWritable(javaPath);
            
            isWindows = Utils.isWindows(); 

            BasicFileAttributes basicAttributes;
            
            if (isWindows) {                
                // TODO: Seems to fail in Windows ?
                hidden = false;
                
                // These should always work.
                basicAttributes = Files.readAttributes(javaPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                
                // These are windows only.
//                AclFileAttributeView aclAttributes = Files.getFileAttributeView(javaPath, AclFileAttributeView.class, 
//                        LinkOption.NOFOLLOW_LINKS);
//                
                group = null;
                permissions = null;
                owner = null;
            } else {
                hidden = Files.isHidden(javaPath);
                
                // Note: when in a posix environment, basicAttributes point to posixAttributes.
                PosixFileAttributes posixAttributes = Files.readAttributes(javaPath, PosixFileAttributes.class, 
                        LinkOption.NOFOLLOW_LINKS);
                
                basicAttributes = posixAttributes;
    
                owner = posixAttributes.owner().getName();
                group = posixAttributes.group().getName();
                permissions = LocalUtils.xenonPermissions(posixAttributes.permissions());
            }
            
            creationTime = basicAttributes.creationTime().toMillis();
            lastAccessTime = basicAttributes.lastAccessTime().toMillis();
            lastModifiedTime = basicAttributes.lastModifiedTime().toMillis();
            
            isDirectory = basicAttributes.isDirectory();
            isRegular = basicAttributes.isRegularFile();
            isSymbolicLink = basicAttributes.isSymbolicLink();
            isOther = basicAttributes.isOther();
            
            if (isRegular) { 
                size = basicAttributes.size();
            } else { 
                size = 0;
            }
            
        } catch (IOException e) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Cannot read attributes.", e);
        }
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
        return isRegular;
    }

    @Override
    public boolean isSymbolicLink() {
        return isSymbolicLink;
    }

    @Override
    public long creationTime() {
        return creationTime;
    }

    @Override
    public long lastAccessTime() {
        return lastAccessTime;
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
    public String group() throws AttributeNotSupportedException {
        
        if (isWindows) { 
            throw new AttributeNotSupportedException(LocalAdaptor.ADAPTOR_NAME, "Attribute not supported: group");
        }

        return group;
    }
 
    @Override
    public String owner() throws AttributeNotSupportedException {
        
        if (isWindows) { 
            throw new AttributeNotSupportedException(LocalAdaptor.ADAPTOR_NAME, "Attribute not supported: owner");
        }

        return owner;
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        
        if (isWindows) { 
            throw new AttributeNotSupportedException(LocalAdaptor.ADAPTOR_NAME, "Attribute not supported: permissions");            
        } else { 
            return permissions;
        }
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean isReadable() {
        return readable;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }
    
    @Override
    @SuppressWarnings("PMD.NPathComplexity")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
        result = prime * result + (executable ? 1231 : 1237);
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + (hidden ? 1231 : 1237);
        result = prime * result + (isDirectory ? 1231 : 1237);
        result = prime * result + (isOther ? 1231 : 1237);
        result = prime * result + (isRegular ? 1231 : 1237);
        result = prime * result + (isSymbolicLink ? 1231 : 1237);
        result = prime * result + (isWindows ? 1231 : 1237);
        result = prime * result + (int) (lastAccessTime ^ (lastAccessTime >>> 32));
        result = prime * result + (int) (lastModifiedTime ^ (lastModifiedTime >>> 32));
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
        result = prime * result + (readable ? 1231 : 1237);
        result = prime * result + (int) (size ^ (size >>> 32));
        result = prime * result + (writable ? 1231 : 1237);
        return result;
    }

    @Override
    public String toString() {
        return "LocalFileAttributes [isDirectory=" + isDirectory + ", isRegular=" + isRegular + ", isSymbolicLink="
                + isSymbolicLink + ", isOther=" + isOther + ", executable=" + executable + ", readable=" + readable
                + ", writable=" + writable + ", hidden=" + hidden + ", creationTime=" + creationTime + ", lastAccessTime="
                + lastAccessTime + ", lastModifiedTime=" + lastModifiedTime + ", size=" + size + ", owner=" + owner + ", group="
                + group + ", permissions=" + permissions + ", isWindows=" + isWindows + "]";
    }

    @Override
    @SuppressWarnings("PMD.NPathComplexity")
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
        
        if (group == null) {
            if (other.group != null) { 
                return false;
            }
        } else if (!group.equals(other.group)) { 
            return false;
        }
                
        if (owner == null) {
            if (other.owner != null) { 
                return false;
            }
        } else if (!owner.equals(other.owner)) { 
            return false;
        }
        
        if (permissions == null) { 
            if (other.permissions != null) { 
                return false;
            }
        } else if (!permissions.equals(other.permissions)) { 
            return false;
        }

        return (hidden == other.hidden && isDirectory == other.isDirectory && isOther == other.isOther && 
                isRegular == other.isRegular && isSymbolicLink == other.isSymbolicLink && isWindows == other.isWindows &&
                lastAccessTime == other.lastAccessTime && lastModifiedTime == other.lastModifiedTime && 
                readable == other.readable && size == other.size && writable == other.writable && 
                creationTime == other.creationTime && executable == other.executable); 
    }
}
