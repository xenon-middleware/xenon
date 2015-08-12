package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.List;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.generic.DirectoryStreamBase;
import nl.esciencecenter.xenon.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;

import org.apache.commons.net.ftp.FTPFile;

public class FtpDirectoryAttributeStream extends DirectoryStreamBase<FTPFile, PathAttributesPair> {

    FtpDirectoryAttributeStream(Path dir, nl.esciencecenter.xenon.files.DirectoryStream.Filter filter, List<FTPFile> listing)
            throws XenonException {
        super(dir, filter, listing);
    }

    @Override
    protected PathAttributesPair getStreamElementFromEntry(FTPFile entry, Path entryPath) throws XenonException {
        return new PathAttributesPairImplementation(entryPath, new FtpFileAttributes(entry));
    }

    @Override
    protected String getFileNameFromEntry(FTPFile entry, Path parentPath) {
        return entry.getName();
    }

}
