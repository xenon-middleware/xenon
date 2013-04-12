package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Iterator;
import java.util.Vector;

import nl.esciencecenter.octopus.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

import com.jcraft.jsch.ChannelSftp.LsEntry;

class SshDirectoryAttributeStream implements DirectoryStream<PathAttributesPair>, Iterator<PathAttributesPair> {
    private final DirectoryStream.Filter filter;
    private final AbsolutePath dir;
    private Vector<LsEntry> listing;

    private int current = 0;

    SshDirectoryAttributeStream(AbsolutePath dir, DirectoryStream.Filter filter, Vector<LsEntry> listing)
            throws OctopusIOException {
        this.dir = dir;
        this.filter = filter;
        this.listing = listing;
    }

    @Override
    public Iterator<PathAttributesPair> iterator() {
        return this;
    }

    @Override
    public void close() throws OctopusIOException {
        listing = null;
    }

    @Override
    public synchronized boolean hasNext() {
        return current < listing.size()-1;
    }

    @Override
    public synchronized PathAttributesPair next() {
        while (hasNext()) {
            LsEntry nextEntry = listing.get(current);
            current++;
            AbsolutePath nextPath = dir.resolve(new RelativePath(nextEntry.getLongname()));
            if (filter.accept(nextPath)) {
                SshFileAttributes attributes = new SshFileAttributes(nextEntry.getAttrs(), nextPath);
                PathAttributesPair next = new PathAttributesPairImplementation(nextPath, attributes);
                return next;
            }
        }
        return null;
    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException("SshDirectoryStream", "DirectoryStream iterator does not support remove");
    }
}