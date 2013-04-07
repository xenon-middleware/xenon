package nl.esciencecenter.octopus.files;

import java.io.Closeable;
import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public interface DirectoryStream<T> extends Closeable, Iterable<T> {

    public interface Filter {

        /**
         * Decides if the given directory entry should be accepted or filtered.
         */
        boolean accept(Path entry) throws OctopusException;
    }

    public Iterator<T> iterator();

    public void close() throws OctopusException;
}
