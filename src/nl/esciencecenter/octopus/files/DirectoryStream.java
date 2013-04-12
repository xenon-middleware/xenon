package nl.esciencecenter.octopus.files;

import java.io.Closeable;
import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * DirectoryStream represents an Iterable set of elements. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * @param <T>
 */
public interface DirectoryStream<T> extends Closeable, Iterable<T> {

    /** 
     * A filter use to decides if the given directory entry should be accepted. 
     */
    public interface Filter {

        /**
         * Decide if the AbsolutePath entry should be accepted.
         * 
         * @param entry the AbsolutePath to test.
         * 
         * @return if the AbsolutePath entry should be accepted.
         */
        boolean accept(AbsolutePath entry);
    }

    public Iterator<T> iterator();

    public void close() throws OctopusIOException;
}
