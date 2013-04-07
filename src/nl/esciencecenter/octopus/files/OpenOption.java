package nl.esciencecenter.octopus.files;

public enum OpenOption {

    /**
     * If the file is opened for WRITE access then bytes will be written to the
     * end of the file rather than the beginning.
     */
    APPEND,
    
    /**
     * Create a new file if it does not exist.
     */
    CREATE,

    /**
     * Create a new file, failing if the file already exists.
     */
    CREATE_NEW,

    /**
     * Open for read access.
     */
    READ,

    /**
     * If the file already exists and it is opened for WRITE access, then its
     * length is truncated to 0.
     */
    TRUNCATE_EXISTING,

    /**
     * Open for write access.
     */
    WRITE,

}
