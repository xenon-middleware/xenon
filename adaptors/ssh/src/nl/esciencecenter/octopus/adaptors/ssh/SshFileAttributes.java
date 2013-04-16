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

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;

import com.jcraft.jsch.SftpATTRS;

public class SshFileAttributes implements FileAttributes {
    static final int SUID = 04000; // set user ID on execution
    static final int SGID = 02000; // set group ID on execution
    static final int SVTX = 01000; // sticky bit   ****** NOT DOCUMENTED *****

    static final int READ = 00400; // read by owner
    static final int WRITE = 00200; // write by owner
    static final int EXEC = 00100; // execute/search by owner

    static final int READ_GROUP = 00040; // read by group
    static final int WRITE_GROUP = 00020; // write by group
    static final int EXEC_GROUP = 00010; // execute/search by group

    static final int READ_OTHERS = 00004; // read by others
    static final int WRITE_OTHERS = 00002; // write by others
    static final int EXEC_OTHERS = 00001; // execute/search by others

    SftpATTRS attributes;
    AbsolutePath path;

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
        return attributes.isBlk() || attributes.isChr() || attributes.isDir() || attributes.isFifo() || attributes.isLink()
                || attributes.isSock();
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
        throw new AttributeNotSupportedException("ssh", "Attribute create time not supported");
    }

    @Override
    public long lastAccessTime() throws AttributeNotSupportedException {
        return (long) attributes.getATime() * 1000;
    }

    @Override
    public long lastModifiedTime() throws AttributeNotSupportedException {
        return (long) attributes.getMTime() * 1000;
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
        HashSet<PosixFilePermission> result = new HashSet<PosixFilePermission>();
        if ((attributes.getPermissions() & READ) != 0) {
            result.add(PosixFilePermission.OWNER_READ);
        }
        if ((attributes.getPermissions() & WRITE) != 0) {
            result.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((attributes.getPermissions() & EXEC) != 0) {
            result.add(PosixFilePermission.OWNER_EXECUTE);
        }

        if ((attributes.getPermissions() & READ_GROUP) != 0) {
            result.add(PosixFilePermission.GROUP_READ);
        }
        if ((attributes.getPermissions() & WRITE_GROUP) != 0) {
            result.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((attributes.getPermissions() & EXEC_GROUP) != 0) {
            result.add(PosixFilePermission.GROUP_EXECUTE);
        }

        if ((attributes.getPermissions() & READ_OTHERS) != 0) {
            result.add(PosixFilePermission.OTHERS_READ);
        }
        if ((attributes.getPermissions() & WRITE_OTHERS) != 0) {
            result.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((attributes.getPermissions() & EXEC_OTHERS) != 0) {
            result.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return result;
    }

    @Override
    public boolean isExecutable() throws AttributeNotSupportedException {
        return (attributes.getPermissions() & EXEC) != 0;
    }

    @Override
    public boolean isHidden() throws AttributeNotSupportedException {
        return path.getFileName().startsWith(".");
    }

    @Override
    public boolean isReadable() throws AttributeNotSupportedException {
        return (attributes.getPermissions() & READ) != 0;
    }

    @Override
    public boolean isWritable() throws AttributeNotSupportedException {
        return (attributes.getPermissions() & WRITE) != 0;
    }

    @Override
    public String toString() {
        return "" + attributes;
    }
}
