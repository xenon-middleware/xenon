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
package nl.esciencecenter.xenon.adaptors.filesystems.ftp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;

public class FtpFileAdaptor extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "ftp";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The FTP adaptor implements file access on remote ftp servers.";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "host[:port][/workdir]" };

    /** List of properties supported by this FTP adaptor */
    private static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

    public FtpFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    protected FTPClient connect(String location, Credential credential) throws XenonException {

        URI uri;

        try {
            uri = new URI("ftp://" + location);
        } catch (Exception e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        }

        FTPClient ftpClient = new FTPClient();
        ftpClient.setListHiddenFiles(true);

        String host = uri.getHost();
        int port = uri.getPort();

        if (port == -1) {
            port = DEFAULT_PORT;
        }

        connectToServer(host, port, ftpClient);
        login(credential, ftpClient);

        try {
            ftpClient.enterLocalPassiveMode();
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to switch to PASSIVE mode");
        }

        return ftpClient;
    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        LOGGER.debug("newFileSystem ftp location = {} credential = {} properties = {}", location, credential, properties);

        if (location == null || location.isEmpty()) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Location may not be empty");
        }

        if (credential == null) {
            throw new InvalidCredentialException(getName(), "Credential may not be null.");
        }

        if (!(credential instanceof PasswordCredential || credential instanceof DefaultCredential)) {
            throw new InvalidCredentialException(getName(), "Credential type not supported.");
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        FTPClient ftpClient = connect(location, credential);

        String cwd = null;

        try {
            cwd = getCurrentWorkingDirectory(ftpClient, location);
        } catch (Exception e) {
            try {
                ftpClient.disconnect();
            } catch (Exception ex) {
                // ignored
            }
            throw e;
        }

        return new FtpFileSystem(getNewUniqueID(), ADAPTOR_NAME, location, new Path(cwd), (int) bufferSize, ftpClient, credential, this, xp);
    }

    private String getCurrentWorkingDirectory(FTPClient ftpClient, String location) throws XenonException {

        try {
            String pathFromURI = new URI("ftp://" + location).getPath();

            if (pathFromURI == null || pathFromURI.isEmpty()) {
                return ftpClient.printWorkingDirectory();
            }

            if (ftpClient.changeWorkingDirectory(pathFromURI)) {
                return pathFromURI;
            } else {
                throw new NoSuchPathException(ADAPTOR_NAME, "Specified working directory does not exist: " + pathFromURI);
            }
        } catch (URISyntaxException e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        } catch (IOException e) {
            throw new XenonException(getName(), "Could not set current working directory", e);
        }
    }

    private void connectToServer(String host, int port, FTPClient ftp) throws XenonException {
        try {
            ftp.connect(host, port);
        } catch (IOException e) {
            throw new XenonException(getName(), "Failed to connect", e);
        }
    }

    private void login(Credential credential, FTPClient ftp) throws XenonException {
        try {
            loginWithCredentialOrDefault(ftp, credential);
            int replyCode = ftp.getReplyCode();
            verifyLoginSuccess(replyCode);
        } catch (XenonException | IOException e) {
            throw new XenonException(getName(), "Failed to login", e);
        }
    }

    /*
     * Returns true if code is in interval [200,300). See http://en.wikipedia.org/wiki/List_of_FTP_server_return_codes.
     *
     * @param replyCode
     */
    private void verifyLoginSuccess(int replyCode) throws XenonException {
        if (replyCode < 200 || replyCode >= 300) {
            String message = MessageFormat.format("Server status not succesfull after login (status code {0}).", replyCode);
            throw new XenonException(getName(), message);
        }
    }

    private void loginWithCredentialOrDefault(FTPClient ftp, Credential credential) throws IOException {
        String password = "";
        String user = "anonymous";
        if (credential instanceof PasswordCredential) {
            PasswordCredential passwordCredential = (PasswordCredential) credential;
            password = new String(passwordCredential.getPassword());
            user = passwordCredential.getUsername();
        }
        ftp.login(user, password);
    }

    @Override
    public boolean supportsReadingPosixPermissions() {
        return true;
    }

    @Override
    public boolean canAppend() {
        return true;
    }

}
