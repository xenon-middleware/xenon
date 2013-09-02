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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.engine.util.CommandRunner;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.files.PosixFilePermission;

/**
 * LocalUtils contains various utilities for local file operations.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
final class LocalUtils {

    public static final String LOCAL_JOB_URI = "local:///";
    public static final String LOCAL_FILE_URI = "file:///";

    private LocalUtils() { 
        // DO NOTE USE
    }

    static URI getURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            // NOTE: Cannot unit test as this will never fail!
            throw new OctopusRuntimeException(LocalAdaptor.ADAPTOR_NAME, "Failed to create URI: " + uri, e);
        }
    }

    static URI getLocalJobURI() {
        return getURI(LOCAL_JOB_URI);
    }

    static URI getLocalFileURI() {
        return getURI(LOCAL_FILE_URI);
    }

    static String getHome() throws OctopusIOException { 
        String home = System.getProperty("user.home");
        
        if (home == null || home.length() == 0) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Home directory property user.home not set!");
        }

        return home;        
    }
    
    static String getCWD() throws OctopusIOException { 
        String cwd = System.getProperty("user.dir");
        
        if (cwd == null || cwd.length() == 0) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Current working directory property user.dir not set!");
        }

        return cwd;        
    }
    
    static String getRoot(String absolutePath) { 
        
        if (isWindows()) { 
            if (absolutePath != null && absolutePath.length() >= 2 && (absolutePath.charAt(1) == ':') && 
                    Character.isLetter(absolutePath.charAt(0))) { 
                return absolutePath.substring(0, 2).toUpperCase();
            }
            
            throw new InvalidParameterException("Path is not absolute! " + absolutePath);
        }
        
        if (absolutePath != null && absolutePath.length() >= 1 && (absolutePath.charAt(0) == '/')) { 
            return "/";
        }
            
        throw new InvalidParameterException("Path is not absolute! " + absolutePath);
    }
    
    static boolean isWindows() { 
        String os = System.getProperty("os.name");
        return (os != null && os.startsWith("Windows"));
    }
    
    static boolean isOSX() { 
        String os = System.getProperty("os.name");
        return (os != null && os.equals("MacOSX"));
    }
    
    static boolean isLinux() { 
        String os = System.getProperty("os.name");
        return (os != null && os.equals("Linux"));
    }
    
    /**
     * Check is a location is a valid windows root such as "C:". 
     * @param root the root to check. 
     * @return if the location is a valid windows root.
     */
    static boolean isWindowsRoot(String root) {
        
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

    static boolean isLinuxRoot(String root) {
        return (root != null && root.equals("/"));
    }
    
    static boolean isLocalRoot(String location) {
        
        if (isWindows()) { 
            return isWindowsRoot(location);
        }
        
        return isLinuxRoot(location);
    }
    
    public static Pathname getRelativePath(String path, String root) throws OctopusIOException {
        
        if (!path.startsWith(root)) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Path does not start with root: " + path + " " + root);
        }

        if (root.length() == path.length()) { 
            return new Pathname(getLocalSeparator());
        }
       
        return new Pathname(getLocalSeparator(), path.substring(root.length()));
    }

    private static char getLocalSeparator() {
        return File.separatorChar;
    }

//    static Pathname expandHome(Pathname path) throws OctopusIOException { 
//        
//        if (path.startsWith("~")) { 
//            return new Pathname(getHome(), path);
//        } 
//        
//        return path;
//    }

    static String getDefaultRoot() {
        // TODO: Test this on windows!
        File [] roots = File.listRoots();
        return roots[0].getPath();
    }
    
    static java.nio.file.Path javaPath(Path path) throws OctopusIOException {
        
        // Pathname tmp = expandHome(path.getPathname());
       
        FileSystem fs = path.getFileSystem();
        Pathname tmp = path.getPathname();
        
        return FileSystems.getDefault().getPath(fs.getLocation() + tmp.getAbsolutePath());
    }

    static FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>> javaPermissionAttribute(
            Set<PosixFilePermission> permissions) {
        return PosixFilePermissions.asFileAttribute(javaPermissions(permissions));
    }

    static Set<java.nio.file.attribute.PosixFilePermission> javaPermissions(Set<PosixFilePermission> permissions) {
        Set<java.nio.file.attribute.PosixFilePermission> result = new HashSet<java.nio.file.attribute.PosixFilePermission>();

        if (permissions == null) {
            return result;
        }

        for (PosixFilePermission permission : permissions) {
            result.add(java.nio.file.attribute.PosixFilePermission.valueOf(permission.toString()));
        }

        return result;
    }

    static Set<PosixFilePermission> octopusPermissions(Set<java.nio.file.attribute.PosixFilePermission> permissions) {
        if (permissions == null) {
            return null;
        }

        Set<PosixFilePermission> result = new HashSet<PosixFilePermission>();

        for (java.nio.file.attribute.PosixFilePermission permission : permissions) {
            result.add(PosixFilePermission.valueOf(permission.toString()));
        }

        return result;
    }

    static java.nio.file.OpenOption[] javaOpenOptions(OpenOption[] options) {

        ArrayList<java.nio.file.OpenOption> result = new ArrayList<java.nio.file.OpenOption>();

        for (OpenOption opt : options) {
            switch (opt) {
            case CREATE:
                result.add(StandardOpenOption.CREATE_NEW);
                break;
            case OPEN:
            case OPEN_OR_CREATE:
                result.add(StandardOpenOption.CREATE);
                break;
            case APPEND:
                result.add(StandardOpenOption.APPEND);
                break;
            case TRUNCATE:
                result.add(StandardOpenOption.TRUNCATE_EXISTING);
                break;
            case WRITE:
                result.add(StandardOpenOption.WRITE);
                break;
            case READ:
                result.add(StandardOpenOption.READ);
                break;
            }
        }

        return result.toArray(new java.nio.file.OpenOption[result.size()]);
    }

    /**
     * @param path
     * @throws OctopusIOException
     */
    static InputStream newInputStream(Path path) throws OctopusIOException {
        try {
            return Files.newInputStream(javaPath(path));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to create InputStream.", e);
        }
    }

    /**
     * @param path
     * @param options
     * @throws OctopusIOException
     */
    static SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws OctopusIOException {
        try {
            return Files.newByteChannel(javaPath(path), javaOpenOptions(options));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to create byte channel " + path, e);
        }
    }

    /**
     * @param path
     * @param permissions
     * @throws OctopusIOException
     */
    static void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);
            view.setPermissions(LocalUtils.javaPermissions(permissions));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to set permissions " + path, e);
        }
    }

    /**
     * @param path
     * @throws OctopusIOException
     */
    static void createFile(Path path) throws OctopusIOException {
        try {
            Files.createFile(LocalUtils.javaPath(path));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to create file " + path, e);
        }
    }

    /**
     * @param path
     * @return
     * @throws OctopusIOException
     */
    static long size(Path path) throws OctopusIOException {
        try {
            return Files.size(LocalUtils.javaPath(path));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to retrieve size of " + path, e);
        }
    }

    /**
     * @param path
     * @throws OctopusIOException
     */
    static void delete(Path path) throws OctopusIOException {

        try {
            Files.delete(LocalUtils.javaPath(path));
        } catch (java.nio.file.NoSuchFileException e1) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!", e1);

        } catch (java.nio.file.DirectoryNotEmptyException e2) {
            throw new DirectoryNotEmptyException(LocalAdaptor.ADAPTOR_NAME, "Directory " + path + " not empty!", e2);

        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to delete file " + path, e);
        }
    }

    /**
     * @param source
     * @param target
     * @throws OctopusIOException
     */
    static void move(Path source, Path target) throws OctopusIOException {

        try {
            Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to move " + source + " to " + target, e);
        }
    }

    static void unixDestroy(java.lang.Process process) {

        boolean success = false;
        
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
        } catch (Exception e) {
            // Failed, so use the regular Java destroy.
        }

        if (!success) { 
            process.destroy();
        }
    }
}
