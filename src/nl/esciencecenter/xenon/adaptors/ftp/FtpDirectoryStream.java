package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPFile;

public class FtpDirectoryStream implements DirectoryStream<Path>, Iterator<Path> {

    private final Deque<Path> stream;

    FtpDirectoryStream(Path dir, DirectoryStream.Filter filter, FTPFile[] ftpFiles) throws XenonException {

        stream = new LinkedList<Path>();

        for (FTPFile e : ftpFiles) {

            String filename = e.getName();

            Path tmp = new PathImplementation(dir.getFileSystem(), dir.getRelativePath().resolve(filename));

            if (filename.equals(".") || filename.equals("..")) {
                // filter out the "." and ".."
            } else {
                if (filter.accept(tmp)) {
                    stream.add(tmp);
                }
            }
        }
    }

    @Override
    public Iterator<Path> iterator() {
        return this;
    }

    @Override
    public synchronized void close() throws IOException {
        stream.clear();
    }

    @Override
    public synchronized boolean hasNext() {
        return (stream.size() > 0);
    }

    @Override
    public synchronized Path next() {

        if (stream.size() > 0) {
            return stream.removeFirst();
        }

        throw new NoSuchElementException("No more files in directory");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("DirectoryStream iterator does not support remove");
    }
}