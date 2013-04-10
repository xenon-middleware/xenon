package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;

import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryIteratorException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.Path;

import com.jcraft.jsch.ChannelSftp.LsEntry;

class SshDirectoryStream implements DirectoryStream<Path>, Iterator<Path> {
    private final DirectoryStream.Filter filter;
    private final Path dir;
    private Vector<LsEntry> listing;

    private int current = 0;
    
    SshDirectoryStream(Path dir, DirectoryStream.Filter filter, Vector<LsEntry> listing) throws OctopusIOException {
            this.dir = dir;
            this.filter = filter;
            this.listing = listing;
    }

    @Override
    public Iterator<Path> iterator() {
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

    @Override
    public synchronized Path next() {
            while (current < listing.size()) {
                Path next;
                try {
                    next = new PathImplementation(null, new URI(listing.get(current).getLongname()), "ssh", null);
                } catch (URISyntaxException e) {
                     throw new OctopusRuntimeException("ssh", e.getMessage(), e);
                }
                current++;
                
                try {
                    if (filter.accept(next)) {
                        return next;
                    }
                } catch (OctopusIOException e) {
                    // TODO exception will be removed
                    e.printStackTrace();
                }
            }
            return null;
    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException("SshDirectoryStream", "DirectoryStream iterator does not support remove");
    }
}