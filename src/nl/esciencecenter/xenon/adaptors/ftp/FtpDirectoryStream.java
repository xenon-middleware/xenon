package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.List;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.generic.DirectoryStreamBase;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPFile;

public class FtpDirectoryStream extends DirectoryStreamBase<FTPFile, Path> {

    public FtpDirectoryStream(Path dir, nl.esciencecenter.xenon.files.DirectoryStream.Filter filter, List<FTPFile> listing)
            throws XenonException {
        super(dir, filter, listing);
    }

    @Override
    protected Path getStreamElementFromEntry(FTPFile entry, Path entryPath) {
        return entryPath;
    }

    @Override
    protected String getFileNameFromEntry(FTPFile entry) {
        return entry.getName();
    }

}