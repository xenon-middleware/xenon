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
package nl.esciencecenter.xenon.adaptors.filesystems.sftp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;
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
import nl.esciencecenter.xenon.adaptors.shared.ssh.SSHUtil;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;

public class SftpFileAdaptor extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpFileAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "sftp";

    /** The default SSH port */
    public static final int DEFAULT_PORT = 22;

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The SFTP adaptor implements all file access functionality to remote SFTP servers";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** Enable strict host key checking. */
    public static final String STRICT_HOST_KEY_CHECKING = PREFIX + "strictHostKeyChecking";

    /** Enable the use of an ssh-agent */
    public static final String AGENT = PREFIX + "agent";

    /** Enable the use of ssh-agent-forwarding */
    public static final String AGENT_FORWARDING = PREFIX + "agentForwarding";

    /** Load the known_hosts file by default. */
    public static final String LOAD_STANDARD_KNOWN_HOSTS = PREFIX + "loadKnownHosts";

    /** Load the OpenSSH config file by default. */
    public static final String LOAD_SSH_CONFIG = PREFIX + "loadSshConfig";

    /** OpenSSH config filename. */
    public static final String SSH_CONFIG_FILE = PREFIX + "sshConfigFile";

    /** Enable strict host key checking. */
    public static final String AUTOMATICALLY_ADD_HOST_KEY = PREFIX + "autoAddHostKey";

    /** Add gateway to access machine. */
    public static final String GATEWAY = PREFIX + "gateway";

    /** Property for maximum history length for finished jobs */
    public static final String CONNECTION_TIMEOUT = PREFIX + "connection.timeout";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "host[:port][/workdir]" };

    /** List of properties supported by this SSH adaptor */
    private static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(AUTOMATICALLY_ADD_HOST_KEY, Type.BOOLEAN, "true", "Automatically add unknown host keys to known_hosts."),
            new XenonPropertyDescription(STRICT_HOST_KEY_CHECKING, Type.BOOLEAN, "true", "Enable strict host key checking."),
            new XenonPropertyDescription(LOAD_STANDARD_KNOWN_HOSTS, Type.BOOLEAN, "true", "Load the standard known_hosts file."),
            new XenonPropertyDescription(LOAD_SSH_CONFIG, Type.BOOLEAN, "true", "Load the OpenSSH config file."),
            new XenonPropertyDescription(SSH_CONFIG_FILE, Type.STRING, null, "OpenSSH config filename."),
            new XenonPropertyDescription(AGENT, Type.BOOLEAN, "false", "Use a (local) ssh-agent."),
            new XenonPropertyDescription(AGENT_FORWARDING, Type.BOOLEAN, "false", "Use ssh-agent forwarding when setting up a connection."),
            new XenonPropertyDescription(CONNECTION_TIMEOUT, Type.NATURAL, "10000",
                    "The timeout for creating and authenticating connections (in milliseconds)."),
            new XenonPropertyDescription(BUFFER_SIZE, Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

    public SftpFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public boolean canCreateSymboliclinks() {
        // SFTP can create symbolic links.
        return true;
    }

    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {

        LOGGER.debug("new SftpFileSystem location = {} credential = {} properties = {}", location, credential, properties);

        if (credential == null) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Credential may not be null");
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        boolean loadSSHConfig = xp.getBooleanProperty(LOAD_SSH_CONFIG);
        boolean strictHostCheck = xp.getBooleanProperty(STRICT_HOST_KEY_CHECKING);
        boolean addHostKey = xp.getBooleanProperty(AUTOMATICALLY_ADD_HOST_KEY);
        boolean useSSHAgent = xp.getBooleanProperty(AGENT);
        boolean useAgentForwarding = xp.getBooleanProperty(AGENT_FORWARDING);

        SshClient client = SSHUtil.createSSHClient(loadSSHConfig, strictHostCheck, addHostKey, useSSHAgent, useAgentForwarding);

        long timeout = xp.getNaturalProperty(CONNECTION_TIMEOUT);

        ClientSession session = SSHUtil.connect(ADAPTOR_NAME, client, location, credential, timeout);

        SftpClient sftpClient = null;

        try {
            sftpClient = session.createSftpClient();
        } catch (IOException e) {
            client.close(true);
            throw new XenonException(ADAPTOR_NAME, "Failed to create SFTP session", e);
        }

        String cwd = null;

        try {
            cwd = getCurrentWorkingDirectory(sftpClient, location);
        } catch (Exception e) {
            client.close(true);
            throw e;
        }

        return new SftpFileSystem(getNewUniqueID(), ADAPTOR_NAME, location, new Path(cwd), (int) bufferSize, sftpClient, xp);
    }

    private String getCurrentWorkingDirectory(SftpClient sftpClient, String location) throws XenonException {

        String pathFromURI;

        try {
            pathFromURI = new URI("sftp://" + location).getPath();
        } catch (URISyntaxException e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        }

        try {
            if (pathFromURI == null || pathFromURI.isEmpty()) {
                return sftpClient.canonicalPath(".");
            }
        } catch (IOException e) {
            throw new XenonException(getName(), "Could not retrieve current working directory", e);
        }

        try {
            sftpClient.lstat(pathFromURI);
        } catch (IOException e) {
            // Path does not exist!
            throw new NoSuchPathException(ADAPTOR_NAME, "Specified working directory does not exist: " + pathFromURI);
        }

        return pathFromURI;
    }

    @Override
    public boolean supportsReadingPosixPermissions() {
        return true;
    }

    @Override
    public boolean supportsSettingPosixPermissions() {
        return true;
    }
}
