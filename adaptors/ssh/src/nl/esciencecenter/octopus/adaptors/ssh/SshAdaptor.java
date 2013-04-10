package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.credentials.CredentialsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SshAdaptor extends Adaptor {
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
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, properties);

        this.filesAdaptor = new SshFiles(properties, this, octopusEngine);
        this.jobsAdaptor = new SshJobs(properties, this, octopusEngine);
        this.credentialsAdaptor = new SshCredentials(properties, this, octopusEngine);
        jsch = new JSch();
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
    public SshFiles filesAdaptor() {
        return filesAdaptor;
    }

    @Override
    public SshJobs jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public CredentialsAdaptor credentialsAdaptor() {
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

    // idee: adaptor handelt alle sessions en channels af, er zitten nl beperkingen op het aantal channels per session, etc.
    // TODO cache van sessions / channels

    protected Session getSession(String user, String host, int port) {
        Session session;
        try {
            session = jsch.getSession(user, host, port);
            // session.setPassword("password");
            session.connect();
            return session;
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ChannelSftp getSftpChannel(Session session) {
        Channel channel;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ChannelSftp getSftpChannel(String user, String host, int port) {
        Session session = getSession(user, host, port);
        return getSftpChannel(session);
    }

    protected void closeSession(Session session) {
        session.disconnect();
    }

    protected void closeChannel(Channel channel) {
        channel.disconnect();
    }
}
