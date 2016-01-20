package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatusResponse;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.generic.DirectoryStreamBase;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * Base class for webdav directory streams. These streams share some tricky code for getting the file name out of a response,
 * which is now stored in this base class.
 *
 * @author Christiaan Meijer
 *
 * @param <O>
 *            Type of the elements the stream returns.
 */
public abstract class WebdavDirectoryStreamBase<O> extends DirectoryStreamBase<MultiStatusResponse, O> {

    private static final String CURRENT_DIR_SYMBOL = ".";

    public WebdavDirectoryStreamBase(Path dir, Filter filter, List<MultiStatusResponse> listing) throws XenonException {
        super(dir, filter, listing);
    }

    @Override
    protected String getFileNameFromEntry(MultiStatusResponse entry, Path parentPath) {
        RelativePath entryPath = new RelativePath(entry.getHref());
        RelativePath displacement = parentPath.getRelativePath().relativize(entryPath);
        return displacement.isEmpty() ? CURRENT_DIR_SYMBOL : entryPath.getFileNameAsString();
    }

}