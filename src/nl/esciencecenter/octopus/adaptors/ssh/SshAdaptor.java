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
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.SftpException;

public class SshAdaptor extends Adaptor {

    // private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    public static final String ADAPTOR_NAME = "ssh";

    protected static final int DEFAULT_PORT = 22; // The default ssh port.

    private static final String ADAPTOR_DESCRIPTION = "The SSH adaptor implements all functionality with remove ssh servers.";

    private static final String[] ADAPTOR_SCHEME = new String[] { "ssh", "sftp" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "ssh.";

    /** Enable strict host key checking. */
    public static final String STRICT_HOST_KEY_CHECKING = PREFIX + "strictHostKeyChecking";

    /** Load the known_hosts file by default. */
    public static final String LOAD_STANDARD_KNOWN_HOSTS = PREFIX + "loadKnownHosts";

    /** Enable strict host key checking. */
    public static final String AUTOMATICALLY_ADD_HOST_KEY = PREFIX + "autoAddHostKey";

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

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] {
            { AUTOMATICALLY_ADD_HOST_KEY, "true", "Boolean: automatically add unknown host keys to known_hosts." },
            { STRICT_HOST_KEY_CHECKING, "true", "Boolean: enable strict host key checking." },
            { LOAD_STANDARD_KNOWN_HOSTS, "true", "Boolean: load the standard known_hosts file." },
            { POLLING_DELAY, "1000", "Int: the polling delay for monitoring running jobs (in milliseconds)." },
            { MULTIQ_MAX_CONCURRENT, "4", "Int: the maximum number of concurrent jobs in the multiq." } };

    private final SshFiles filesAdaptor;

    private final SshJobs jobsAdaptor;

    private final SshCredentials credentialsAdaptor;

    private JSch jsch;

    public SshAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        this(properties, octopusEngine, new JSch());
    }

    public SshAdaptor(OctopusProperties properties, OctopusEngine octopusEngine, JSch jsch) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, properties);

        this.filesAdaptor = new SshFiles(getProperties(), this, octopusEngine);
        this.jobsAdaptor = new SshJobs(getProperties(), this, octopusEngine);
        this.credentialsAdaptor = new SshCredentials(getProperties(), this, octopusEngine);
        this.jsch = jsch;
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
    public Map<String, String> getSupportedProperties() {
        return new HashMap<String, String>();
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

    protected SshSession createNewSession(URI location, Credential credential, OctopusProperties properties)
            throws OctopusException {
        return new SshSession(this, jsch, location, credential, properties);
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO Auto-generated method stub
        return null;
    }
}
