package nl.esciencecenter.octopus.util;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Path;

public interface FileVisitor {

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     */
    FileVisitResult postVisitDirectory(Path dir, OctopusIOException exception, Octopus octopus) throws OctopusIOException;

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Octopus octopus) throws OctopusIOException;

    /**
     * Invoked for a file in a directory.
     */
    FileVisitResult visitFile(Path file, FileAttributes attributes, Octopus octopus) throws OctopusIOException;

    /**
     * Invoked for a file that could not be visited.
     */
    FileVisitResult visitFileFailed(Path file, OctopusIOException exception, Octopus octopus) throws OctopusIOException;
}
