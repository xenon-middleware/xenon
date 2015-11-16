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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.util.Set;

import nl.esciencecenter.xenon.engine.util.PosixFileUtils;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import com.jcraft.jsch.SftpATTRS;

@SuppressWarnings("OctalInteger")
public class SshFileAttributes implements FileAttributes {

    /** Number of millisecond per second seems to be 1000 */
    private static final int MILLISECONDS_PER_SECOND = 1000;

    /** Bit set to set user ID on execution */
    @SuppressWarnings("OctalInteger")
    public static final int SUID = 04000;

    /** Bit set to set group ID on execution */
    @SuppressWarnings("OctalInteger")
    public static final int SGID = 02000;

    /** Bit set to set sticky bit ****** NOT DOCUMENTED *****/
    @SuppressWarnings("OctalInteger")
    public static final int SVTX = 01000;

    private final SftpATTRS attributes;
    private final Path path;

    public SshFileAttributes(SftpATTRS attributes, Path path) {
        this.attributes = attributes;
        this.path = path;
    }

    @Override
    public boolean isDirectory() {
        return attributes.isDir();
    }

    @Override
    public boolean isOther() {
        return attributes.isBlk() || attributes.isChr() || attributes.isFifo() || attributes.isSock();
    }

    @Override
    public boolean isRegularFile() {
        return attributes.isReg();
    }

    @Override
    public boolean isSymbolicLink() {
        return attributes.isLink();
    }

    @Override
    public long creationTime() {
        return lastModifiedTime();
    }

    @Override
    public long lastAccessTime()  {
        return (long) attributes.getATime() * MILLISECONDS_PER_SECOND;
    }

    @Override
    public long lastModifiedTime() {
        return (long) attributes.getMTime() * MILLISECONDS_PER_SECOND;
    }

    @Override
    public long size() {
        if (isRegularFile()) { 
            return attributes.getSize();
        } else { 
            return 0;
        }
    }

    @Override
    public String group() {
        return "" + attributes.getGId();
    }

    @Override
    public String owner() {
        return "" + attributes.getUId();
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        return PosixFileUtils.bitsToPermissions(attributes.getPermissions());
    }

    @Override
    public boolean isExecutable() {
        return PosixFileUtils.isExecutable(attributes.getPermissions());
    }

    @Override
    public boolean isHidden() {
        return path.getRelativePath().getFileNameAsString().startsWith(".");
    }

    @Override
    public boolean isReadable() {
        return PosixFileUtils.isReadable(attributes.getPermissions());
    }

    @Override
    public boolean isWritable() {
        return PosixFileUtils.isWritable(attributes.getPermissions());
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
