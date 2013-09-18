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
package nl.esciencecenter.cobalt.adaptors.local;

import java.io.InputStream;
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

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.engine.util.CommandRunner;
import nl.esciencecenter.cobalt.files.DirectoryNotEmptyException;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.files.NoSuchPathException;
import nl.esciencecenter.cobalt.files.OpenOption;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.PosixFilePermission;
import nl.esciencecenter.cobalt.files.RelativePath;

/**
 * LocalUtils contains various utilities for local file operations.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
final class LocalUtils {

    private LocalUtils() {
        // DO NOTE USE
    }

    static java.nio.file.Path javaPath(Path path) throws CobaltException {
        FileSystem fs = path.getFileSystem();
        RelativePath tmp = path.getRelativePath();

        return FileSystems.getDefault().getPath(fs.getLocation() + tmp.getAbsolutePath());
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
     * @throws CobaltException
     */
    static InputStream newInputStream(Path path) throws CobaltException {
        try {
            return Files.newInputStream(javaPath(path));
        } catch (Exception e) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Failed to create InputStream.", e);
        }
    }

    /**
     * @param path
     * @param permissions
     * @throws CobaltException
     */
    static void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws CobaltException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);
            view.setPermissions(LocalUtils.javaPermissions(permissions));
        } catch (Exception e) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Failed to set permissions " + path, e);
        }
    }

    /**
     * @param path
     * @throws CobaltException
     */
    static void createFile(Path path) throws CobaltException {
        try {
            Files.createFile(LocalUtils.javaPath(path));
        } catch (Exception e) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Failed to create file " + path, e);
        }
    }

    /**
     * @param path
     * @throws CobaltException
     */
    static void delete(Path path) throws CobaltException {

        try {
            Files.delete(LocalUtils.javaPath(path));
        } catch (java.nio.file.NoSuchFileException e1) {
            throw new NoSuchPathException(LocalAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!", e1);

        } catch (java.nio.file.DirectoryNotEmptyException e2) {
            throw new DirectoryNotEmptyException(LocalAdaptor.ADAPTOR_NAME, "Directory " + path + " not empty!", e2);

        } catch (Exception e) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Failed to delete file " + path, e);
        }
    }

    /**
     * @param source
     * @param target
     * @throws CobaltException
     */
    static void move(Path source, Path target) throws CobaltException {

        try {
            Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target));
        } catch (Exception e) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Failed to move " + source + " to " + target, e);
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
