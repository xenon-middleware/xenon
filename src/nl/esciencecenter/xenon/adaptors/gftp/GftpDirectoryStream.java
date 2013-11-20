package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.Path;

import org.globus.ftp.MlsxEntry;

public class GftpDirectoryStream implements DirectoryStream<Path>, Iterator<Path> {

    private final Deque<Path> stream;

    public GftpDirectoryStream(Path dir, DirectoryStream.Filter filter, List<MlsxEntry> entries) throws XenonException {

        stream = new LinkedList<Path>();

        for (MlsxEntry e : entries) {

            String basename = GftpUtil.basename(e.getFileName());

            if (basename.equals(".") || basename.equals("..")) {
                // filter out the "." and ".."
            } else {
                Path tmp = new PathImplementation(dir.getFileSystem(), dir.getRelativePath().resolve(basename));

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
