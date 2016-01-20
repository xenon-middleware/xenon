package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatusResponse;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

public class WebdavDirectoryStream extends WebdavDirectoryStreamBase<Path> {
    public WebdavDirectoryStream(Path dir, Filter filter, List<MultiStatusResponse> listing) throws XenonException {
        super(dir, filter, listing);
    }

    @Override
    protected Path getStreamElementFromEntry(MultiStatusResponse entry, Path entryPath) throws XenonException {
        return entryPath;
    }

}
