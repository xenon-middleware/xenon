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
import nl.esciencecenter.octopus.files.PathAttributes;

import com.jcraft.jsch.ChannelSftp.LsEntry;

class SshDirectoryAttributeStream implements DirectoryStream<PathAttributes>, Iterator<PathAttributes> {
    private final DirectoryStream.Filter filter;
    private final Path dir;
    private Vector<LsEntry> listing;

    private int current = 0;

    SshDirectoryAttributeStream(Path dir, DirectoryStream.Filter filter, Vector<LsEntry> listing) throws OctopusIOException {
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

    @Override
    public synchronized PathAttributes next() {
        while (current < listing.size()) {
            try {
                LsEntry nextEntry = listing.get(current);
                current++;
                Path nextPath = new PathImplementation(null, new URI(nextEntry.getLongname()), "ssh", null);
                PathAttributes next = SshFiles.convertAttributes(nextEntry);

                if (filter.accept(nextPath)) {
                    return next;
                }
            } catch (OctopusIOException e) {
                // TODO exception will be removed
                e.printStackTrace();
            } catch (URISyntaxException e) {
                throw new OctopusRuntimeException("ssh", e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public synchronized void remove() {
        throw new DirectoryIteratorException("SshDirectoryStream", "DirectoryStream iterator does not support remove");
    }
}