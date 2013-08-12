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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Type;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Level;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusPropertyDescriptionImplementation;
import nl.esciencecenter.octopus.engine.util.ImmutableArray;
import nl.esciencecenter.octopus.exceptions.ConnectionLostException;
import nl.esciencecenter.octopus.exceptions.EndOfFileException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.NotConnectedException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.PermissionDeniedException;
import nl.esciencecenter.octopus.exceptions.UnsupportedIOOperationException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class SshAdaptor extends Adaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "ssh";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 22;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The SSH adaptor implements all functionality with remove ssh servers.";

    /** The schemes supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("ssh", "sftp");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "ssh.";

    /** Enable strict host key checking. */
    public static final String STRICT_HOST_KEY_CHECKING = PREFIX + "strictHostKeyChecking";

    /** Load the known_hosts file by default. */
    public static final String LOAD_STANDARD_KNOWN_HOSTS = PREFIX + "loadKnownHosts";

    /** Enable strict host key checking. */
    public static final String AUTOMATICALLY_ADD_HOST_KEY = PREFIX + "autoAddHostKey";

    /** Add gateway to access machine. */
    public static final String GATEWAY = PREFIX + "gateway";

    /** All our own queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** Property for maximum history length for finished jobs */
    public static final String POLLING_DELAY = QUEUE + "pollingDelay";

    /** Local multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Property for the maximum number of concurrent jobs in the multi queue. */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** List of properties supported by this SSH adaptor */
    private static final ImmutableArray<OctopusPropertyDescription> VALID_PROPERTIES = new ImmutableArray<OctopusPropertyDescription>(
            new OctopusPropertyDescriptionImplementation(AUTOMATICALLY_ADD_HOST_KEY, Type.BOOLEAN, EnumSet.of(Level.SCHEDULER,
                    Level.FILESYSTEM), "true", "Automatically add unknown host keys to known_hosts."),
            new OctopusPropertyDescriptionImplementation(STRICT_HOST_KEY_CHECKING, Type.BOOLEAN, EnumSet.of(Level.SCHEDULER,
                    Level.FILESYSTEM), "true", "Enable strict host key checking."), 
            new OctopusPropertyDescriptionImplementation(LOAD_STANDARD_KNOWN_HOSTS, Type.BOOLEAN, EnumSet.of(Level.OCTOPUS), 
                    "true", "Load the standard known_hosts file."), 
            new OctopusPropertyDescriptionImplementation(POLLING_DELAY, Type.LONG, EnumSet.of(Level.SCHEDULER), "1000",
                    "The polling delay for monitoring running jobs (in milliseconds)."),
            new OctopusPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER, EnumSet.of(Level.SCHEDULER), "4", 
                    "The maximum number of concurrent jobs in the multiq.."), 
            new OctopusPropertyDescriptionImplementation(GATEWAY, Type.STRING, EnumSet.of(Level.SCHEDULER, Level.FILESYSTEM), 
                    null, "The gateway machine used to create an SSH tunnel to the target."));

    private final SshFiles filesAdaptor;

    private final SshJobs jobsAdaptor;

    private final SshCredentials credentialsAdaptor;

    private JSch jsch;

    public SshAdaptor(OctopusEngine octopusEngine, Map<String, String> properties) throws OctopusException {
        this(octopusEngine, new JSch(), properties);
    }

    public SshAdaptor(OctopusEngine octopusEngine, JSch jsch, Map<String, String> properties) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, new OctopusProperties(
                VALID_PROPERTIES, Level.OCTOPUS, properties));

        this.filesAdaptor = new SshFiles(this, octopusEngine);
        this.jobsAdaptor = new SshJobs(getProperties(), this, octopusEngine);
        this.credentialsAdaptor = new SshCredentials(getProperties(), this);
        this.jsch = jsch;

        if (getProperties().getBooleanProperty(SshAdaptor.LOAD_STANDARD_KNOWN_HOSTS)) {
            String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
            LOGGER.debug("Setting ssh known hosts file to: " + knownHosts);
            setKnownHostsFile(knownHosts);
        }
    }

    private void setKnownHostsFile(String knownHostsFile) throws OctopusException {
        try {
            jsch.setKnownHosts(knownHostsFile);
        } catch (JSchException e) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Could not set known_hosts file", e);
        }

        if (LOGGER.isDebugEnabled()) {
            HostKeyRepository hkr = jsch.getHostKeyRepository();
            HostKey[] hks = hkr.getHostKey();
            if (hks != null) {
                LOGGER.debug("Host keys in " + hkr.getKnownHostsRepositoryID());
                for (HostKey hk : hks) {
                    LOGGER.debug(hk.getHost() + " " + hk.getType() + " " + hk.getFingerPrint(jsch));
                }
                LOGGER.debug("");
            } else {
                LOGGER.debug("No keys in " + knownHostsFile);
            }
        }
    }

    void checkURI(URI location) throws InvalidLocationException {
        if (!supports(location.getScheme())) {
            throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "SSH adaptor does not support scheme "
                    + location.getScheme());
        }
    }

    void checkPath(URI location, String adaptor) throws InvalidLocationException {

        String path = location.getPath();

        if (path == null || path.length() == 0 || path.equals("/")) {
            return;
        }

        throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Cannot create SSH " + adaptor + " with path (URI="
                + location.getScheme() + ")");
    }

    @Override
    public OctopusPropertyDescription[] getSupportedProperties() {
        return VALID_PROPERTIES.asArray();
    }

    @Override
    public Files filesAdaptor() {
        return filesAdaptor;
    }

    @Override
    public Jobs jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public Credentials credentialsAdaptor() {
        return credentialsAdaptor;
    }

    @Override
    public void end() {
        jobsAdaptor.end();
        filesAdaptor.end();
    }

    /*
    SSH_FX_OK
       Indicates successful completion of the operation.
    SSH_FX_EOF
      indicates end-of-file condition; for SSH_FX_READ it means that no
        more data is available in the file, and for SSH_FX_READDIR it
       indicates that no more files are contained in the directory.
    SSH_FX_NO_SUCH_FILE
       is returned when a reference is made to a file which should exist
       but doesn't.
    SSH_FX_PERMISSION_DENIED
       is returned when the authenticated user does not have sufficient
       permissions to perform the operation.
    SSH_FX_FAILURE
       is a generic catch-all error message; it should be returned if an
       error occurs for which there is no more specific error code
       defined.
    SSH_FX_BAD_MESSAGE
       may be returned if a badly formatted packet or protocol
       incompatibility is detected.
    SSH_FX_NO_CONNECTION
       is a pseudo-error which indicates that the client has no
       connection to the server (it can only be generated locally by the
       client, and MUST NOT be returned by servers).
    SSH_FX_CONNECTION_LOST
       is a pseudo-error which indicates that the connection to the
       server has been lost (it can only be generated locally by the
       client, and MUST NOT be returned by servers).
    SSH_FX_OP_UNSUPPORTED
       indicates that an attempt was made to perform an operation which
       is not supported for the server (it may be generated locally by
       the client if e.g.  the version number exchange indicates that a
       required feature is not supported by the server, or it may be
       returned by the server if the server does not implement an
       operation).
    */
    OctopusIOException sftpExceptionToOctopusException(SftpException e) {
        switch (e.id) {
        case ChannelSftp.SSH_FX_OK:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_EOF:
            return new EndOfFileException(SshAdaptor.ADAPTOR_NAME, "Unexpected EOF", e);
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
            return new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "No such file", e);
        case ChannelSftp.SSH_FX_PERMISSION_DENIED:
            return new PermissionDeniedException(SshAdaptor.ADAPTOR_NAME, "Permission denied", e);
        case ChannelSftp.SSH_FX_FAILURE:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "SSH gave an unknown error", e);
        case ChannelSftp.SSH_FX_BAD_MESSAGE:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "SSH received a malformed message", e);
        case ChannelSftp.SSH_FX_NO_CONNECTION:
            return new NotConnectedException(SshAdaptor.ADAPTOR_NAME, "SSH does not have a connection!", e);
        case ChannelSftp.SSH_FX_CONNECTION_LOST:
            return new ConnectionLostException(SshAdaptor.ADAPTOR_NAME, "SSH lost connection!", e);
        case ChannelSftp.SSH_FX_OP_UNSUPPORTED:
            return new UnsupportedIOOperationException(SshAdaptor.ADAPTOR_NAME, "Unsupported operation", e);
        default:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Unknown SSH exception", e);
        }
    }

    protected SshMultiplexedSession createNewSession(URI location, Credential credential, OctopusProperties properties)
            throws OctopusException, OctopusIOException {
        return new SshMultiplexedSession(this, jsch, location, credential, properties);
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO Auto-generated method stub
        return null;
    }
}
