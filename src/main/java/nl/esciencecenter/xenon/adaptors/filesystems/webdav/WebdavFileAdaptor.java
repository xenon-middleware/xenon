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
package nl.esciencecenter.xenon.adaptors.filesystems.webdav;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class WebdavFileAdaptor extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFileAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "webdav";

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The webdav file adaptor implements file access to remote webdav servers.";

    /** The locations supported by this adaptor */
    public static final String[] ADAPTOR_LOCATIONS = new String[] { "http://host[:port][/workdir]", "https://host[:port][/workdir]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    /** List of properties supported by this FTP adaptor */
    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

    public static final int OK_CODE = 200;

    public WebdavFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public boolean canReadSymboliclinks() {
        // Webdav cannot read symbolic links.
        return false;
    }

    @Override
    public boolean isConnectionless() {
        // Webdav is connectionless.
        return true;
    }

    @Override
    public boolean canAppend() {
        // Webdav cannot append to a file.
        return false;
    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {

        LOGGER.debug("newFileSystem location = {} credential = {} properties = {}", location, credential, properties);

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        URI uri;

        try {
            uri = new URI(location);
        } catch (Exception e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        }

        Sardine sardine = null;

        if (credential == null || credential instanceof DefaultCredential) {
            sardine = SardineFactory.begin();
        } else if (credential instanceof PasswordCredential) {
            PasswordCredential tmp = (PasswordCredential) credential;
            sardine = SardineFactory.begin(tmp.getUsername(), new String(tmp.getPassword()));
        }

        String server = uri.getScheme() + "://" + uri.getHost();

        int port = uri.getPort();

        if (port != -1) {
            server = server + ":" + port;
        }

        String cwd = uri.getPath();

        return new WebdavFileSystem(getNewUniqueID(), ADAPTOR_NAME, location, server, new Path(cwd), (int) bufferSize, sardine, xp);
    }

    public void end() {
        LOGGER.debug("end OK");
    }

}
