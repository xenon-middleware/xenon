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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;

import com.jcraft.jsch.SftpATTRS;

public class SshFileAttributes implements FileAttributes {

    private final static int MILLISECONDS_PER_SECOND = 1000;
    
    
    /** Bitset to set user ID on execution */
    static final int SUID = 04000; 
 
    /** Bitset to set group ID on execution */
    static final int SGID = 02000;
    
    /** Bitset to set sticky bit   ****** NOT DOCUMENTED *****/
    static final int SVTX = 01000; 

    private final SftpATTRS attributes;
    private final AbsolutePath path;

    public SshFileAttributes(SftpATTRS attributes, AbsolutePath path) {
        this.attributes = attributes;
        this.path = path;
    }

    @Override
    public boolean isDirectory() throws AttributeNotSupportedException {
        return attributes.isDir();
    }

    @Override
    public boolean isOther() throws AttributeNotSupportedException {
        return attributes.isBlk() || attributes.isChr() || attributes.isFifo() || attributes.isSock();
    }

    @Override
    public boolean isRegularFile() throws AttributeNotSupportedException {
        return attributes.isReg();
    }

    @Override
    public boolean isSymbolicLink() throws AttributeNotSupportedException {
        return attributes.isLink();
    }

    @Override
    public long creationTime() throws AttributeNotSupportedException {
        throw new AttributeNotSupportedException(SshAdaptor.ADAPTOR_NAME, "Attribute create time not supported");
    }

    @Override
    public long lastAccessTime() throws AttributeNotSupportedException {
        return (long) attributes.getATime() * MILLISECONDS_PER_SECOND;
    }

    @Override
    public long lastModifiedTime() throws AttributeNotSupportedException {
        return (long) attributes.getMTime() * MILLISECONDS_PER_SECOND;
    }

    @Override
    public long size() throws AttributeNotSupportedException {
        return attributes.getSize();
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        return "" + attributes.getGId();
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return "" + attributes.getUId();
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        return SshUtil.bitsToPermissions(attributes.getPermissions());
    }

    @Override
    public boolean isExecutable() throws AttributeNotSupportedException {
        return SshUtil.isExecutable(attributes.getPermissions());
    }

    @Override
    public boolean isHidden() throws AttributeNotSupportedException {
        return path.getFileName().startsWith(".");
    }

    @Override
    public boolean isReadable() throws AttributeNotSupportedException {
        return SshUtil.isReadable(attributes.getPermissions());
    }

    @Override
    public boolean isWritable() throws AttributeNotSupportedException {
        return SshUtil.isWritable(attributes.getPermissions());
    }

    @Override
    public String toString() {
        return "" + attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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

        SshFileAttributes other = (SshFileAttributes) obj;

        if (!SshUtil.equals(attributes, other.attributes)) {
            return false;
        }

        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }

        return true;
    }
}
