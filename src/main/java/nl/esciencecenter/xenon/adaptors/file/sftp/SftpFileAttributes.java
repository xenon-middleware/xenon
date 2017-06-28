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
package nl.esciencecenter.xenon.adaptors.file.sftp;

import java.util.Set;

import org.apache.sshd.client.subsystem.sftp.SftpClient;

import nl.esciencecenter.xenon.adaptors.file.PosixFileUtils;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PosixFilePermission;

@SuppressWarnings("OctalInteger")
public class SftpFileAttributes implements FileAttributes {

    /** Bit set to set user ID on execution */
    @SuppressWarnings("OctalInteger")
    public static final int SUID = 04000;

    /** Bit set to set group ID on execution */
    @SuppressWarnings("OctalInteger")
    public static final int SGID = 02000;

    /** Bit set to set sticky bit ****** NOT DOCUMENTED *****/
    @SuppressWarnings("OctalInteger")
    public static final int SVTX = 01000;

    private final SftpClient.Attributes attributes;
    private final Path path;

    public SftpFileAttributes(SftpClient.Attributes attributes, Path path) {
        this.attributes = attributes;
        this.path = path;
    }

    @Override
    public boolean isDirectory() {	
        return attributes.isDirectory();
    }

    @Override
    public boolean isOther() {
        return attributes.isOther();
    }

    @Override
    public boolean isRegularFile() {
        return attributes.isRegularFile();
    }

    @Override
    public boolean isSymbolicLink() {
        return attributes.isSymbolicLink();
    }

    @Override
    public long creationTime() {
        return lastModifiedTime();
    }

    @Override
    public long lastAccessTime()  {
    	return attributes.getAccessTime().toMillis();
    }

    @Override
    public long lastModifiedTime() {
        return attributes.getModifyTime().toMillis();
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
        return attributes.getGroup();
    }

    @Override
    public String owner() {
        return attributes.getOwner();
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
        return path.getFileNameAsString().startsWith(".");
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
    
    private static boolean equalAttributes(SftpClient.Attributes a1, SftpClient.Attributes a2) {
        // Handles case of aliased object + both null
        if (a1 == a2) {
            return true;
        }

        // Handle case of one of the two null
        if (a1 == null || a2 == null) {
            return false;
        }

        System.out.println("COMPARE ATTR");
        System.out.println("Flags = " + (a1.getFlags() == a2.getFlags()));
        System.out.println("UserID = " + (a1.getUserId() == a2.getUserId()));
        System.out.println("Permissions = " + (a1.getPermissions() == a2.getPermissions()));
        System.out.println("Size = " + (a1.getSize() == a2.getSize()));
        System.out.println("Group = " + a1.getGroup().equals(a2.getGroup()));
        System.out.println("ModifyTime = " + a1.getModifyTime().equals(a2.getModifyTime()));
        System.out.println("AccessTime = " + a1.getAccessTime().equals(a2.getAccessTime()));
        
        // Test all fields
        return  a1.getFlags() == a2.getFlags()
                && a1.getUserId() == a2.getUserId() 
                && a1.getPermissions() == a2.getPermissions()
                && a1.getSize() == a2.getSize()
                && a1.getGroup().equals(a2.getGroup())
                && a1.getAccessTime().equals(a2.getAccessTime())
                && a1.getModifyTime().equals(a2.getModifyTime());
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

        SftpFileAttributes other = (SftpFileAttributes) obj;

        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }

        return equalAttributes(attributes, other.attributes); 
    }
}
