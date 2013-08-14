/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.octopus.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * Various file utilities implemented on top of the Octopus API.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0
 */
public final class FileUtils {

    private static final String NAME = "FileUtils";
    
    public static final int BUFFER_SIZE = 10240;

    private FileUtils() { 
        // DO NOT USE
    }
    
    private static void close(Closeable c) {

        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // ignored!
        }
    }

    private static OpenOption[] openOptionsForWrite(boolean truncate) {
        if (truncate) {
            return new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.TRUNCATE };
        } else {
            return new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.APPEND };
        }
    }

    /**
     * Copies all bytes from an input stream to a file.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param in the 
     *          {@link java.util.InputStream} to read from.
     * @param target 
     *          the file to write to.
     * @param truncate 
     *          should the file be truncated before writing data into it ?
     * 
     * @return the number of bytes copied.
     * 
     * @throws OctopusException
     *             if an I/O error occurs when reading or writing
     * @throws FileAlreadyExistsException
     *             if the target file exists but cannot be replaced because the {@code REPLACE_EXISTING} option is not specified
     *             <i>(optional specific exception)</i>
     * @throws DirectoryNotEmptyException
     *             the {@code REPLACE_EXISTING} option is specified but the file cannot be replaced because it is a non-empty
     *             directory <i>(optional specific exception)</i> *
     * @throws UnsupportedOperationException
     *             if {@code options} contains a copy option that is not supported
     */
    public static long copy(Files files, InputStream in, AbsolutePath target, boolean truncate) throws OctopusException {

        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = 0;

        OutputStream out = null;

        try {
            out = files.newOutputStream(target, openOptionsForWrite(truncate));

            while (true) {
                int read = in.read(buffer);

                if (read == -1) {
                    out.close();
                    return totalBytes;
                }

                out.write(buffer, 0, read);
                totalBytes += read;
            }
        } catch (IOException e) {
            close(out);
            throw new OctopusException(NAME, "Failed to copy stream to file.", e);
        }
    }

    /**
     * Copies all bytes from a file to an output stream.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param source
     *          the file to read from.
     * @param out  
     *          the {@link java.util.OutputStream} to write to.
     * 
     * @return the number of bytes copied.
     * 
     * @throws OctopusException
     *             if an I/O error occurs while reading or writing
     * 
     */
    public static long copy(Files files, AbsolutePath source, OutputStream out) throws OctopusException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = 0;

        InputStream in = null;

        try {
            in = files.newInputStream(source);

            while (true) {
                int read = in.read(buffer);

                if (read == -1) {
                    in.close();
                    return totalBytes;
                }
                out.write(buffer, 0, read);
                totalBytes += read;
            }
        } catch (IOException e) {
            close(in);
            throw new OctopusException(NAME, "Failed to copy stream to file.", e);
        }
    }

    /**
     * Opens a file for reading, returning a {@link java.util.BufferedReader} that may be used to read text from the file in an 
     * efficient manner.
     * 
     * @param files
     *          the files interface to use for file access. 
     * @param source
     *          the file to read from.
     * @param cs
     *          the Charset to use. 
     *
     * @return the BufferedReader.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while opening or reading the file.
     */
    public static BufferedReader newBufferedReader(Files files, AbsolutePath source, Charset cs) throws OctopusIOException {
        return new BufferedReader(new InputStreamReader(files.newInputStream(source), cs));
    }

    /**
     * Opens or creates a file for writing, returning a BufferedWriter that may be used to write text to the file in an efficient
     * manner.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param target
     *          the file to write to.
     * @param cs
     *          the Charset to use. 
     * @param truncate
     *          should the file be truncated before writing data into it ?
     *          
     * @return the BufferedWriter.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while opening or writing the file.
     */
    public static BufferedWriter newBufferedWriter(Files files, AbsolutePath target, Charset cs, boolean truncate)
            throws OctopusIOException {

        OutputStream out = files.newOutputStream(target, openOptionsForWrite(truncate));
        return new BufferedWriter(new OutputStreamWriter(out, cs));
    }

    /**
     * Read all the bytes from a file and return them as a <code>byte[]<\code>.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param source
     *          the file to read from.
     *          
     * @return a <code>byte[]</code> containing all bytes in the file.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while opening or reading the file.
     */
    public static byte[] readAllBytes(Files files, AbsolutePath source) throws OctopusException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(files, source, out);
        return out.toByteArray();
    }

    /**
     * Read all lines from a file and return them in a {@link java.util.List}.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param source
     *          the file to read from.
     * @param cs
     *          the Charset to use.
     *           
     * @return a <code>List<String></code> containing all lines in the file.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while opening or reading the file.
     */
    public static List<String> readAllLines(Files files, AbsolutePath source, Charset cs) throws OctopusIOException {

        ArrayList<String> result = new ArrayList<String>();

        BufferedReader reader = null;

        try {
            reader = newBufferedReader(files, source, cs);

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    reader.close();
                    return result;
                }

                result.add(line);
            }
        } catch (IOException e) {
            close(reader);
            throw new OctopusIOException("FileUtils", "failed to read lines", e);
        }
    }

    /**
     * Writes bytes to a file.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param target
     *          the file to write to.
     * @param bytes
     *          the bytes to write to the file.
     * @param truncate
     *          should the file be truncated before writing data into it ?
     *          
     * @throws OctopusException
     *          if an I/O error occurs while opening or writing to the file.
     */
    public static void write(Files files, AbsolutePath target, byte[] bytes, boolean truncate) throws OctopusException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        copy(files, in, target, truncate);
        close(in);
    }

    /**
     * Write lines of text to a file.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param target
     *          the file to write to.
     * @param lines
     *          the text to write to the file. 
     * @param cs
     *          the Charset to use.
     * @param truncate
     *          should the file be truncated before writing data into it ?
     *          
     * @throws OctopusIOException
     *          if an I/O error occurs while opening or writing to the file.
     */
    public static void write(Files files, AbsolutePath target, Iterable<? extends CharSequence> lines, Charset cs,
            boolean truncate) throws OctopusIOException {

        BufferedWriter writer = null;

        try {
            writer = newBufferedWriter(files, target, cs, truncate);

            for (CharSequence line : lines) {
                writer.write(line.toString());
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            close(writer);
            throw new OctopusIOException("FileUtils", "failed to write lines", e);
        }
    }

    /**
     * Walks over a file tree. 
     * 
     * <p>
     * This method is equivalent to invoking {@link #walkFileTree(Files, AbsolutePath, boolean, int, FileVisitor) 
     * walkFileTree(files, start, false, Integer.MAX_VALUE, visitor}. 
     * </p>
     * 
     * @param files
     *          the files interface to use for file access. 
     * @param start
     *          the path to start from.
     * @param visitor
     *          a {@link FileVisitor} that will be invoked for every {@link AbsolutePath} encountered during the walk.
     * @throws OctopusIOException
     *          if an I/O error occurs during the walk.
     */
    public static void walkFileTree(Files files, AbsolutePath start, FileVisitor visitor) throws OctopusIOException {
        walkFileTree(files, start, false, Integer.MAX_VALUE, visitor);
    }

    /**
     * Walks a file tree.
     *
     * <p>
     * This method walks over a file tree, starting at <code>start</code> and then recursively applying the following steps:  
     * </p>
     * <ul>
     * <li> 
     * If the current path is a file, it is forwarded to 
     * {@link FileVisitor#visitFile(AbsolutePath, FileAttributes, Files) visitor.visitFile} and the result of this call will be 
     * returned.
     * </li>
     * <li>
     * If the current path is a link and <code>followLinks</code> is <code>true</code> the link is followed,  
     * <code>walkFileTree</code> is called on the target of the link, and the result of this call is returned.
     * </li>
     * <li> 
     * If the current path is a link and <code>followLinks</code> is <code>false</code> the link is not followed. Instead the 
     * link itself is forwarded to {@link FileVisitor#visitFile(AbsolutePath, FileAttributes, Files) visitor.visitFile} and the
     * result of this call is returned.
     * </li>
     * <li>
     * If the current path is a directory and the current distance from <code>start</code> is more than <code>maxDepth</code>, 
     * the directory is forwarded to {@link FileVisitor#visitFile(AbsolutePath, FileAttributes, Files) visitor.visitFile} and 
     * the result of this call is returned.
     * </li>
     * <li>
     * If the current path is a directory and the current distance from <code>start</code> is less or equal to 
     * <code>maxDepth</code> the path is forwarded to 
     * {@link FileVisitor#preVisitDirectory(AbsolutePath, FileAttributes, Files) visitor.preVisitDirectory}. The subsequent 
     * behavior then depends on the result of this call:
     * <ul>
     * <li> 
     * If {@link FileVisitResult#TERMINATE} is returned, the walk is terminated immediately and <code>walkFileTree</code> returns 
     * {@link FileVisitResult#TERMINATE}.
     * </li>
     * <li> 
     * If {@link FileVisitResult#SKIP_SUBTREE} is returned, the elements in the directory will not be visited. Instead 
     * {@link FileVisitor#postVisitDirectory(AbsolutePath, OctopusIOException, Files) visitor.postVisitDirectory} and  
     * {@link FileVisitResult#CONTINUE} is returned.
     * </li>
     * <li> 
     * If {@link FileVisitResult#SKIP_SIBLINGS} is returned, the elements in the directory will not be visited and 
     * {@link FileVisitResult#SKIP_SIBLINGS} is returned immediately.
     * </li>
     * <li> 
     * If {@link FileVisitResult#CONTINUE} is returned <code>walkFileTree</code> is called on each of the elements 
     * in the directory.
     * If any of these calls returns {@link FileVisitResult#SKIP_SIBLINGS} the remaining elements will be 
     * skipped, {@link FileVisitor#postVisitDirectory(AbsolutePath, OctopusIOException, Files) visitor.postVisitDirectory} will be 
     * called, and its result will be returned.  
     * If any of these calls returns {@link FileVisitResult#TERMINATE} the walk is terminated immediately and 
     * {@link FileVisitResult#TERMINATE} is returned.
     * </li>
     * </ul>
     * </li>   
     * </ul>
     * 
     * @param files
     *          the files interface to use for file access. 
     * @param start
     *          the path to start from.
     * @param followLinks
     *          should links be followed ?
     * @param maxDepth
     *          the maximum distance from the start to walk to. 
     * @param visitor
     *          a {@link FileVisitor} that will be invoked for every {@link AbsolutePath} encountered during the walk.
     *          
     * @throws OctopusIOException
     *          if an I/O error occurs during the walk.
     */
    public static void walkFileTree(Files files, AbsolutePath start, boolean followLinks, int maxDepth,
            FileVisitor visitor) throws OctopusIOException {
        FileAttributes attributes = files.getAttributes(start);
        walk(files, start, attributes, followLinks, maxDepth, visitor);
    }

    // Walk a file tree.
    private static FileVisitResult walk(Files files, AbsolutePath path, FileAttributes attributes, boolean followLinks,
            int maxDepth, FileVisitor visitor) throws OctopusIOException {
        FileVisitResult visitResult;
        OctopusIOException exception = null;

        try {
            if (attributes.isDirectory() && maxDepth > 0) {
                visitResult = visitor.preVisitDirectory(path, attributes, files);
                if (visitResult == FileVisitResult.CONTINUE) {
                    try {
                        for (PathAttributesPair attributesEntry : files.newAttributesDirectoryStream(path)) {
                            // recursion step
                            FileVisitResult result = walk(files, attributesEntry.path(), attributesEntry.attributes(),
                                    followLinks, maxDepth - 1, visitor);

                            if (result == FileVisitResult.SKIP_SIBLINGS) {
                                // stop handling entries in this directory`
                                break;
                            } else if (result == FileVisitResult.TERMINATE) {
                                return FileVisitResult.TERMINATE;
                            }
                        }
                    } catch (OctopusIOException e) {
                        exception = e;
                    }
                    return visitor.postVisitDirectory(path, exception, files);
                } else if (visitResult == FileVisitResult.SKIP_SIBLINGS) {
                    // skip entries, skip post-visit, skip siblings
                    return FileVisitResult.SKIP_SIBLINGS;
                } else if (visitResult == FileVisitResult.SKIP_SUBTREE) {
                    // skip visiting entries
                    visitor.postVisitDirectory(path, exception, files);
                    return FileVisitResult.CONTINUE;
                } else {
                    // TERMINATE
                    return FileVisitResult.TERMINATE;
                }
            } else if (attributes.isSymbolicLink()) {
                if (followLinks) {
                    AbsolutePath target = files.readSymbolicLink(path);
                    return walk(files, target, files.getAttributes(target), followLinks, maxDepth - 1, visitor);
                } else {
                    // visit the link itself
                    return visitor.visitFile(path, attributes, files);
                }
            } else {
                return visitor.visitFile(path, attributes, files);
            }
        } catch (OctopusIOException e) {
            return visitor.visitFileFailed(path, e, files);
        }
    }

    /**
     * Recursively copies directories, files and symbolic links from source to target.
     * 
     * @param files
     *          the files interface to use for file access. 
     * @param source
     *          the path to copy from.
     * @param target
     *          the path to copy to.
     * @param options
     *          the options to use while copying. See {@link CopyOption} for details.
     * 
     * @throws UnsupportedOperationException
     *           if an invalid combination of options is used.
     * @throws OctopusIOException
     *           if an I/O error occurs during the copying
     */
    public static void recursiveCopy(Files files, AbsolutePath source, AbsolutePath target, CopyOption... options)
            throws OctopusIOException, UnsupportedOperationException {

        boolean exist = files.exists(target);
        boolean replace = CopyOption.contains(CopyOption.REPLACE, options);
        boolean ignore = CopyOption.contains(CopyOption.IGNORE, options);
        if (replace && ignore) {
            throw new UnsupportedOperationException("FileUtils", "Can not replace and ignore existing files at the same time");
        }

        FileAttributes att = files.getAttributes(source);
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
                    throw new FileAlreadyExistsException(target.getFileSystem().getAdaptorName(), "Target " + target.getPath()
                            + " already exists!");
                }
            } else {
                files.createDirectories(target);
            }
            for (AbsolutePath f : files.newDirectoryStream(source)) {
                AbsolutePath fsource = f;
                AbsolutePath ftarget = target.resolve(new RelativePath(f.getFileName()));
                recursiveCopy(files, fsource, ftarget, options);
            }
        } else {
            if (exist) {
                if (ignore) {
                    // do nothing as requested
                } else if (replace) {
                    files.copy(source, target, nl.esciencecenter.octopus.files.CopyOption.REPLACE);
                } else {
                    throw new FileAlreadyExistsException(target.getFileSystem().getAdaptorName(), "Target " + target.getPath()
                            + " already exists!");
                }
            } else {
                files.copy(source, target);
            }
        }
    }

    /**
     * Recursively removes all directories, files and symbolic links in path.
     * 
     * @param files
     *          the files interface to use for file access. 
     * @param path
     *          the path to delete.
     *          
     * @throws OctopusIOException
     *          if an I/O error occurs during the copying
     */
    public static void recursiveDelete(Files files, AbsolutePath path) throws OctopusIOException {

        FileAttributes att = files.getAttributes(path);

        if (att.isDirectory()) {
            for (AbsolutePath f : files.newDirectoryStream(path)) {
                FileUtils.recursiveDelete(files, f);
            }
        }
        files.delete(path);
    }
}
