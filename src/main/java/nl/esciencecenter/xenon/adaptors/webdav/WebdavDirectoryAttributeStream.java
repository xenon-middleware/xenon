package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatusResponse;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;

public class WebdavDirectoryAttributeStream extends WebdavDirectoryStreamBase<PathAttributesPair> {

    WebdavDirectoryAttributeStream(Path dir, nl.esciencecenter.xenon.files.DirectoryStream.Filter filter,
            List<MultiStatusResponse> list) throws XenonException {
        super(dir, filter, list);
    }

    @Override
    protected PathAttributesPair getStreamElementFromEntry(MultiStatusResponse entry, Path entryPath) throws XenonException {
        return new PathAttributesPairImplementation(entryPath, new WebdavFileAttributes(entry.getProperties(WebdavFiles.OK)));
    }
}
