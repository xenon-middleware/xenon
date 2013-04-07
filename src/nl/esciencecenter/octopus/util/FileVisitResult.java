package nl.esciencecenter.octopus.util;

public enum FileVisitResult {

    /**
     * Continue.
     */
    CONTINUE,

    /**
     * Continue without visiting the siblings of this file or directory.
     */
    SKIP_SIBLINGS,

    /**
     * Continue without visiting the entries in this directory.
     */
    SKIP_SUBTREE,

    /**
     * Terminate.
     */
    TERMINATE,

}
