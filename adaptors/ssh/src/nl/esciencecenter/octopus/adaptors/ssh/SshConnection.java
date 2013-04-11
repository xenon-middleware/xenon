package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.ArrayList;

import nl.esciencecenter.octopus.engine.credentials.CredentialImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

class SshConnection {
    static final int MAX_CHANNELS_PER_SESSION = 10;

    class Channel {
        ChannelSftp channel;
        String type; // e.g. "sftp"
    }

    private Session session;
    private CredentialImplementation credential;
    private URI destination;
    private ArrayList<Channel> channelsInUse;
    private ArrayList<Channel> channelsFree;

    private boolean closed = true;

    protected ChannelSftp getSftpConnection() {
        synchronized (this) {
            if (channelsFree.size() > 0) {
                return channelsFree.remove(channelsFree.size() - 1).channel;
            } else {
                // TODO
            }

        }

        // we have to create a new channel
        // TODO
        return null;
    }

    protected void close() {
        synchronized (this) {
            if (closed) {
                throw new OctopusRuntimeException("ssh", "Trying to close an already closed connection");
            }
            if (channelsInUse.size() != 0) {
                throw new OctopusRuntimeException("ssh", "Trying to close connection with open channels");
            }

            closed = true;
        }

        while (channelsFree.size() > 0) {
            Channel channel = channelsFree.remove(channelsFree.size() - 1);
            channel.channel.disconnect();
        }
    }
}