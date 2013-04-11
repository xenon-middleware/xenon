package nl.esciencecenter.octopus.adaptors.ssh;

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
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SshAdaptor extends Adaptor {

    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    private static final int DEFAULT_PORT = 22; // The default ssh port.

    private static final String ADAPTOR_NAME = "ssh";

    private static final String ADAPTOR_DESCRIPTION = "The Ssh adaptor implements all functionality with remove ssh servers.";

    private static final String[] ADAPTOR_SCHEME = new String[] { "ssh", "sftp" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "ssh.";

    /** All our own queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** All our multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multiq.";

    /** Maximum number of concurrent jobs in the multiq */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] {
            { MAX_HISTORY, "1000", "Int: the maximum history length for finished jobs." },
            { MULTIQ_MAX_CONCURRENT, null, "Int: the maximum number of concurrent jobs in the multiq." } };

    private final SshFiles filesAdaptor;

    private final SshJobs jobsAdaptor;

    private final SshCredentials credentialsAdaptor;

    private JSch jsch;

    public SshAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        this(properties, octopusEngine, new JSch());
    }

    public SshAdaptor(OctopusProperties properties, OctopusEngine octopusEngine, JSch jsch) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, properties);

        this.filesAdaptor = new SshFiles(properties, this, octopusEngine);
        this.jobsAdaptor = new SshJobs(properties, this, octopusEngine);
        this.credentialsAdaptor = new SshCredentials(properties, this, octopusEngine);
        this.jsch = jsch;
    }

    void checkURI(URI location) throws OctopusException {
        if (!supports(location.getScheme())) {
            throw new OctopusException(getName(), "Ssh adaptor does not support scheme " + location.getScheme());
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

    @Override
    public String toString() {
        return getName();
    }

    // TODO make specific exceptions
    OctopusIOException sftpExceptionToOctopusException(SftpException e) {
        switch (e.id) {
        case ChannelSftp.SSH_FX_OK:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_EOF:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_PERMISSION_DENIED:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_FAILURE:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_BAD_MESSAGE:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_NO_CONNECTION:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_CONNECTION_LOST:
            return new OctopusIOException("ssh", e.getMessage(), e);
        case ChannelSftp.SSH_FX_OP_UNSUPPORTED:
            return new OctopusIOException("ssh", e.getMessage(), e);
        default:
            return new OctopusIOException("ssh", e.getMessage(), e);
        }
    }

    void setKnownHostsFile(String knownHostsFile) throws OctopusException {
        try {
            jsch.setKnownHosts(knownHostsFile);
        } catch (JSchException e) {
            throw new OctopusException("ssh", "Could not set known_hosts file", e);
        }

        if (logger.isTraceEnabled()) {
            HostKeyRepository hkr = jsch.getHostKeyRepository();
            HostKey[] hks = hkr.getHostKey();
            if (hks != null) {
                logger.debug("Host keys in " + hkr.getKnownHostsRepositoryID());
                for (int i = 0; i < hks.length; i++) {
                    HostKey hk = hks[i];
                    logger.debug(hk.getHost() + " " + hk.getType() + " " + hk.getFingerPrint(jsch));
                }
                logger.debug("");
            }
        }
    }

 // TODO more specific exception, octopus really is couldnotinitcredential
    // idee: adaptor handelt alle sessions en channels af, er zitten nl beperkingen op het aantal channels per session, etc.
    // TODO cache van sessions / channels
    // session.setConfig("StrictHostKeyChecking", "no");

    private CredentialImplementation getDefaultCredential() throws OctopusException {
        throw new OctopusException("ssh", "Please specify a valid credential, credential is 'null'");
    }

    private void setCredential(CredentialImplementation credential, Session session) throws OctopusException {
        logger.debug("using credential: " + credential);

        if (credential instanceof CertificateCredentialImplementation) {
            CertificateCredentialImplementation certificate = (CertificateCredentialImplementation) credential;
            try {
                jsch.addIdentity(certificate.getKeyfile(), Arrays.toString(certificate.getPassword()));
            } catch (JSchException e) {
                throw new OctopusException("ssh", "Could not read private key file.", e);
            }
        } else if (credential instanceof PasswordCredentialImplementation) {
            PasswordCredentialImplementation passwordCredential = (PasswordCredentialImplementation) credential; 
            // session.setPassword("password");
        } else {
            throw new OctopusException("ssh", "Unknown credential type.");
        }
    }

    protected Session createNewSession(String uniqueID, URI location, Credential credential) throws OctopusException {
        URI uri = location;
        String user = uri.getUserInfo();
        String host = uri.getHost();
        int port = uri.getPort();

        if (port < 0) {
            port = DEFAULT_PORT;
        }
        if (host == null) {
            host = "localhost";
        }

        logger.debug("creating new session to " + user + "@" + host + ":" + port);

        Session session;
        try {
            session = jsch.getSession(user, host, port);
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }

        if (credential == null) {
            credential = getDefaultCredential();
        }

        setCredential((CredentialImplementation)credential, session);
        
        String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
        logger.debug("setting ssh known hosts file to: " + knownHosts);

        setKnownHostsFile(knownHosts);

        try {
            session.connect();
        } catch (JSchException e) {
            throw new OctopusException(getName(), e.getMessage(), e);
        }

        return session;
    }
}
