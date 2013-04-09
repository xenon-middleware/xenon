package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshAdaptor implements Adaptor {

    public static final String SSH_MULTIQ_MAX_JOBS = "ssh.multiq.max.concurrent.jobs";
    public static final String SSH_Q_HISTORY_SIZE = "ssh.q.history.size";

    public static final int DEFAULT_SSH_Q_HISTORY_SIZE = 1000;

    private final OctopusEngine octopusEngine;

    private final SshFiles filesAdaptor;

    private final SshJobs jobsAdaptor;

    private JSch jsch;

    public SshAdaptor(ImmutableTypedProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        this.octopusEngine = octopusEngine;
        this.filesAdaptor = new SshFiles(properties, this, octopusEngine);
        this.jobsAdaptor = new SshJobs(properties, this, octopusEngine);
        jsch = new JSch();
    }

    @Override
    public String[] getSupportedSchemes() {
        return new String[] { "ssh", "sftp" };
    }

    // TODO move to base class
    @Override
    public boolean supports(String scheme) {
        for (String string : getSupportedSchemes()) {
            if (string.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        if (scheme == null) {
            return true;
        }

        return false;
    }

    void checkURI(URI location) throws OctopusException {
        if (!supports(location.getScheme())) {
            throw new OctopusException("Ssh adaptor does not support scheme " + location.getScheme(), getName(), location);
        }
    }

    @Override
    public String getName() {
        return "ssh";
    }

    @Override
    public String getDescription() {
        return "The Ssh adaptor implements all functionality with remove ssh servers.";
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
    public void end() {
        jobsAdaptor.end();
        filesAdaptor.end();
    }

    @Override
    public String toString() {
        return getName();
    }

    // idee: adaptor handelt alle sessions en channels af, er zitten nl beperkingen op het aantal channels per session, etc.
    // TODO cache van sessions / channels
    
    protected Session getSession(String user, String host, int port) {
        Session session;
        try {
            session = jsch.getSession(user, host, port);
            session.setPassword("password");
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
