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
package nl.esciencecenter.xenon.adaptors.local;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.util.CommandRunner;
import nl.esciencecenter.xenon.files.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Utils;

/**
 * LocalUtils contains various utilities for local file operations.
 * 
 * @version 1.0
 * @since 1.0
 */
final class LocalUtils {

    // Windows occasionally refuses to delete a directory because is it locked by some external service (i.e., virus scanner). 
    // The only solution to this is to retry the delete a couple of times. This constant sets the number of attempts to be done.
    private static final int WINDOWS_EXTRA_DELETE_ATTEMPTS = 1000;

    private LocalUtils() {
        // DO NOT USE
    }

    /**
     * Expand a Xenon Path to a Java Path.
     *
     * This will normalize the path and expand the user directory.
     *
     * @param path Xenon Path
     * @return a normalized java Path
     * @throws XenonException 
     */
    public static java.nio.file.Path javaPath(Path path) throws XenonException {
        FileSystem fs = path.getFileSystem();
        RelativePath relPath = path.getRelativePath().normalize();
        int numElems = relPath.getNameCount();
        String root = fs.getLocation();                

        // replace tilde        
        if (numElems != 0) {
            String firstPart = relPath.getName(0).getRelativePath();
            if ("~".equals(firstPart)) {                
                String tmp = System.getProperty("user.home");        
                root = Utils.getLocalRoot(tmp);
                RelativePath home = Utils.getRelativePath(tmp, root);
                
                if (numElems == 1) {
                    relPath = home;
                } else {
                    relPath = home.resolve(relPath.subpath(1, numElems));
                }
            } 
        }
        
        return FileSystems.getDefault().getPath(root, relPath.getAbsolutePath());
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

    public static java.nio.file.OpenOption[] javaOpenOptions(OpenOption[] options) {
        ArrayList<java.nio.file.OpenOption> result = new ArrayList<>(options.length);

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
            default:
                // No other options left         
            }
        }

        return result.toArray(new java.nio.file.OpenOption[result.size()]);
    }

    /**
     * @param path
     * @throws XenonException
     */
    public static InputStream newInputStream(Path path) throws XenonException {
        try {
            return Files.newInputStream(javaPath(path));
        } catch (IOException e) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Failed to create InputStream.", e);
        }
    }

    /**
     * @param path
     * @param permissions
     * @throws XenonException
     */
    public static void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);
            view.setPermissions(LocalUtils.javaPermissions(permissions));
        } catch (IOException e) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Failed to set permissions " + path, e);
        }
    }

    /**
     * Create a local file
     * @param path
     * @throws XenonException
     * @throws NullPointerException if the path is not set
     */
    public static void createFile(Path path) throws XenonException {
        try {
            Files.createFile(LocalUtils.javaPath(path));
        } catch (IOException e) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Failed to create file " + path, e);
        }
    }

    /**
     * Delete a local file
     * @param path
     * @throws XenonException
     * @throws NullPointerException if the path is not set
     */
    public static void delete(Path path) throws XenonException {
        try {
            Files.delete(LocalUtils.javaPath(path));
        } catch (java.nio.file.NoSuchFileException e1) {
            throw new NoSuchPathException(LocalAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!", e1);
        } catch (java.nio.file.DirectoryNotEmptyException e2) {            
            throw new DirectoryNotEmptyException(LocalAdaptor.ADAPTOR_NAME, "Directory " + path + " not empty!", e2);
        } catch (IOException e) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Failed to delete file " + path, e);
        }
    }

    /**
     * @param source
     * @param target
     * @throws XenonException
     */
    public static void move(Path source, Path target) throws XenonException {

        try {
            Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target));
        } catch (IOException e) {
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Failed to move " + source + " to " + target, e);
        }
    }

    public static void unixDestroy(Process process) {
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
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | XenonException e) {
            // Failed, so use the regular Java destroy.
        }

        if (!success) {
            process.destroy();
        }
    }
}
