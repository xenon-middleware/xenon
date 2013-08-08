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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.engine.util.CommandRunner;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PosixFilePermission;

/**
 * LocalUtils contains various utilities for local file operations.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalUtils {

    public static final String LOCAL_JOB_URI = "local:///";
    public static final String LOCAL_FILE_URI = "file:///";

    private LocalUtils() { 
        // DO NOTE USE
    }
    
    static String getHome() throws OctopusException {

        String path = System.getProperty("user.home");

        if (!LocalUtils.exists(path)) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Home directory does not exist: " + path);
        }

        return path;
    }

    static String getCWD() throws OctopusException {

        String path = System.getProperty("user.dir");

        if (!LocalUtils.exists(path)) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Current working directory does not exist: " + path);
        }

        return path;
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

    private static String expandHome(String path) {

        if (path.startsWith("~")) {
            return System.getProperty("user.home") + "/" + (path.length() > 1 ? path.substring(1) : "");
        }

        return path;
    }

    static boolean exists(String path) {

        if (path == null) {
            return false;
        }

        return Files.exists(FileSystems.getDefault().getPath(expandHome(path)), LinkOption.NOFOLLOW_LINKS);
    }

    static java.nio.file.Path javaPath(AbsolutePath path) {
        return FileSystems.getDefault().getPath(expandHome(path.getPath()));
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
    static InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
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
    static SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
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
    static void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {
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
    static void createFile(AbsolutePath path) throws OctopusIOException {
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
    static long size(AbsolutePath path) throws OctopusIOException {
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
    static void delete(AbsolutePath path) throws OctopusIOException {

        try {
            Files.delete(LocalUtils.javaPath(path));
        } catch (java.nio.file.NoSuchFileException e1) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " does not exist!", e1);

        } catch (java.nio.file.DirectoryNotEmptyException e2) {
            throw new DirectoryNotEmptyException(LocalAdaptor.ADAPTOR_NAME, "Directory " + path.getPath() + " not empty!", e2);

        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to delete file " + path, e);
        }
    }

    /**
     * @param source
     * @param target
     * @throws OctopusIOException
     */
    static void move(AbsolutePath source, AbsolutePath target) throws OctopusIOException {

        try {
            Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target));
        } catch (Exception e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to move " + source + " to " + target, e);
        }
    }

    static void unixDestroy(java.lang.Process process) {

        try {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);

            int pid = pidField.getInt(process);

            if (pid <= 0) {
                throw new Exception("Pid reported as 0 or negative: " + pid);
            }

            CommandRunner killRunner = new CommandRunner("kill", "-9", "" + pid);

            if (killRunner.getExitCode() != 0) {
                throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Failed to kill process, exit code was "
                        + killRunner.getExitCode() + " output: " + killRunner.getStdout() + " error: " + killRunner.getStderr());
            }
        } catch (Throwable t) {
            // Failed, so use the regular Java destroy.
            process.destroy();
        }
    }

}
