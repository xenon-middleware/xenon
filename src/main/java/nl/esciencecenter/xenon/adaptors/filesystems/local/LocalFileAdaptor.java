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
package nl.esciencecenter.xenon.adaptors.filesystems.local;

import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.getLocalRoot;
import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.getLocalRootlessPath;
import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.isWindows;

import java.nio.file.Files;
import java.util.Map;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.utils.LocalFileSystemUtils;

/**
 * LocalFiles implements an Xenon <code>Files</code> adaptor for local file operations.
 *
 * @see Files
 *
 * @version 1.0
 * @since 1.0
 */
public class LocalFileAdaptor extends FileAdaptor {

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = "file";

    /** Local properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "This is the local file adaptor that implements" + " file functionality for local access.";

    /** The locations supported by the adaptor */
    public static final String[] ADAPTOR_LOCATIONS = new String[] { "(null)", "(empty string)", "[/workdir]", "driveletter:[/workdir]" };

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    /** List of properties supported by this FTP adaptor */
    public static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

    public LocalFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public boolean canCreateSymboliclinks() {
        // non-WIndows can
        // TODO Also can on Windows when user has create symbolic link rights
        return !isWindows();
    }

    @Override
    public boolean supportsReadingPosixPermissions() {
        return !isWindows();
    }

    @Override
    public boolean supportsSettingPosixPermissions() {
        return !isWindows();
    }

    @Override
    public boolean isConnectionless() {
        return true;
    }

    /**
     * Check if a location string is valid for the local filesystem.
     *
     * The location should -only- contain a file system root, such as "/" or "C:".
     *
     * @param location
     *            the location to check.
     * @throws InvalidLocationException
     *             if the location is invalid.
     */
    private static void checkFileLocation(String location) throws InvalidLocationException {
        if (location == null || location.isEmpty() || LocalFileSystemUtils.isLocalRoot(location)) {
            return;
        }

        throw new InvalidLocationException(ADAPTOR_NAME, "Location must only contain a file system root! (not " + location + ")");
    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {

        if (location == null || location.isEmpty()) {
            location = System.getProperty("user.dir");
        }

        String root = getLocalRoot(location);
        String path = getLocalRootlessPath(location);

        if (credential != null && !(credential instanceof DefaultCredential)) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Adaptor does not support this credential!");
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        Path entry = new Path(LocalFileSystemUtils.getLocalSeparator(), path);
        // for Windows remove the drive letter from entry?

        return new LocalFileSystem(getNewUniqueID(), location, root, entry, (int) bufferSize, xp);
    }
}
