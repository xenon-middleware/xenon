package nl.esciencecenter.xenon.adaptors.webdav;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.generic.DirectoryStreamBase;
import nl.esciencecenter.xenon.files.Path;

import org.apache.jackrabbit.webdav.MultiStatusResponse;

public class WebdavDirectoryStream extends DirectoryStreamBase<MultiStatusResponse, Path> {
    public WebdavDirectoryStream(Path dir, nl.esciencecenter.xenon.files.DirectoryStream.Filter filter,
            List<MultiStatusResponse> listing) throws XenonException {
        super(dir, filter, listing);
    }

    @Override
    protected Path getStreamElementFromEntry(MultiStatusResponse entry, Path entryPath) throws XenonException {
        return entryPath;
    }

    @Override
    protected String getFileNameFromEntry(MultiStatusResponse entry, Path parentPath) {
        String path = entry.getHref();
        List<String> entryElements = clean(path);
        List<String> parentElements = clean(parentPath.toString());
        if (isSame(entryElements, parentElements)) {
            return ".";
        }
        return Paths.get(path).getFileName().toString();
    }

    public static boolean isSame(List<String> entryElements, List<String> parentElements) {
        if (entryElements.size() > parentElements.size()) {
            return false;
        }

        return isTailOf(entryElements, parentElements);
    }

    private static boolean isTailOf(List<String> tailElements, List<String> parentElements) {
        LinkedList<String> tail = new LinkedList<String>(tailElements);
        LinkedList<String> parent = new LinkedList<String>(parentElements);
        while (!tail.isEmpty()) {
            if (!tail.pollLast().equals(parent.pollLast())) {
                return false;
            }
        }
        return true;
    }

    public static List<String> clean(String path) {
        String tail = path.substring(path.lastIndexOf("//") + 1).replaceAll("\\\\$", "");
        String[] elements = tail.split("/");
        List<String> list = new LinkedList<String>(Arrays.asList(elements));
        list.removeAll(Collections.singleton(""));
        return list;
    }
}
