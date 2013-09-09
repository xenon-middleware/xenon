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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * This class contains various utilities for handling files, jobs, streams, and for getting OS specific information.
 * 
 * These utilities are used inside Octopus, but may also be used by applications. 
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0
 */
public final class Utils {

    /** Name of this class (used in Exceptions) */
    private static final String NAME = "FileUtils";
    
    /** The default buffer size to use for copy operations */
    public static final int DEFAULT_BUFFER_SIZE = 16*1024;

    private Utils() { 
        // DO NOT USE
    }
    
    /**
     * Returns a <code>Scheduler</code> that can be used to run jobs locally. 
     * 
     * @param jobs
     *          the <code>Jobs</code> to use to instantiate the scheduler. 
     * @return
     *          the local <code>Scheduler</code>.
     *          
     * @throws OctopusIOException
     *          If an I/O error occurred
     * @throws OctopusException
     *          If the creation of the Scheduler failed. 
     */
    public static Scheduler getLocalScheduler(Jobs jobs) throws OctopusIOException, OctopusException { 
        return jobs.newScheduler("local", null, null, null);
    }
    
    /**
     * Create a <code>Scheduler</code> for the given scheme, but without using a location, credential, or properties. 
     * 
     * This methods is a shortcut for calling {@link Jobs.newScheduler(String, String, Credential, Map)} with only the 
     * <code>scheme</code> set. 
     * 
     * @param jobs
     *          the <code>Jobs</code> to use to instantiate the scheduler. 
     * @param scheme
     *          the scheme to use to access the scheduler.
     * @return
     *          the local <code>Scheduler</code>.
     *          
     * @throws OctopusIOException
     *          If an I/O error occurred
     * @throws OctopusException
     *          If the creation of the Scheduler failed. 
     */
    public static Scheduler newScheduler(Jobs jobs, String scheme) throws OctopusIOException, OctopusException { 
        return jobs.newScheduler(scheme, null, null, null);
    }    
    
    /**
     * Copy all bytes from an input stream to an output stream.
     * 
     * A temporary buffer of size <code>bufferSize</code> is used. If <code>bufferSize <= 0</code> then the 
     * {@link #DEFAULT_BUFFER_SIZE} will be used.  
     * <p>
     * NOTE: <code>in</code> and <code>out</code> will NOT be explicitly closed once the end of the stream is reached.
     * </p>
     * 
     * @param in
     *          the InputStream to read from.
     * @param out
     *          the OutputStream to write to.
     * @param bufferSize
     *          the size of the temporary buffer, or <= 0 to use the {@link #DEFAULT_BUFFER_SIZE}. 
     * @return
     *          the number of bytes copied.
     * @throws IOException
     *          if an I/O error occurs during the copy operation. 
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        
        long bytes = 0;

        if (bufferSize <= 0) { 
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        
        int len = in.read(buffer);

        while (len != -1) {
            bytes += len;
            out.write(buffer, 0, len);            
            len = in.read(buffer);
        }
        
        return bytes;
    }

    
    /**
     * Read all bytes from the input stream and return them in a byte array.
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * @param in 
     *          the input stream to read. 
     * @return 
     *          a byte array containing all bytes that the input stream produced. 
     * @throws IOException 
     *          if an I/O error was produced while reading the stream. 
     */
    public static byte [] readAllBytes(InputStream in) throws IOException {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

        copy(in, buffer, DEFAULT_BUFFER_SIZE);
        
        return buffer.toByteArray();
    }

    /**
     * Read all bytes from the input stream and return them in as a single String.
     * 
     * The bytes are converted to a String using Charset <code>cs</code>. 
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * @param in 
     *          the input stream to read.
     * @param cs
     *          the Charset to use          
     * @return 
     *          a byte array containing all bytes that the input stream produced. 
     * @throws IOException 
     *          if an I/O error was produced while reading the stream. 
     */
    public static String readToString(InputStream in, Charset cs) throws IOException {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

        copy(in, buffer, DEFAULT_BUFFER_SIZE);
        
        return buffer.toString(cs.name());
    }

    /**
     * Read all bytes from the input stream and return them in as a single String. 
     * 
     * The bytes are converted to a String using the default Charset. 
     * <p> 
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * 
     * @param in 
     *          the input stream to read.
     * @return 
     *          a byte array containing all bytes that the input stream produced. 
     * @throws IOException 
     *          if an I/O error was produced while reading the stream. 
     */
    public static String readToString(InputStream in) throws IOException {
        return readToString(in, Charset.defaultCharset());
    }
    
    /**
     * Read all lines from a InputStream and return them in a {@link java.util.List}.
     * 
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * 
     * @param in
     *          the InputStream to read from 
     * @param cs
     *          the Charset to use.
     * @return 
     *          a <code>List<String></code> containing all lines in the file.
     * @throws IOException
     *          if an I/O error was produced while reading the stream. 
     */
    public static List<String> readLines(InputStream in, Charset cs) throws IOException {

        ArrayList<String> result = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, cs));

        while (true) {
            String line = reader.readLine();

            if (line == null) {
                return result;
            }

            result.add(line);
        }
    }

    /**
     * Write lines of text to a file.
     * 
     * @param lines 
     *          the lines of text to write.
     * @param cs 
     *          the Charset to use.
     * @param out
     *          the output stream to write to
     * @throws IOException
     *          if an I/O error was produced while writing the stream. 
     */ 
    public static void writeLines(Iterable<? extends CharSequence> lines, Charset cs, OutputStream out) throws IOException {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, cs));
        
        for (CharSequence line : lines) {
            writer.write(line.toString());
            writer.newLine();
        }
        
        writer.flush();
    }
    
    
    /** 
     * Close a <code>Closable</code> and ignore any exceptions thrown. 
     * @param c the <code>Closable</code> to close.
     */
    public static void close(Closeable c) {

        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            // ignored!
        }
    }

    /**
     * Return an {@link OpenOption} array containing all options needed to open a file for writing. 
     *   
     * The <code>truncate</code> parameter determines if an existing file will be truncated or appended. 
     *   
     * @param truncate 
     *          Should an existing file be truncated ?
     * @return
     *          An {@link OpenOption} array containing all options needed to open a file for writing.  
     */    
    public static OpenOption[] openOptionsForWrite(boolean truncate) {
        if (truncate) {
            return new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.TRUNCATE };
        } else {
            return new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.APPEND };
        }
    }

    /**
     * Return the home directory of the current user as a String.
     * 
     * @return the home directory of the current user.
     * 
     * @throws OctopusIOException
     *          If the home directory could not be retrieved. 
     */
    public static String getHome() throws OctopusIOException { 
        String home = System.getProperty("user.home");
        
        if (home == null || home.length() == 0) { 
            throw new OctopusIOException(NAME, "Home directory property user.home not set!");
        }

        return home;        
    }

    /**
     * Return the current working directory as a String.
     * 
     * @return the current working directory.
     * 
     * @throws OctopusIOException
     *          If the current working directory could not be retrieved. 
     */
    public static String getCWD() throws OctopusIOException { 
        String cwd = System.getProperty("user.dir");
        
        if (cwd == null || cwd.length() == 0) { 
            throw new OctopusIOException(NAME, "Current working directory property user.dir not set!");
        }

        return cwd;        
    }
    
    /**
     * Return the locally valid root element of an <code>String</code> representation of an absolute path.
     * 
     * Examples of a root elements are "/" or "C:". If the provided path does not contain a locally valid root element, an 
     * exception will be thrown. For example, providing "/user/local" will return "/" on Linux or OSX, but throw an exception 
     * on Windows; providing "C:\test" will return "C:" on Windows but throw an exception on Linux or OSX.   
     * 
     * @param path
     *          The absolute path for which to determine the root element.
     * @return 
     *          The locally valid root element.
     * @throws OctopusException
     *          If the provided <code>path</code> is not absolute, or does not contain a locally valid root. 
     */
    public static String getLocalRoot(String path) throws OctopusException { 
        
        if (isWindows()) { 
            if (path != null && path.length() >= 2 && (path.charAt(1) == ':') && 
                    Character.isLetter(path.charAt(0))) { 
                return path.substring(0, 2).toUpperCase();
            }
            
            throw new OctopusException(NAME, "Path is not absolute! " + path);
        }
        
        if (path != null && path.length() >= 1 && (path.charAt(0) == '/')) { 
            return "/";
        }
            
        throw new OctopusException(NAME, "Path is not absolute! " + path);
    }
    
    /**
     * Returns if we are currently running on Windows. 
     * 
     * @return if we are currently running on Window.
     */
    public static boolean isWindows() { 
        String os = System.getProperty("os.name");
        return (os != null && os.startsWith("Windows"));
    }
    
    /**
     * Returns if we are currently running on OSX. 
     * 
     * @return if we are currently running on OSX.
     */
    public static boolean isOSX() { 
        String os = System.getProperty("os.name");
        return (os != null && os.equals("MacOSX"));
    }

    /**
     * Returns if we are currently running on Linux. 
     * 
     * @return if we are currently running on Linux.
     */
    public static boolean isLinux() { 
        String os = System.getProperty("os.name");
        return (os != null && os.equals("Linux"));
    }
    
    /**
     * Check if <code>root</code> only contains a valid Windows root element such as "C:".
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false<code> will be returned.  
     * If <code>root</code> contains more than just a root element, <code>false<code> will be returned.  
     *  
     * @param root 
     *          The root to check.
     * @return 
     *          If <code>root</code> only contains a valid Windows root element.
     */
    public static boolean isWindowsRoot(String root) {
        
        if (root == null) { 
            return false;
        }
        
        if (root.length() == 2 && root.endsWith(":") && Character.isLetter(root.charAt(0))) { 
            return true;
        }
        
        if (root.length() == 3 && root.endsWith(":") && Character.isLetter(root.charAt(0)) && root.charAt(3) == '\\') { 
            return true;
        }
        
        return false;
    }

    /**
     * Check if <code>root</code> only contains a valid Linux root element, which is "/".
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false<code> will be returned.  
     * If <code>root</code> contains more than just a root element, <code>false<code> will be returned.  
     *  
     * @param root 
     *          The root to check.
     * @return 
     *          If <code>root</code> only contains a valid Linux root element.
     */
    public static boolean isLinuxRoot(String root) {
        return (root != null && root.equals("/"));
    }
    
    /**
     * Check if <code>root</code> contains a valid OSX root element, which is "/".
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false<code> will be returned.
     * If <code>root</code> contains more than just a root element, <code>false<code> will be returned.  
     *  
     * @param root 
     *          The root to check.
     * @return 
     *          If <code>root</code> only contains a valid OSX root element.
     */
    public static boolean isOSXRoot(String root) {
        return (root != null && root.equals("/"));
    }
    
    /**
     * Check if <code>root</code> contains a locally valid root element, such as "C:" on Windows or "/" on Linux and OSX.
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false<code> will be returned.
     * If <code>root</code> contains more than just a root element, <code>false<code> will be returned.  
     *  
     * Note that the result of this method depends on the OS the application is running on.
     *
     * @param root 
     *          The root to check.
     * @return 
     *          If <code>root</code> only contains a valid OSX root element.
     */
    public static boolean isLocalRoot(String root) {
        
        if (isWindows()) { 
            return isWindowsRoot(root);
        }
        
        return isLinuxRoot(root);
    }

    /**
     * Checks if the provide path starts with a valid Linux root, that is "/".
     * 
     * @param path
     *          The path to check. 
     * @return
     *          If the provide path starts with a valid Linux root.
     */
    public static boolean startsWithLinuxRoot(String path) {
        
        if (path == null) { 
            return false;
        }
        
        return path.startsWith("/");
    }

    /**
     * Checks if the provide path starts with a valid Windows root, for example "C:".
     * 
     * @param path
     *          The path to check. 
     * @return
     *          If the provide path starts with a valid Windows root.
     */
    public static boolean startWithWindowsRoot(String path) {

        if (path == null) { 
            return false;
        }
        
        return path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0));
    }
   
    /**
     * Checks if the provide path starts with a valid root, such as "/" or "C:".
     * 
     * @param path
     *          The path to check. 
     * @return
     *          If the provide path starts with a valid root.
     */
    public static boolean startWithRoot(String path) {
        return startsWithLinuxRoot(path) || startWithWindowsRoot(path);
    }
    
    /**
     * Provided with an absolute <code>path</code> and a <code>root</code>, this method returns a <code>RelativePath</code> that
     * represents the part of <code>path</code> that is realtive to the <code>root</code>.
     * 
     * For example, if "C:\dir\file" is provided as <code>path</code> and "C:" as <code>root</code>, a <code>RelativePath</code>
     * will be returned that represents "dir\file".
     * 
     * @param path
     *          The absolute path.
     * @param root
     *          The root element. 
     * @return
     *          A <code>RelativePath</code> that contains the part of <code>path</code> that is relative to <code>root</code>.
     * @throws OctopusException
     *          If the <code>path</code> does not start with <code>root</code>.
     */
    public static RelativePath getRelativePath(String path, String root) throws OctopusException {
        
        if (!path.toUpperCase(Locale.getDefault()).startsWith(root.toUpperCase(Locale.getDefault()))) { 
            throw new OctopusException(NAME, "Path does not start with root: " + path + " " + root);
        }

        if (root.length() == path.length()) { 
            return new RelativePath(getLocalSeparator());
        }
       
        return new RelativePath(getLocalSeparator(), path.substring(root.length()));
    }

    /**
     * Returns the local file system path separator character.
     *  
     * @return
     *          The local file system path separator character. 
     */    
    public static char getLocalSeparator() {
        return File.separatorChar;
    }

    /**
     * Takes the String representation of a local path (for example "/bin/foo" or "C:\dir\test.txt") and converts it into a 
     * <code>Path</code>.
     *   
     * <code>path</code> must contain an absolute path starting with a root such as "/" or "C:".   
     *   
     * @param files
     *          the files interface used to create the <code>Path</code>.
     * @param path
     *          the local path to convert.
     *           
     * @return a <code>Path</code> representing the same location as <code>path</code>. 
     *          
     * @throws OctopusIOException   
     *          If an I/O error occurred
     * @throws OctopusException 
     *          If the creation of the FileSystem failed.
     */
    public static Path fromLocalPath(Files files, String path) throws OctopusException, OctopusIOException { 
        String root = getLocalRoot(path);
        FileSystem fs = files.newFileSystem("file", root, null, null);
        return files.newPath(fs, getRelativePath(path, root));
    }
    
    /**
     * Returns a <code>Path</code> that represents the current working directory.
     * 
     * This method retrieves the current working directory using {@link #getCWD()}, and converts this into a path using 
     * {@link #fromLocalPath(Files, String)}. 
     * 
     * @param files
     *          the files interface used to create the <code>Path</code>.
     * @return
     *          a <code>Path</code> that represents the current working directory.
     * 
     * @throws OctopusIOException
     *          If an I/O error occurred
     * @throws OctopusException
     *          If the creation of the FileSystem failed.
     */    
    public static Path getLocalCWD(Files files) throws OctopusException, OctopusIOException { 
        return fromLocalPath(files, getCWD());
    }

    /**
     * Returns a <code>Path</code> that represents the home directory of the current user.
     * 
     * This method retrieves the home directory using {@link #getHome()}, and converts this into a path using 
     * {@link #fromLocalPath(Files, String)}. 
     * 
     * @param files
     *          the files interface used to create the <code>Path</code>.
     * @return
     *          a <code>Path</code> that represents the home directory of the current user.
     * 
     * @throws OctopusIOException
     *          If an I/O error occurred
     * @throws OctopusException
     *          If the creation of the FileSystem failed.
     */    
    public static Path getLocalHome(Files files) throws OctopusException, OctopusIOException { 
        return fromLocalPath(files, getHome());
    }
    
    /**
     * Returns all local FileSystems. 
     *   
     * This method detects all local file system roots, and returns one or more <code>FileSystems</code> representing each of 
     * these roots.   
     *   
     * @param files
     *          the files interface to use to create the <code>FileSystems</code>.
     * @return all local FileSystems.
     * 
     * @throws OctopusIOException
     *          If an I/O error occurred
     * @throws OctopusException
     *          If the creation of the FileSystem failed.
     */
    public static FileSystem [] getLocalFileSystems(Files files) throws OctopusException, OctopusIOException {
        
        File [] roots = File.listRoots();
        
        FileSystem [] result = new FileSystem[roots.length]; 
        
        for (int i=0;i<result.length;i++) { 
            result[i] = files.newFileSystem("file", roots[i].getPath(), null, null);
        }
        
        return result;
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
    public static long copy(Files files, InputStream in, Path target, boolean truncate) throws OctopusException {

        long bytes = 0;
        OutputStream out = null;

        try { 
            out = files.newOutputStream(target, openOptionsForWrite(truncate));            
            bytes = copy(in, out, DEFAULT_BUFFER_SIZE);            
        } catch (IOException e) {
            throw new OctopusException(NAME, "Failed to copy stream to file.", e);
        } finally { 
            close(out);
        }
        
        return bytes;
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
    public static long copy(Files files, Path source, OutputStream out) throws OctopusException {
        
        long bytes = 0;
        InputStream in = null;

        try {
            in = files.newInputStream(source);
            bytes = copy(in, out, DEFAULT_BUFFER_SIZE);
        } catch (IOException e) {
            throw new OctopusException(NAME, "Failed to copy stream to file.", e);
        } finally { 
            close(in);
        }

        return bytes;
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
    public static BufferedReader newBufferedReader(Files files, Path source, Charset cs) throws OctopusIOException {
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
    public static BufferedWriter newBufferedWriter(Files files, Path target, Charset cs, boolean truncate)
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
    public static byte[] readAllBytes(Files files, Path source) throws OctopusException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(files, source, out);
        return out.toByteArray();
    }
    
    /**
     * Read all the bytes from a file and return them as a <code>String<\code> using the <code>Charset</code> for conversion.
     * 
     * @param files
     *          the files interface to use for file access.
     * @param source
     *          the file to read from.
     * @param cs
     *          the Charset to use.
     *          
     * @return a <code>String</code> containing all data from the file as converted using <code>cs</code>.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while opening or reading the file.
     */
    public static String readToString(Files files, Path source, Charset cs) throws OctopusIOException {
        
        InputStream in = null;
        
        try { 
            in = files.newInputStream(source);
            return readToString(in, cs);
        } catch (IOException e) {
            throw new OctopusIOException(NAME, "Failed to read data", e);
        } finally { 
            close(in);
        }
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
    public static List<String> readAllLines(Files files, Path source, Charset cs) throws OctopusIOException {

        InputStream in = null;
        
        try { 
            in = files.newInputStream(source);
            return readLines(in, cs);
        } catch (IOException e) {
            throw new OctopusIOException(NAME, "Failed to read lines", e);
        } finally { 
            close(in);
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
    public static void write(Files files, Path target, byte[] bytes, boolean truncate) throws OctopusException {
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
    public static void write(Files files, Path target, Iterable<? extends CharSequence> lines, Charset cs,
            boolean truncate) throws OctopusIOException {

        OutputStream out = null;
        
        try { 
            out = files.newOutputStream(target, openOptionsForWrite(truncate));
            writeLines(lines, cs, out);
        } catch (IOException e) {
            throw new OctopusIOException("FileUtils", "failed to write lines", e);
        } finally { 
            close(out);
        }
    }

    /**
     * Walks over a file tree. 
     * 
     * <p>
     * This method is equivalent to invoking {@link #walkFileTree(Files, Path, boolean, int, FileVisitor) 
     * walkFileTree(files, start, false, Integer.MAX_VALUE, visitor}. 
     * </p>
     * 
     * @param files
     *          the files interface to use for file access. 
     * @param start
     *          the path to start from.
     * @param visitor
     *          a {@link FileVisitor} that will be invoked for every {@link Path} encountered during the walk.
     * @throws OctopusIOException
     *          if an I/O error occurs during the walk.
     */
    public static void walkFileTree(Files files, Path start, FileVisitor visitor) throws OctopusIOException {
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
     * {@link FileVisitor#visitFile(Path, FileAttributes, Files) visitor.visitFile} and the result of this call will be 
     * returned.
     * </li>
     * <li>
     * If the current path is a link and <code>followLinks</code> is <code>true</code> the link is followed,  
     * <code>walkFileTree</code> is called on the target of the link, and the result of this call is returned.
     * </li>
     * <li> 
     * If the current path is a link and <code>followLinks</code> is <code>false</code> the link is not followed. Instead the 
     * link itself is forwarded to {@link FileVisitor#visitFile(Path, FileAttributes, Files) visitor.visitFile} and the
     * result of this call is returned.
     * </li>
     * <li>
     * If the current path is a directory and the current distance from <code>start</code> is more than <code>maxDepth</code>, 
     * the directory is forwarded to {@link FileVisitor#visitFile(Path, FileAttributes, Files) visitor.visitFile} and 
     * the result of this call is returned.
     * </li>
     * <li>
     * If the current path is a directory and the current distance from <code>start</code> is less or equal to 
     * <code>maxDepth</code> the path is forwarded to 
     * {@link FileVisitor#preVisitDirectory(Path, FileAttributes, Files) visitor.preVisitDirectory}. The subsequent 
     * behavior then depends on the result of this call:
     * <ul>
     * <li> 
     * If {@link FileVisitResult#TERMINATE} is returned, the walk is terminated immediately and <code>walkFileTree</code> returns 
     * {@link FileVisitResult#TERMINATE}.
     * </li>
     * <li> 
     * If {@link FileVisitResult#SKIP_SUBTREE} is returned, the elements in the directory will not be visited. Instead 
     * {@link FileVisitor#postVisitDirectory(Path, OctopusIOException, Files) visitor.postVisitDirectory} and  
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
     * skipped, {@link FileVisitor#postVisitDirectory(Path, OctopusIOException, Files) visitor.postVisitDirectory} will be 
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
     *          a {@link FileVisitor} that will be invoked for every {@link Path} encountered during the walk.
     *          
     * @throws OctopusIOException
     *          if an I/O error occurs during the walk.
     */
    public static void walkFileTree(Files files, Path start, boolean followLinks, int maxDepth,
            FileVisitor visitor) throws OctopusIOException {
        FileAttributes attributes = files.getAttributes(start);
        walk(files, start, attributes, followLinks, maxDepth, visitor);
    }

    // Walk a file tree.
    private static FileVisitResult walk(Files files, Path path, FileAttributes attributes, boolean followLinks,
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
                    visitor.postVisitDirectory(path, null, files);
                    return FileVisitResult.CONTINUE;
                } else {
                    // TERMINATE
                    return FileVisitResult.TERMINATE;
                }
            } else if (attributes.isSymbolicLink()) {
                if (followLinks) {
                    Path target = files.readSymbolicLink(path);
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
    public static void recursiveCopy(Files files, Path source, Path target, CopyOption... options)
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
                    throw new FileAlreadyExistsException(target.getFileSystem().getAdaptorName(), "Target " + target
                            + " already exists!");
                }
            } else {
                files.createDirectories(target);
            }
            for (Path f : files.newDirectoryStream(source)) {
                Path fsource = f;
                Path ftarget = files.newPath(target.getFileSystem(), target.getRelativePath().resolve(f.getRelativePath().getFileName()));
                recursiveCopy(files, fsource, ftarget, options);
            }
        } else {
            if (exist) {
                if (ignore) {
                    // do nothing as requested
                } else if (replace) {
                    files.copy(source, target, nl.esciencecenter.octopus.files.CopyOption.REPLACE);
                } else {
                    throw new FileAlreadyExistsException(target.getFileSystem().getAdaptorName(), "Target " + target
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
    public static void recursiveDelete(Files files, Path path) throws OctopusIOException {

        FileAttributes att = files.getAttributes(path);

        if (att.isDirectory()) {
            for (Path f : files.newDirectoryStream(path)) {
                Utils.recursiveDelete(files, f);
            }
        }
        files.delete(path);
    }

    /**
     * Resolve a relative <code>path</code> against <code>root</code> and return a new {@link Path} that represents this location.  
     *   
     * @param files 
     *          the files interface to use for file access. 
     * @param root
     *          the root to resolve against.
     * @param path
     *          the relative path to resolve.
     * @return
     *          a new <code>Path</code> that represents the location.
     * @throws OctopusIOException
     *          if an I/O error occurs during the resolve.
     */
    public static Path resolveWithRoot(Files files, Path root, String... path) throws OctopusIOException { 
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(new RelativePath(path)));
    }

    /**
     * Resolve a relative <code>path</code> with the entry path of the <code>fileSystem</code> and return a new 
     * {@link Path} that represents this location.  
     *   
     * @param files 
     *          the files interface to use for file access. 
     * @param fileSystem
     *          the FileSystem for which to use the entry path.
     * @param path
     *          the relative path to resolve.
     * @return
     *          a new <code>Path</code> that represents the location.
     * @throws OctopusIOException
     *          if an I/O error occurs during the resolve.
     */
    public static Path resolveWithEntryPath(Files files, FileSystem fileSystem, String ... path) throws OctopusIOException {
        return resolveWithRoot(files, fileSystem.getEntryPath(), path);
    }
}
