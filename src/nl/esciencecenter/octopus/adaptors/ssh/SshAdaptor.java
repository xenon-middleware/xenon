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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.CredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.ConnectionLostException;
import nl.esciencecenter.octopus.exceptions.EndOfFileException;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.NotConnectedException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.PermissionDeniedException;
import nl.esciencecenter.octopus.exceptions.UnsupportedIOOperationException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

// TODO cache van sessions / channels

public class SshAdaptor extends Adaptor {

    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    public static final String ADAPTOR_NAME = "ssh";
    
    private static final int DEFAULT_PORT = 22; // The default ssh port.

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
            { MAX_HISTORY, "1000", "Int: the maximum history length for finished jobs." },
            { POLLING_DELAY, "1000", "Int: the polling delay for monitoring running jobs (in milliseconds)." }, 
            { MULTIQ_MAX_CONCURRENT, "4", "Int: the maximum number of concurrent jobs in the multiq." } };
        
    class Robot implements UserInfo {

        private final boolean yesNo;
        
        Robot(boolean yesyNo) { 
            this.yesNo = yesyNo;
        }
        
        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public boolean promptYesNo(String message) {
            return yesNo;
        }

        @Override
        public void showMessage(String message) {
            // ignored
        } 
    }

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

    void checkURI(URI location) throws OctopusException {
        if (!supports(location.getScheme())) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "SSH adaptor does not support scheme " + location.getScheme());
        }
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
            return new EndOfFileException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
            return new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_PERMISSION_DENIED:
            return new PermissionDeniedException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_FAILURE:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_BAD_MESSAGE:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_NO_CONNECTION:
            return new NotConnectedException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_CONNECTION_LOST:
            return new ConnectionLostException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_OP_UNSUPPORTED:
            return new UnsupportedIOOperationException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        default:
            return new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    void setKnownHostsFile(String knownHostsFile) throws OctopusException {
        try {
            jsch.setKnownHosts(knownHostsFile);
        } catch (JSchException e) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Could not set known_hosts file", e);
        }

        if (logger.isDebugEnabled()) {
            HostKeyRepository hkr = jsch.getHostKeyRepository();
            HostKey[] hks = hkr.getHostKey();
            if (hks != null) {
                logger.debug("Host keys in " + hkr.getKnownHostsRepositoryID());
                for (HostKey hk : hks) {
                    logger.debug(hk.getHost() + " " + hk.getType() + " " + hk.getFingerPrint(jsch));
                }
                logger.debug("");
            } else { 
                logger.debug("No keys in " + knownHostsFile);
            }
        }
    }

    protected Credential getDefaultCredential() throws OctopusException {
        // FIXME implement agent forwarding

        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot get user home directory.");
        }

        String userName = System.getProperty("user.name");
        if (userName == null) {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot get user name.");
        }

        File keyFile = new File(userHome + File.separator + ".ssh" + File.separator + "id_dsa");
        File certFile = new File(userHome + File.separator + ".ssh" + File.separator + "id_dsa.pub");

        if (keyFile.exists() && certFile.exists()) {
            logger.info("Using default credential: "+ keyFile.getPath());
            return octopusEngine.credentials().newCertificateCredential("ssh", getProperties(), keyFile.getPath(),
                    certFile.getPath(), userName, "");
        }

        File keyFile2 = new File(userHome + File.separator + ".ssh" + File.separator + "id_rsa");
        File certFile2 = new File(userHome + File.separator + ".ssh" + File.separator + "id_rsa.pub");

        if (keyFile2.exists() && certFile2.exists()) {
            logger.info("Using default credential: "+ keyFile2.getPath());
            return octopusEngine.credentials().newCertificateCredential("ssh", getProperties(), keyFile2.getPath(),
                    certFile2.getPath(), userName, "");
        }

        throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot create a default credential for ssh, tried " + 
                keyFile.getPath() + " and " + keyFile2.getPath());
    }

    private void setCredential(CredentialImplementation credential, Session session) throws OctopusException {
        logger.debug("using credential: " + credential);

        if (credential instanceof CertificateCredentialImplementation) {
            CertificateCredentialImplementation certificate = (CertificateCredentialImplementation) credential;
            try {
                jsch.addIdentity(certificate.getKeyfile(), Arrays.toString(certificate.getPassword()));
            } catch (JSchException e) {
                throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Could not read private key file.", e);
            }
        } else if (credential instanceof PasswordCredentialImplementation) {
            PasswordCredentialImplementation passwordCredential = (PasswordCredentialImplementation) credential;
            session.setPassword(Arrays.toString(passwordCredential.getPassword()));
        } else {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Unknown credential type.");
        }
    }

    protected Session createNewSession(String uniqueID, URI location, Credential credential, OctopusProperties localProperties)
            throws OctopusException {
        
        URI uri = location;
        String user = uri.getUserInfo();
        String host = uri.getHost();
        int port = uri.getPort();

        if (credential == null) {
            credential = getDefaultCredential();
        }

        if (port < 0) {
            port = DEFAULT_PORT;
        }
        if (host == null) {
            host = "localhost";
        }

        String credentialUserName = ((CredentialImplementation) credential).getUsername();
        if (user != null && credentialUserName != null && !user.equals(credentialUserName)) {
            throw new BadParameterException(SshAdaptor.ADAPTOR_NAME,
                    "If a user name is given in the URI, it must match the one in the credential");
        }

        if (user == null) {
            user = credentialUserName;
        }

        if (user == null) {
            throw new BadParameterException(SshAdaptor.ADAPTOR_NAME, "No user name given. Specify it in URI or credential.");
        }

        logger.debug("creating new session to " + user + "@" + host + ":" + port);

        Session session;
        try {
            session = jsch.getSession(user, host, port);
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }

        setCredential((CredentialImplementation) credential, session);

        if (localProperties.getBooleanProperty(STRICT_HOST_KEY_CHECKING)) {
            logger.debug("strict host key checking enabled");
       
            if (localProperties.getBooleanProperty(AUTOMATICALLY_ADD_HOST_KEY)) { 
                logger.debug("automatically add host key to known_hosts");
                session.setConfig("StrictHostKeyChecking", "ask");
                session.setUserInfo(new Robot(true));
            } else { 
                session.setConfig("StrictHostKeyChecking", "yes");
            }
        } else {
            logger.debug("strict host key checking disabled");
            session.setConfig("StrictHostKeyChecking", "no");
        }
        
        if (localProperties.getBooleanProperty(LOAD_STANDARD_KNOWN_HOSTS)) {
            String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
            logger.debug("setting ssh known hosts file to: " + knownHosts);
            setKnownHostsFile(knownHosts);
        }

        try {
            session.connect();
        } catch (JSchException e) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

        return session;
    }

   
    /**
     * Get a new exec channel. The channel is not connected yet, because the input and output streams should be set before
     * connecting.
     * 
     * @param session
     *            The authenticated session.
     * @return the channel
     * @throws OctopusIOException
     */
    protected ChannelExec getExecChannel(Session session) throws OctopusIOException {
        ChannelExec channel;

        try {
            channel = (ChannelExec) session.openChannel("exec");
            return channel;
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    protected void putExecChannel(ChannelExec channel) {
        channel.disconnect();
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO Auto-generated method stub
        return null;
    }
}
