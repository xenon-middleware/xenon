package nl.esciencecenter.xenon.util;


import java.io.IOException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidOptionsException;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;

public class FileSystemUtil {
	
    /**
     * Recursively removes all directories, files and symbolic links in path.
     *
     * @param filesytem
     *            the files interface to use for file access.
     * @param path
     *            the path to delete.
     * 
     * @throws XenonException
     *             if an I/O error occurs during the copying
     */
    public static void recursiveDelete(FileSystem filesytem, Path path) throws XenonException {
        FileAttributes att = filesytem.getAttributes(path);

        if (att.isDirectory()) {

            DirectoryStream<Path> stream = null;

            try {             
                stream = filesytem.newDirectoryStream(path);

                for (Path f : stream) {
                	recursiveDelete(filesytem, f);
                }
            } finally { 
                if (stream != null) { 
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // TODO: ignored for now, should LOG
                    }
                }
            }
        }
        filesytem.delete(path);
    }

    /**
     * Recursively copies directories, files and symbolic links from source to target.
     *
     * @param sourceFS
     *            the files interface to use for file access.
     * @param source
     *            the path to copy from.
     * @param target
     *            the path to copy to.
     * @param options
     *            the options to use while copying. See {@link CopyOption} for details.
     *
     * @throws InvalidOptionsException
     *             if an invalid combination of options is used.
     * @throws XenonException
     *             if an I/O error occurs during the copying
     */
    @SuppressWarnings("PMD.EmptyIfStmt")
    public static void recursiveCopy(FileSystem sourceFS, Path source, FileSystem targetFS, Path target, CopyOption option) throws XenonException {

        boolean exist = sourceFS.exists(target);
        boolean replace = CopyOption.REPLACE.equals(option);
        boolean ignore = CopyOption.IGNORE.equals(option);

        if (replace && ignore) {
            throw new InvalidOptionsException(targetFS.getAdaptorName(), "Can not replace and ignore existing files at the same time");
        }

        FileAttributes att = sourceFS.getAttributes(source);
        boolean srcIsDir = att.isDirectory();

        if (srcIsDir) {
            if (exist) {
                if (ignore) {
                    // do nothing as requested
                } else if (replace) {
                    // keep existing directory
                    // Can not replace directory, to replace have to do recursive delete and createDirectories
                    // because recursive delete can delete unwanted files
                } else {
                    throw new PathAlreadyExistsException(targetFS.getAdaptorName(), "Target " + target
                            + " already exists!");
                }
            } else {
                sourceFS.createDirectories(target);
            }
            
            DirectoryStream<Path> stream  = null;

            try { 
                stream = sourceFS.newDirectoryStream(source);

                for (Path f : stream) {
                        Path ftarget = target.resolve(f.getFileName());
                    recursiveCopy(sourceFS, f, targetFS, ftarget, option);
                }
            } finally { 
                if (stream != null) { 
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // TODO: ignored for now -- should LOG ?
                    }
                } 
            }
                
        } else {
            if (exist) {
                if (ignore) {
                    // do nothing as requested
                } else if (replace) {
                    sourceFS.copy(new CopyDescription(sourceFS, source, targetFS, target, CopyOption.REPLACE));
                } else {
                    throw new PathAlreadyExistsException(targetFS.getAdaptorName(), "Target " + target
                            + " already exists!");
                }
            } else {
                sourceFS.copy(new CopyDescription(sourceFS, source, targetFS, target, CopyOption.CREATE));
            }
        }
    }
    
    /**
     * Walks over a file tree.
     *
     * <p>
     * This method is equivalent to invoking {@link #walkFileTree(Files, Path, boolean, int, FileVisitor) walkFileTree(files,
     * start, false, Integer.MAX_VALUE, visitor}.
     * </p>
     *
     * @param fs
     *            the files interface to use for file access.
     * @param start
     *            the path to start from.
     * @param visitor
     *            a {@link FileVisitor} that will be invoked for every {@link Path} encountered during the walk.
     * @throws XenonException
     *             if an I/O error occurs during the walk.
     */
    public static void walkFileTree(FileSystem fs, Path start, FileVisitor visitor) throws XenonException {
        walkFileTree(fs, start, false, Integer.MAX_VALUE, visitor);
    }

    /**
     * Walks a file tree.
     *
     * <p>
     * This method walks over a file tree, starting at <code>start</code> and then recursively applying the following steps:
     * </p>
     * <ul>
     * <li>
     * If the current path is a file, it is forwarded to {@link FileVisitor#visitFile(Files, Path, FileAttributes)
     * visitor.visitFile} and the result of this call will be returned.</li>
     * <li>
     * If the current path is a link and <code>followLinks</code> is <code>true</code> the link is followed,
     * <code>walkFileTree</code> is called on the target of the link, and the result of this call is returned.</li>
     * <li>
     * If the current path is a link and <code>followLinks</code> is <code>false</code> the link is not followed. Instead the link
     * itself is forwarded to {@link FileVisitor#visitFile(Files, Path, FileAttributes) visitor.visitFile} and the result of this
     * call is returned.</li>
     * <li>
     * If the current path is a directory and the current distance from <code>start</code> is more than <code>maxDepth</code>, the
     * directory is forwarded to {@link FileVisitor#visitFile(Files, Path, FileAttributes) visitor.visitFile} and the result of
     * this call is returned.</li>
     * <li>
     * If the current path is a directory and the current distance from <code>start</code> is less or equal to
     * <code>maxDepth</code> the path is forwarded to {@link FileVisitor#preVisitDirectory(Files, Path, FileAttributes)
     * visitor.preVisitDirectory}. The subsequent behavior then depends on the result of this call:
     * <ul>
     * <li>
     * If {@link FileVisitResult#TERMINATE} is returned, the walk is terminated immediately and <code>walkFileTree</code> returns
     * {@link FileVisitResult#TERMINATE}.</li>
     * <li>
     * If {@link FileVisitResult#SKIP_SUBTREE} is returned, the elements in the directory will not be visited. Instead
     * {@link FileVisitor#postVisitDirectory(Files, Path, XenonException) visitor.postVisitDirectory} and
     * {@link FileVisitResult#CONTINUE} is returned.</li>
     * <li>
     * If {@link FileVisitResult#SKIP_SIBLINGS} is returned, the elements in the directory will not be visited and
     * {@link FileVisitResult#SKIP_SIBLINGS} is returned immediately.</li>
     * <li>
     * If {@link FileVisitResult#CONTINUE} is returned <code>walkFileTree</code> is called on each of the elements in the
     * directory. If any of these calls returns {@link FileVisitResult#SKIP_SIBLINGS} the remaining elements will be skipped,
     * {@link FileVisitor#postVisitDirectory(Files, Path, XenonException) visitor.postVisitDirectory} will be called, and its
     * result will be returned. If any of these calls returns {@link FileVisitResult#TERMINATE} the walk is terminated immediately
     * and {@link FileVisitResult#TERMINATE} is returned.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param fs
     *            the files interface to use for file access.
     * @param start
     *            the path to start from.
     * @param followLinks
     *            should links be followed ?
     * @param maxDepth
     *            the maximum distance from the start to walk to.
     * @param visitor
     *            a {@link FileVisitor} that will be invoked for every {@link Path} encountered during the walk.
     * 
     * @throws XenonException
     *             if an I/O error occurs during the walk.
     */
    public static void walkFileTree(FileSystem fs, Path start, boolean followLinks, int maxDepth, FileVisitor visitor)
            throws XenonException {
        FileAttributes attributes = fs.getAttributes(start);
        walk(fs, start, attributes, followLinks, maxDepth, visitor);
    }

    // Walk a file tree.
    private static FileVisitResult walk(FileSystem fs, Path path, FileAttributes attributes, boolean followLinks, int maxDepth,
            FileVisitor visitor) throws XenonException {
        FileVisitResult visitResult;
        XenonException exception = null;

        try {
            if (attributes.isDirectory() && maxDepth > 0) {
                visitResult = visitor.preVisitDirectory(fs, path, attributes);
                if (visitResult == FileVisitResult.CONTINUE) {
                    
                    DirectoryStream<PathAttributesPair> stream = null;
                    
                    try {
                        stream = fs.newAttributesDirectoryStream(path); 
                       
                        for (PathAttributesPair attributesEntry : stream) {
                            // recursion step
                            FileVisitResult result = walk(fs, attributesEntry.path(), attributesEntry.attributes(),
                                    followLinks, maxDepth - 1, visitor);

                            if (result == FileVisitResult.SKIP_SIBLINGS) {
                                // stop handling entries in this directory`
                                break;
                            } else if (result == FileVisitResult.TERMINATE) {
                                return FileVisitResult.TERMINATE;
                            }
                        }
                    } catch (XenonException e) {
                        exception = e;
                    } finally { 
                        if (stream != null) { 
                            try {
                                stream.close();
                            } catch (IOException e) {
                                // TODO: ignored for now, should log ?  
                            }
                        }
                    }
                    return visitor.postVisitDirectory(fs, path, exception);
                } else if (visitResult == FileVisitResult.SKIP_SIBLINGS) {
                    // skip entries, skip post-visit, skip siblings
                    return FileVisitResult.SKIP_SIBLINGS;
                } else if (visitResult == FileVisitResult.SKIP_SUBTREE) {
                    // skip visiting entries
                    visitor.postVisitDirectory(fs, path, null);
                    return FileVisitResult.CONTINUE;
                } else {
                    // TERMINATE
                    return FileVisitResult.TERMINATE;
                }
            } else if (attributes.isSymbolicLink()) {
                if (followLinks) {
                    Path target = fs.readSymbolicLink(path);
                    return walk(fs, target, fs.getAttributes(target), followLinks, maxDepth - 1, visitor);
                } else {
                    // visit the link itself
                    return visitor.visitFile(fs, path, attributes);
                }
            } else {
                return visitor.visitFile(fs, path, attributes);
            }
        } catch (XenonException e) {
            return visitor.visitFileFailed(fs, path, e);
        }
    }
}
