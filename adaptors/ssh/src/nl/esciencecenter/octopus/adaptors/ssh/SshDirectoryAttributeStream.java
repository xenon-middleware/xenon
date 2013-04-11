package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Iterator;
import java.util.Vector;

import nl.esciencecenter.octopus.engine.files.AbstractPathAttributes;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.RelativePath;

import com.jcraft.jsch.ChannelSftp.LsEntry;

class SshDirectoryAttributeStream implements DirectoryStream<PathAttributes>, Iterator<PathAttributes> {
    private final DirectoryStream.Filter filter;
    private final AbsolutePath dir;
    private Vector<LsEntry> listing;

    private int current = 0;

    SshDirectoryAttributeStream(AbsolutePath dir, DirectoryStream.Filter filter, Vector<LsEntry> listing) throws OctopusIOException {
        this.dir = dir;
        this.filter = filter;
        this.listing = listing;
    }

    @Override
    public Iterator<PathAttributes> iterator() {
        return this;
    }

    @Override
    public void close() throws OctopusIOException {
        listing = null;
    }

    @Override
    public synchronized boolean hasNext() {
        return current < listing.size();
    }
    
    public synchronized PathAttributes next() {
        while (current < listing.size()) {
            LsEntry nextEntry = listing.get(current);
            current++;
            AbsolutePath nextPath = dir.resolve(new RelativePath(listing.get(current).getLongname()));
            if (filter.accept(nextPath)) {
                SshFileAttributes attributes = new SshFileAttributes(nextEntry.getAttrs(), nextPath);
                PathAttributes next = new AbstractPathAttributes(nextPath, attributes);
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