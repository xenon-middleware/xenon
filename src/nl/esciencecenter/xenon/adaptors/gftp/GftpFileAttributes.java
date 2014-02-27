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

import nl.esciencecenter.xenon.engine.util.PosixFileUtils;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PosixFilePermission;

import org.globus.ftp.MlsxEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GftpFileAttributes class wraps around an MslxEntry containing default and file system specific attributes.
 * <p>
 * Various Grid FTP FileSystems support various file attributes.<br>
 * Note that some (POSIX) attributes like 'group' and 'user' are not applicable in most Grid environments. Also permission
 * attributes are given relative for the authenticated user.
 * 
 * @author Piter T. de Boer
 */
public class GftpFileAttributes implements FileAttributes {

    private static final Logger logger = LoggerFactory.getLogger(GftpFileAttributes.class);

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
    public boolean isOther() {
        return ((!isRegularFile()) && (!isDirectory()) && (!isSymbolicLink()));
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
        return getSize();
    }

    @Override
    public String group() {
        // default to unix.group could be "VO" as well. 
        return getUnixGroup();
    }

    @Override
    public String owner() {
        // default to unix.user, could also be "VO" or Proxy Subject DN.
        return getUnixOwner();
    }

    @Override
    public Set<PosixFilePermission> permissions() {

        // unix mode supported ? 
        int mode = getUnixMode();

        if (mode >= 0) {
            return PosixFileUtils.bitsToPermissions(mode);
        } else {
            return createPosixPermissionsFromPERMString();
        }
    }

    @Override
    public boolean isExecutable() {

        if (isDirectory()) {
            //'x' bit for directories means 'accessible'.
            return isAccessable();
        } else {
            // default to --x--x--x 
            return ((getUnixMode() & PosixFileUtils.EXEC_OWNER) > 0);
        }
    }

    @Override
    public boolean isHidden() {
        // assume unix style hidden files on the grid, most are unix/linux file systems: 
        return path.getRelativePath().getFileName().startsWith(".");
    }

    @Override
    public String toString() {
        String str = "GftpFileAttributes:[path=" + path + ",";
        if (mlsxEntry == null) {
            str+="mlsxEntry=NULL]";
        } else {
            str+="mlsxEntry=" + mlsxEntry.toString() + "]";
        }
        return str; 
    }

    @Override
    public boolean isReadable() {

        // use generic FTP permissions (not unix mode):
        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));

        if (perm == null) {
            return false;
        }
        return perm.isPosixReadable();
    }

    public boolean isAccessable() {

        // use generic FTP permissions (not unix mode): 
        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));

        if (perm == null) {
            return false;
        }

        // Files must be 'readable' and directories must be 'listable':
        return (perm.readable || perm.listable);
    }

    @Override
    public boolean isDirectory() {
        String val = mlsxEntry.get(MlsxEntry.TYPE);

        if (val == null)
            return false;

        return (val.compareTo(MlsxEntry.TYPE_DIR) == 0);
    }

    @Override
    public boolean isWritable() {

        // use generic FTP permissions:
        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));

        if (perm == null)
            return false;

        return perm.isPosixWritable();
    }

    /**
     * @return (Unix) User ID of owner, which typically is a number and OS dependent, not the actual user name.
     */
    public String getUnixUID() {
        return mlsxEntry.get(UNIX_UID);
    }

    /**
     * @return Actual (Unix) user name of owner, if known. Might be null if not applicable.
     */
    public String getUnixOwner() {
        return mlsxEntry.get(UNIX_OWNER);
    }

    /**
     * @return (Octal) Unix File Mode as integer, or -1 if not supported.
     */
    public int getUnixMode() {

        String val = mlsxEntry.get(UNIX_MODE);

        if ((val == null) || (val == "")) {
            return -1;
        }

        return Integer.parseInt(val, 8);
    }

    /**
     * @return Unix Group ID, which typically is a system dependent group id number, or null otherwise.
     */
    public String getGID() {
        return mlsxEntry.get(UNIX_GID);
    }

    /**
     * @return Actual Unix Group name, if supported, null otherwise.
     */
    public String getUnixGroup() {
        return mlsxEntry.get(UNIX_GROUP);
    }

    /**
     * @return Creation Time in millis since EPOCH, or -1 if not known.
     */
    public long getCreationTime() {

        String val = mlsxEntry.get(MlsxEntry.CREATE);

        if (val == null) {
            return -1;
        }

        return GftpUtil.timeStringToMillis(val);
    }

    /**
     * @return Creation Time as Java Date, or null if not known.
     */
    public java.util.Date getCreationTimeDate() {

        String val = mlsxEntry.get(MlsxEntry.CREATE);

        if (val == null) {
            return null;
        }

        return GftpUtil.timeStringToDate(val);
    }

    /**
     * @return Modification time in millis since EPOCH, or -1 if not known.
     */
    public long getModificationTime() {

        String val = mlsxEntry.get(MlsxEntry.MODIFY);

        if (val == null) {
            return -1;
        }

        return GftpUtil.timeStringToMillis(val);
    }

    /**
     * @return Modification time as Java Date or null if not known.
     */
    public java.util.Date getModificationTimeDate() {

        String val = mlsxEntry.get(MlsxEntry.MODIFY);

        if (val == null) {
            return null;
        }

        return GftpUtil.timeStringToDate(val);
    }

    /**
     * @return unique file id for this file system.
     */
    public String getUnique() {
        return mlsxEntry.get(MlsxEntry.UNIQUE);
    }

    public long getSize() {

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
     * Check whether "TYPE" is either parent dir "PDIR" or current dir "CDIR".
     * 
     * @return true if mlsxEntry is for "." or for ".." (current dir or parent dir respectively).
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

    /**
     * Create PosixFilePermissions from FTP Permissions String (PERM).<br>
     * These permissions are only given for the current authenticated user.
     * 
     * @return PosixFilePermissions of current owner, as parsed from the PERM attribute.
     */
    public Set<PosixFilePermission> createPosixPermissionsFromPERMString() {

        FtpPermissions perm = FtpPermissions.fromString(mlsxEntry.get(MlsxEntry.PERM));
        Set<PosixFilePermission> set = new HashSet<PosixFilePermission>();

        // Gftp Permissions are only given for the current users credentials so 
        // all permissions are "OWNER". 
        // Groups may not exist. VOs are used for this, but can't be checked here.  
        if (perm.isPosixReadable()) {
            set.add(PosixFilePermission.OWNER_READ);
        }
        if (perm.isPosixWritable()) {
            set.add(PosixFilePermission.OWNER_WRITE);
        }

        // Posix "x" bit for directories means 'accessable'. 
        if (isDirectory() && perm.enterable) {
            set.add(PosixFilePermission.OWNER_EXECUTE);
        }

        return set;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mlsxEntry == null) ? 0 : mlsxEntry.toString().hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        logger.error("equals: {} == {} ", this, obj);

        GftpFileAttributes other = (GftpFileAttributes) obj;

        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path)) {
            return false;
        }
        // Compare String representations of mslxEntry !  
        // note that the order of mlsxEntries is not defined. 
        if (mlsxEntry == null) {
            if (other.mlsxEntry != null) {
                return false;
            }
        } else if (!mlsxEntry.toString().equals(other.mlsxEntry.toString()))
            return false;
        return true;
    }

}
