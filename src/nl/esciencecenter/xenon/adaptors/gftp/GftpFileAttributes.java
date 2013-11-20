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
package nl.esciencecenter.xenon.adaptors.gftp;

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.globus.ftp.MlsxEntry;

/**
 * GridFTP File Attributes wrap around an MslxEntry containing the file system specific attributes. Various FileSystems support
 * various file attributes.
 * 
 * Note that some (POSIX) attributes like 'group' and 'user' are not applicable in some Grid environments.
 * 
 * @author Piter T. de Boer
 */
public class GftpFileAttributes implements FileAttributes {

    public static final String UNIX_GROUP = "unix.group";

    public static final String UNIX_GID = "unix.gid";

    public static final String UNIX_OWNER = "unix.owner";

    public static final String UNIX_UID = "unix.uid";

    public static final String UNIX_MODE = "unix.mode";
    
    // ---
    // Instance
    // --- 
    
    private final Path path;

    private final MlsxEntry mlsxEntry;

    public GftpFileAttributes(MlsxEntry mlsxEntry, Path path) {

        if (mlsxEntry == null) {
            throw new NullPointerException(
                    "GftpFileAttributes(): mslxEntry can not be null. use fakeMlsx() or createMslx() methods to create dummy MslxEntry object!");
        }
        this.mlsxEntry = mlsxEntry;
        this.path = path;
    }

    @Override
    public boolean isDirectory() {
        return isDir();
    }

    @Override
    public boolean isOther() {
        return ((!isRegularFile()) && (!isDirectory()) & (!isSymbolicLink()));
    }

    @Override
    public boolean isRegularFile() {
        return isFile();
    }

    @Override
    public boolean isSymbolicLink() {
        return false; // not supported. 
    }

    @Override
    public long creationTime() {
        return getCreationTime();
    }

    @Override
    public long lastAccessTime() {
        return -1;
    }

    @Override
    public long lastModifiedTime() {
        return getModificationTime();
    }

    @Override
    public long size() {
        return getLength();
    }

    @Override
    public String group() {
        return getUnixGroup();
    }

    @Override
    public String owner() {
        return getUnixOwner();
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        
        // unix mode supported ? 
        int mode=getUnixMode();
            
        if (mode>=0)
        {
            return GftpUtil.unixModeToPosixFilePermissions(mode); 
        }
        else
        {
            return getPosixPermissionFromPERMString(); 
        }
    }

    @Override
    public boolean isExecutable() {

        if (isDirectory()) {
            return isAccessable();
        } else {
            return isExecutable();
        }
    }

    @Override
    public boolean isHidden() {
        return path.getRelativePath().getFileName().startsWith(".");
    }

    @Override
    public String toString() {
        if (mlsxEntry == null) {
            return "mlsxEntry:NULL";
        } else {
            return "mlsxEntry:" + mlsxEntry.toString();
        }
    }

    @Override
    public boolean isReadable() {
        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));

        if (perm == null) {
            return false;
        }
        return perm.isPosixReadable();
    }

    public boolean isAccessable() {
        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));

        if (perm == null) {
            return false;
        }

        // files must be 'readable' and directories must be 'listable':
        return (perm.readable || perm.listable);
    }

    public boolean isDir() {
        String val = mlsxEntry.get(MlsxEntry.TYPE);

        if (val == null)
            return false;

        return (val.compareTo(MlsxEntry.TYPE_DIR) == 0);
    }

    @Override
    public boolean isWritable() {
        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));

        if (perm == null)
            return false;

        return perm.isPosixWritable();
    }

    

    public String getUnixUID() {
        // gftp v1 dummy MslxEntry:
        if (mlsxEntry == null) {
            return null;
        }

        return mlsxEntry.get(UNIX_UID);
    }

    public String getUnixOwner() {
        // gftp v1 dummy MslxEntry:
        if (mlsxEntry == null) {
            return null;
        }

        return mlsxEntry.get(UNIX_OWNER);
    }

    /**
     * Returns Octagonal Unix mode as integer !
     * 
     * @return
     */
    public int getUnixMode() {

        // gftp v1 dummy MslxEntry:
        if (mlsxEntry == null) {
            return -1;
        }

        String val = mlsxEntry.get(UNIX_MODE);

        if ((val == null) || (val == "")) {
            return -1;
        }

        return Integer.parseInt(val, 8);
    }

    /**
     * Return Unix Group ID, which typically is a file system depended group id number.
     * 
     * @param mlsxEntry
     *            - MslxEnty
     * @return
     */
    public String getGID() {
        return mlsxEntry.get(UNIX_GID);
    }

    /**
     * Return logical Unix Group name.
     * 
     * @param mlsxEntry
     *            - MslxEnty
     * @return
     */
    public String getUnixGroup() {
        return mlsxEntry.get(UNIX_GROUP);
    }

    public long getCreationTime() {

        String val = mlsxEntry.get(MlsxEntry.CREATE);
        //debug("getCreationTime val=" + val);

        if (val == null) {
            //debug("CREATE=null in mlsxEntry:" + mlsxEntry);
            return 0;
        }

        return GftpUtil.timeStringToMillis(val);
    }

    public long getModificationTime() {

        String val = mlsxEntry.get(MlsxEntry.MODIFY);

        if (val == null) {
            return 0;
        }

        return GftpUtil.timeStringToMillis(val);
    }

    public String getUnique() {
        return mlsxEntry.get(MlsxEntry.UNIQUE);
    }

    public long getLength() {

        String val = mlsxEntry.get(MlsxEntry.SIZE);

        if (val == null) {
            return 0;
        }

        return Long.valueOf(val);
    }

    public boolean isFile() {

        String val = mlsxEntry.get(MlsxEntry.TYPE);

        if (val == null)
            return false;

        return (val.compareTo(MlsxEntry.TYPE_FILE) == 0);
    }

    /**
     * Check Whether "TYPE" is either parent dir (PDIR) or current dir (CDIR)
     * 
     * @param mlsxEntry
     *            - MlsxEntry
     * @return true is mlsxEntry is for "." or ".." (current dir or parent dir respectively).
     */
    public boolean isXDir() {
        String val = mlsxEntry.get(MlsxEntry.TYPE);

        if (val == null) {
            return false;
        }

        if (val.compareTo(MlsxEntry.TYPE_CDIR) == 0) // Current Dir
        {
            return true;
        }

        if (val.compareTo(MlsxEntry.TYPE_PDIR) == 0) // Parent Dir
        {
            return true;
        }

        return false;
    }

    public Set<PosixFilePermission> getPosixPermissionFromPERMString() {

        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));
        Set<PosixFilePermission> set = new HashSet<PosixFilePermission>();

        // Gftp Permissions are only given for the current users credentials so 
        // all permissions are "OWNER". 
        // Groups do not exists. VOs are used for this, but can't be checked here.  
        if (perm.isPosixReadable()) {
            set.add(PosixFilePermission.OWNER_READ);
        }
        if (perm.isPosixWritable()) {
            set.add(PosixFilePermission.OWNER_WRITE);
        }

        // Posix "x" bit for directories means 'listable'. 
        if (isDir() && perm.listable) {
            set.add(PosixFilePermission.OWNER_EXECUTE);
        }

        return set;
    }

}
