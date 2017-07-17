/**
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
package nl.esciencecenter.xenon.adaptors.shared.local;

import static nl.esciencecenter.xenon.adaptors.filesystems.local.LocalFileAdaptor.ADAPTOR_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.local.CommandRunner;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

/**
 * LocalUtils contains various utilities for local file operations.
 * 
 * @version 1.0
 * @since 1.0
 */
public class LocalUtil {

    private LocalUtil() {
        // DO NOT USE
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
     * If <code>root</code> is <code>null</code> or empty, <code>false</code> will be returned.
     * If <code>root</code> contains more than just a root element, <code>false</code> will be returned.
     * 
     * @param root
     *            The root to check.
     * @return If <code>root</code> only contains a valid Windows root element.
     */
    public static boolean isWindowsRoot(String root) {
        if (root == null) {
            return false;
        }

        if (root.length() == 2 && root.endsWith(":") && Character.isLetter(root.charAt(0))) {
            return true;
        }

        return (root.length() == 3 && root.endsWith(":") && Character.isLetter(root.charAt(0)) && root.charAt(3) == '\\');        
    }

    /**
     * Check if <code>root</code> only contains a valid Linux root element, which is "/".
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false</code> will be returned.
     * If <code>root</code> contains more than just a root element, <code>false</code> will be returned.
     * 
     * @param root
     *            The root to check.
     * @return If <code>root</code> only contains a valid Linux root element.
     */
    public static boolean isLinuxRoot(String root) {
        return (root != null && root.equals("/"));
    }

    /**
     * Check if <code>root</code> contains a valid OSX root element, which is "/".
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false</code> will be returned.
     * If <code>root</code> contains more than just a root element, <code>false</code> will be returned.
     * 
     * @param root
     *            The root to check.
     * @return If <code>root</code> only contains a valid OSX root element.
     */
    public static boolean isOSXRoot(String root) {
        return (root != null && root.equals("/"));
    }

    /**
     * Check if <code>root</code> contains a locally valid root element, such as "C:" on Windows or "/" on Linux and OSX.
     *
     * If <code>root</code> is <code>null</code> or empty, <code>false</code> will be returned.
     * If <code>root</code> contains more than just a root element, <code>false</code> will be returned.
     *
     * Note that the result of this method depends on the OS the application is running on.
     *
     * @param root
     *            The root to check.
     * @return If <code>root</code> only contains a valid OSX root element.
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
     *            The path to check.
     * @return If the provide path starts with a valid Linux root.
     */
    public static boolean startsWithLinuxRoot(String path) {
        return path != null && path.startsWith("/");

    }

    /**
     * Checks if the provide path starts with a valid Windows root, for example "C:".
     *
     * @param path
     *            The path to check.
     * @return If the provide path starts with a valid Windows root.
     */
    public static boolean startWithWindowsRoot(String path) {
        return path != null && path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0));

    }

    /**
     * Checks if the provide path starts with a valid root, such as "/" or "C:".
     *
     * @param path
     *            The path to check.
     * @return If the provide path starts with a valid root.
     */
    public static boolean startWithRoot(String path) {
        return startsWithLinuxRoot(path) || startWithWindowsRoot(path);
    }
    
    /**
     * Return the locally valid root element of an <code>String</code> representation of an absolute path.
     *
     * Examples of a root elements are "/" or "C:". If the provided path does not contain a locally valid root element, an
     * exception will be thrown. For example, providing "/user/local" will return "/" on Linux or OSX, but throw an exception on
     * Windows; providing "C:\test" will return "C:" on Windows but throw an exception on Linux or OSX.
     *
     * @param p
     *            The absolute path for which to determine the root element.
     * @return The locally valid root element.
     * @throws XenonException
     *             If the provided <code>path</code> is not absolute, or does not contain a locally valid root.
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public static String getLocalRoot(String p) throws XenonException {
        
        String path = p;
        
        if (isWindows()) {
            if (path == null || path.isEmpty()) {
                return "";
            }
            if (!path.contains("/") && !path.contains("\\")) {
                // Windows URS, network drive
                return path;
            }
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }
            if (path.length() >= 2 && (path.charAt(1) == ':') && Character.isLetter(path.charAt(0))) {
                return path.substring(0, 2).toUpperCase();
            }

            throw new InvalidLocationException(ADAPTOR_NAME, "Path does not include drive name! " + path);
        }

        if (path == null || path.isEmpty() || (path.length() >= 1 && path.charAt(0) == '/')) {
            return "/";
        }

        throw new InvalidLocationException(ADAPTOR_NAME, "Path is not absolute! " + path);
    }
    
    /**
     * Returns the local file system path separator character.
     * 
     * @return The local file system path separator character.
     */
    public static char getLocalSeparator() {
        return File.separatorChar;
    }
    
    /**
     * Provided with an absolute <code>path</code> and a <code>root</code>, this method returns a <code>RelativePath</code> that
     * represents the part of <code>path</code> that is relative to the <code>root</code>.
     *
     * For example, if "C:\dir\file" is provided as <code>path</code> and "C:" as <code>root</code>, a <code>RelativePath</code>
     * will be returned that represents "dir\file".
     *
     * @param path
     *            The absolute path.
     * @param root
     *            The root element.
     * @return A <code>RelativePath</code> that contains the part of <code>path</code> that is relative to <code>root</code>.
     * @throws XenonException
     *             If the <code>path</code> does not start with <code>root</code>.
     */
    public static Path getRelativePath(String path, String root) throws XenonException {
        if (!path.toUpperCase(Locale.getDefault()).startsWith(root.toUpperCase(Locale.getDefault()))) {
            throw new XenonException(ADAPTOR_NAME, "Path does not start with root: " + path + " " + root);
        }

        if (root.length() == path.length()) {
            return new Path(getLocalSeparator());
        }

        return new Path(getLocalSeparator(), path.substring(root.length()));
    }
    
    /**
     * Expand a Xenon Path to a Java Path.
     *
     * This will normalize the path and expand the user directory.
     *
     * @param path Xenon Path
     * @return a normalized java Path
     * @throws XenonException 
     *          if an error occurred
     */
    public static java.nio.file.Path javaPath(FileSystem fs, Path path) throws XenonException {
        Path relPath = path.normalize();
        int numElems = relPath.getNameCount();
        String root = fs.getLocation();                

        // replace tilde        
        if (numElems != 0) {
            String firstPart = relPath.getName(0).getRelativePath();
            if ("~".equals(firstPart)) {                
                String tmp = System.getProperty("user.home");        
                root = getLocalRoot(tmp);
                Path home = getRelativePath(tmp, root);
                
                if (numElems == 1) {
                    relPath = home;
                } else {
                    relPath = home.resolve(relPath.subpath(1, numElems));
                }
            } 
        }
        
        return FileSystems.getDefault().getPath(root, relPath.getAbsolutePath());
    }

    public static List<PathAttributes> listDirectory(FileSystem fs, Path dir) throws XenonException {

    	try { 
    		ArrayList<PathAttributes> result = new ArrayList<>();

    		DirectoryStream<java.nio.file.Path> s = java.nio.file.Files.newDirectoryStream(LocalUtil.javaPath(fs, dir));

    		for (java.nio.file.Path p : s) {
    			result.add(getLocalFileAttributes(dir.resolve(p.getFileName().toString()), p));
    		}
    		
    		return result;
    	} catch (IOException e) {
    		throw new XenonException(ADAPTOR_NAME, "Failed to list directory: " + dir, e);
		}
    }
    
    public static PathAttributes getLocalFileAttributes(FileSystem fs, Path path) throws XenonException {
    	return getLocalFileAttributes(path, javaPath(fs, path));
    }

    public static PathAttributes getLocalFileAttributes(Path p, java.nio.file.Path path) throws XenonException {
    	try {
            PathAttributes result = new PathAttributes();
            
            result.setPath(p);
            result.setExecutable(java.nio.file.Files.isExecutable(path));
            result.setReadable(java.nio.file.Files.isReadable(path));
            result.setReadable(java.nio.file.Files.isWritable(path));
            
            boolean isWindows = LocalUtil.isWindows(); 

            BasicFileAttributes basicAttributes;
            
            if (isWindows) {                
                // TODO: Seems to fail in Windows ?
                result.setHidden(false);
                
                // These should always work.
                basicAttributes = java.nio.file.Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                
//               // These are windows only.
//                AclFileAttributeView aclAttributes = Files.getFileAttributeView(javaPath, AclFileAttributeView.class, 
//                        LinkOption.NOFOLLOW_LINKS);
                
            } else {
            	result.setHidden(java.nio.file.Files.isHidden(path));
                
                // Note: when in a posix environment, basicAttributes point to posixAttributes.
            	 java.nio.file.attribute.PosixFileAttributes posixAttributes = java.nio.file.Files.readAttributes(path,
            			 java.nio.file.attribute.PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                
                basicAttributes = posixAttributes;
    
                result.setOwner(posixAttributes.owner().getName());
                result.setGroup(posixAttributes.group().getName());
                result.setPermissions(LocalUtil.xenonPermissions(posixAttributes.permissions()));
            }
            
            result.setCreationTime(basicAttributes.creationTime().toMillis());
            result.setLastAccessTime(basicAttributes.lastAccessTime().toMillis());
            result.setLastModifiedTime(basicAttributes.lastModifiedTime().toMillis());
            
            result.setDirectory(basicAttributes.isDirectory());
            result.setRegular(basicAttributes.isRegularFile());
            result.setSymbolicLink(basicAttributes.isSymbolicLink());
            result.setOther(basicAttributes.isOther());
            
            if (result.isRegular()) { 
                result.setSize(basicAttributes.size());
            } 
            
            return result;
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Cannot read attributes.", e);
        }
    }
    
    public static Set<java.nio.file.attribute.PosixFilePermission> javaPermissions(Set<PosixFilePermission> permissions) {
        if (permissions == null) {
            return new HashSet<>(0);
        }

        Set<java.nio.file.attribute.PosixFilePermission> result = new HashSet<>(permissions.size() * 4 / 3 + 1);

        for (PosixFilePermission permission : permissions) {
            result.add(java.nio.file.attribute.PosixFilePermission.valueOf(permission.toString()));
        }

        return result;
    }

    public static Set<PosixFilePermission> xenonPermissions(Set<java.nio.file.attribute.PosixFilePermission> permissions) {
        if (permissions == null) {
            return null;
        }

        Set<PosixFilePermission> result = new HashSet<>(permissions.size() * 4 / 3 + 1);

        for (java.nio.file.attribute.PosixFilePermission permission : permissions) {
            result.add(PosixFilePermission.valueOf(permission.toString()));
        }

        return result;
    }
//
//    public static java.nio.file.OpenOption[] javaOpenOptions(OpenOption[] options) {
//        ArrayList<java.nio.file.OpenOption> result = new ArrayList<>(options.length);
//
//        for (OpenOption opt : options) {
//            switch (opt) {
//            case CREATE:
//                result.add(StandardOpenOption.CREATE_NEW);
//                break;
//            case OPEN:
//            case OPEN_OR_CREATE:
//                result.add(StandardOpenOption.CREATE);
//                break;
//            case APPEND:
//                result.add(StandardOpenOption.APPEND);
//                break;
//            case TRUNCATE:
//                result.add(StandardOpenOption.TRUNCATE_EXISTING);
//                break;
//            case WRITE:
//                result.add(StandardOpenOption.WRITE);
//                break;
//            case READ:
//                result.add(StandardOpenOption.READ);
//                break;
//            default:
//                // No other options left         
//            }
//        }
//
//        return result.toArray(new java.nio.file.OpenOption[result.size()]);
//    }

    /*
     * @param path
     * @throws XenonException
     */
    public static InputStream newInputStream(FileSystem fs, Path path) throws XenonException {
        try {
            return Files.newInputStream(javaPath(fs, path));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create InputStream.", e);
        }
    }

    /*
     * @param path
     * @param permissions
     * @throws XenonException
     */
    public static void setPosixFilePermissions(FileSystem fs, Path path, Set<PosixFilePermission> permissions) throws XenonException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtil.javaPath(fs, path), PosixFileAttributeView.class);
            view.setPermissions(LocalUtil.javaPermissions(permissions));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to set permissions " + path, e);
        }
    }

    /*
     * Create a local file
     * @param path
     * @throws XenonException
     * @throws NullPointerException if the path is not set
     */
    public static void createFile(FileSystem fs, Path path) throws XenonException {
        try {
            Files.createFile(LocalUtil.javaPath(fs, path));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create file " + path, e);
        }
    }

    /*
     * Create a symbolic link
     * @param path
     * @throws XenonException
     * @throws NullPointerException if the path is not set
     */
	public static void createSymbolicLink(FileSystem fs, Path link, Path path) throws XenonException {

		try {
            Files.createSymbolicLink(LocalUtil.javaPath(fs, link), LocalUtil.javaPath(fs, path));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create link " + link + " to " + path, e);
        }
	}

    /*
     * Delete a local file
     * @param path
     * @throws XenonException
     * @throws NullPointerException if the path is not set
     */
    public static void delete(FileSystem fs, Path path) throws XenonException {
        try {
            Files.delete(LocalUtil.javaPath(fs, path));
        } catch (java.nio.file.NoSuchFileException e1) {
            throw new NoSuchPathException(ADAPTOR_NAME, "File " + path + " does not exist!", e1);
        } catch (java.nio.file.DirectoryNotEmptyException e2) {            
            throw new DirectoryNotEmptyException(ADAPTOR_NAME, "Directory " + path + " not empty!", e2);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to delete file " + path, e);
        }
    }

    public static void move(FileSystem fs, Path source, Path target) throws XenonException {

        try {
            Files.move(LocalUtil.javaPath(fs, source), LocalUtil.javaPath(fs, target));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to move " + source + " to " + target, e);
        }
    }

    public static void unixDestroy(Process process) {
        boolean success = false;

        if (!isWindows()) { 
            try {
                final Field pidField = process.getClass().getDeclaredField("pid");

                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        pidField.setAccessible(true);
                        return null;
                    }
                });

                int pid = pidField.getInt(process);

                if (pid > 0) {
                    CommandRunner killRunner = new CommandRunner("kill", "-9", "" + pid);
                    success = (killRunner.getExitCode() == 0);
                }
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | XenonException e) {
                // Failed, so use the regular Java destroy.
            }
        }
            
        if (!success) {
            process.destroy();
        }
    }
    
    public static void checkCredential(String adaptorName, Credential credential) throws XenonException {

        if (credential == null || credential instanceof DefaultCredential) {
            return;
        }
       
        throw new InvalidCredentialException(adaptorName, "Adaptor does not support this credential!");
    }
}
