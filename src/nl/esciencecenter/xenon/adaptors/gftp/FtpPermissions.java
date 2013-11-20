package nl.esciencecenter.xenon.adaptors.gftp;

/**
 * Generic (G)FTP Permissions.
 * 
 * <br>
 * 
 * From: http://www.nextgen6.net/docs/proftpd/rfc/draft-ietf-ftpext-mlst-12.txt
 * 
 * <pre>
 * 7.5.5. The perm Fact
 * 
 *     The perm fact is used to indicate access rights the current FTP user
 *     has over the object listed.  Its value is always an unordered
 *     sequence of alphabetic characters.
 * 
 *         perm-fact    = &quot;Perm&quot; &quot;=&quot; *pvals
 *         pvals        = &quot;a&quot; / &quot;c&quot; / &quot;d&quot; / &quot;e&quot; / &quot;f&quot; /
 *                        &quot;l&quot; / &quot;m&quot; / &quot;p&quot; / &quot;r&quot; / &quot;w&quot;
 * 
 *     There are ten permission indicators currently defined.  Many are
 *     meaningful only when used with a particular type of object.  The
 *     indicators are case independent, &quot;d&quot; and &quot;D&quot; are the same indicator.
 * 
 *     The &quot;a&quot; permission applies to objects of type=file, and indicates
 *     that the APPE (append) command may be applied to the file named.
 * 
 *     The &quot;c&quot; permission applies to objects of type=dir (and type=pdir,
 *     type=cdir).  It indicates that files may be created in the directory
 *     named.  That is, that a STOU command is likely to succeed, and that
 *     STOR and APPE commands might succeed if the file named did not
 *     previously exist, but is to be created in the directory object that
 *     has the &quot;c&quot; permission.  It also indicates that the RNTO command is
 *     likely to succeed for names in the directory.
 * 
 *     The &quot;d&quot; permission applies to all types.  It indicates that the
 *     object named may be deleted, that is, that the RMD command may be
 *     applied to it if it is a directory, and otherwise that the DELE
 *     command may be applied to it.
 * 
 *     The &quot;e&quot; permission applies to the directory types.  When set on an
 *     object of type=dir, type=cdir, or type=pdir it indicates that a CWD
 *     command naming the object should succeed, and the user should be able
 *     to enter the directory named.  For type=pdir it also indicates that
 *     the CDUP command may succeed (if this particular pathname is the one
 *     to which a CDUP would apply.)
 * 
 *     The &quot;f&quot; permission for objects indicates that the object named may be
 *     renamed - that is, may be the object of an RNFR command.
 * 
 *     The &quot;l&quot; permission applies to the directory file types, and indicates
 *     that the listing commands, LIST, NLST, and MLSD may be applied to the
 *     directory in question.
 * 
 *     The &quot;m&quot; permission applies to directory types, and indicates that the
 *     MKD command may be used to create a new directory within the
 *     directory under consideration.
 * 
 *     The &quot;p&quot; permission applies to directory types, and indicates that
 *     objects in the directory may be deleted, or (stretching naming a
 *     little) that the directory may be purged.  Note: it does not indicate
 *     that the RMD command may be used to remove the directory named
 *     itself, the &quot;d&quot; permission indicator indicates that.
 * 
 *     The &quot;r&quot; permission applies to type=file objects, and for some
 *     systems, perhaps to other types of objects, and indicates that the
 *     RETR command may be applied to that object.
 * 
 *     The &quot;w&quot; permission applies to type=file objects, and for some
 *     systems, perhaps to other types of objects, and indicates that the
 *     STOR command may be applied to the object named.
 * 
 * </pre>
 * 
 * @Author Piter T. de Boer
 */

public class FtpPermissions {
    /**
     * File permission: 'a' for appendable
     */
    public boolean appendable = false;

    /**
     * Directory permission: 'c' for can create files
     */
    public boolean cancreatefiles = false;

    /**
     * File/Directory permission: 'd' for deletable (itself)
     */
    public boolean deletable = false;

    /**
     * Directory permission: 'e' enterable (cd into is allowed
     */
    public boolean enterable = false;

    /**
     * File/Directory permission: 'f' for renamable (itself)
     */
    public boolean renamable = false;

    /**
     * Directory permission: 'l' for listable
     */
    public boolean listable = false;

    /**
     * Directory permission: 'm' for can create dir (mkdir)
     */
    public boolean cancreatedirs = false;

    /**
     * Dir permission: 'p' for files subdirs can be deleted (purged)
     */
    public boolean canbepurged = false;

    /**
     * File permission: 'r' for readable
     */
    public boolean readable = false;

    /**
     * File permission: 'w' for writable
     */
    public boolean writable = false;

    public FtpPermissions(String str) {
        if (str == null)
            return;

        str = str.toLowerCase();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
            case 'a':
                appendable = true;
                break; // file
            case 'c':
                cancreatefiles = true;
                break; // dir
            case 'd':
                deletable = true;
                break; // dir
            case 'e':
                enterable = true;
                break; // file/dir
            case 'f':
                renamable = true;
                break; // file/dir
            case 'l':
                listable = true;
                break; // dir
            case 'm':
                cancreatedirs = true;
                break; // dir
            case 'p':
                canbepurged = true;
                break; // dir
            case 'r':
                readable = true;
                break; // file
            case 'w':
                writable = true;
                break; // file
            default:
                break; // ignore
            }

        }
    }

    public static FtpPermissions fromString(String str) {
        // Null in Null out, don't throw exceptions here: 
        if (str == null) {
            return null;
        }

        return new FtpPermissions(str);
    }

    public String toString() {
        return "" + ((appendable) ? "a" : "-") + ((cancreatefiles) ? "c" : "-") + ((deletable) ? "d" : "-")
                + ((enterable) ? "e" : "-") + ((renamable) ? "f" : "-") + ((listable) ? "l" : "-") + ((canbepurged) ? "p" : "-")
                + ((cancreatedirs) ? "m" : "-") + ((appendable) ? "r" : "-") + ((writable) ? "w" : "-");

    }

    /**
     * Return Posix compatible "writable" or "w" bit for these permissions. This means files must be writable or directories must
     * be able to add/create new entries.
     */
    public boolean isPosixWritable() {
        return (writable || (cancreatedirs && cancreatefiles));
    }

    /**
     * Return Posix compatible "readable" or "r" bit for these permissions. This means files must be 'readable' or directories
     * must be 'listable':
     */
    public boolean isPosixReadable() {
        return (readable || listable);
    }

    /**
     * Return Posix compatible "accessible" or directory "executable" or "x" bit. This means directories must be 'enterable' but
     * not listable. File executionable does not apply to distributed (grid) file systems.
     */
    public boolean isPosixAccessible() {
        return (enterable);
    }

}
