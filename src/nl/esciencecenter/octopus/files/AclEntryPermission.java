package nl.esciencecenter.octopus.files;

public enum AclEntryPermission {

    /**
     * Permission to append data to a file.
     */
    APPEND_DATA,
    /**
     * Permission to delete the file.
     */
    DELETE,
    /**
     * Permission to delete a file or directory within a directory.
     */
    DELETE_CHILD,
    /**
     * Permission to execute a file.
     */
    EXECUTE,
    /**
     * Permission to read the ACL attribute.
     */
    READ_ACL,
    /**
     * The ability to read (non-acl) file attributes.
     */
    READ_ATTRIBUTES,
    /**
     * Permission to read the data of the file.
     */
    READ_DATA,
    /**
     * Permission to read the named attributes of a file.
     */
    READ_NAMED_ATTRS,
    /**
     * Permission to access file locally at the server with synchronous reads
     * and writes.
     */
    SYNCHRONIZE,
    /**
     * Permission to write the ACL attribute.
     */
    WRITE_ACL,
    /**
     * The ability to write (non-acl) file attributes.
     */
    WRITE_ATTRIBUTES,

    /**
     * Permission to modify the file's data.
     */
    WRITE_DATA,
    /**
     * Permission to write the named attributes of a file.
     */
    WRITE_NAMED_ATTRS,

    /**
     * Permission to change the owner.
     */
    WRITE_OWNER,

}
