package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatusResponse;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.generic.DirectoryStreamBase;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

public class WebdavDirectoryStream extends DirectoryStreamBase<MultiStatusResponse, Path> {
    public WebdavDirectoryStream(Path dir, Filter filter, List<MultiStatusResponse> listing) throws XenonException {
        super(dir, filter, listing);
    }

    @Override
    protected Path getStreamElementFromEntry(MultiStatusResponse entry, Path entryPath) throws XenonException {
        return entryPath;
    }

    @Override
    protected String getFileNameFromEntry(MultiStatusResponse entry, Path parentPath) {
        RelativePath entryPath = new RelativePath(entry.getHref());
        RelativePath displacement = parentPath.getRelativePath().relativize(entryPath);
        return displacement.isEmpty() ? "." : entryPath.getFileNameAsString();
    }
}
