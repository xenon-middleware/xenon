package nl.esciencecenter.octopus.files;

import java.io.Closeable;
import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public interface DirectoryStream<T> extends Closeable, Iterable<T> {

    public interface Filter {

        /**
         * Decides if the given directory entry should be accepted or filtered.
         */
        boolean accept(AbsolutePath entry);
    }

    public Iterator<T> iterator();

    public void close() throws OctopusIOException;
}
