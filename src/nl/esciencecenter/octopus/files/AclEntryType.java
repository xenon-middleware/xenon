package nl.esciencecenter.octopus.files;

public enum AclEntryType {

    /**
     * Generate an alarm, in a system dependent way, the access specified in the
     * permissions component of the ACL entry.
     */
    ALARM,

    /**
     * Explicitly grants access to a file or directory.
     */
    ALLOW,
    /**
     * Log, in a system dependent way, the access specified in the permissions
     * component of the ACL entry.
     */
    AUDIT,
    /**
     * Explicitly denies access to a file or directory.
     */
    DENY

}
