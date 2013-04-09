package nl.esciencecenter.octopus.files;

public enum AclEntryFlag {

    /**
     * Can be placed on a directory and indicates that the ACL entry should be added to each new directory created.
     */
    DIRECTORY_INHERIT,

    /**
     * Can be placed on a directory and indicates that the ACL entry should be added to each new non-directory file created.
     */
    FILE_INHERIT,
    /**
     * Can be placed on a directory but does not apply to the directory, only to newly created files/directories as specified by
     * the FILE_INHERIT and DIRECTORY_INHERIT flags.
     */
    INHERIT_ONLY,
    /**
     * Can be placed on a directory to indicate that the ACL entry should not be placed on the newly created directory which is
     * inheritable by subdirectories of the created directory.
     */
    NO_PROPAGATE_INHERIT,

}
