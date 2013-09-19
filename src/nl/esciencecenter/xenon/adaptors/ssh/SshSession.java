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

package nl.esciencecenter.xenon.adaptors.ssh;

import nl.esciencecenter.xenon.XenonException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
class SshSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshSession.class);

    private static final int MAX_OPEN_CHANNELS = 7;

    private final Session session;
    private final int tunnelPort;
    private final int sessionID;

    private ChannelSftp sftpChannelCache;
    private int openChannels = 0;

    SshSession(Session session, int tunnelPort, int sessionID) {
        this.session = session;
        this.tunnelPort = tunnelPort;
        this.sessionID = sessionID;
    }

    Session getSession() {
        return session;
    }

    int getTunnelPort() {
        return tunnelPort;
    }

    int getSessionID() {
        return sessionID;
    }

    boolean incOpenChannels(String info) {

        if (openChannels == MAX_OPEN_CHANNELS) {
            return false;
        }

        openChannels++;
        LOGGER.debug("SSHSESSION-{}: ++Open channels: {} {}", sessionID, openChannels, info);
        return true;
    }

    void decOpenChannels(String info) {
        openChannels--;
        LOGGER.debug("SSHSESSION-{}: --Open channels: {} {}", sessionID, openChannels, info);
    }

    ChannelSftp getSftpChannelFromCache() {
        ChannelSftp channel = sftpChannelCache;
        sftpChannelCache = null;
        return channel;
    }

    boolean putSftpChannelInCache(ChannelSftp channel) {
        if (sftpChannelCache != null) {
            return false;
        }

        sftpChannelCache = channel;
        return true;
    }

    void releaseExecChannel(ChannelExec channel) {
        LOGGER.debug("SSHSESSION-{}: Releasing EXEC channel", sessionID);
        channel.disconnect();
        decOpenChannels("EXEC");
    }

    void failedExecChannel(ChannelExec channel) {
        LOGGER.debug("SSHSESSION-{}: Releasing FAILED EXEC channel", sessionID);
        channel.disconnect();
        decOpenChannels("FAILED EXEC");
    }

    void releaseSftpChannel(ChannelSftp channel) {
        LOGGER.debug("SSHSESSION-{}: Releasing SFTP channel", sessionID);

        if (!putSftpChannelInCache(channel)) {
            channel.disconnect();
            decOpenChannels("SFTP");
        }
    }

    void failedSftpChannel(ChannelSftp channel) {
        LOGGER.debug("SSHSESSION-{}: Releasing FAILED SFTP channel", sessionID);
        channel.disconnect();
        decOpenChannels("FAILED SFTP");
    }

    void disconnect() {
        if (sftpChannelCache != null) {
            sftpChannelCache.disconnect();
        }

        session.disconnect();
    }

    ChannelExec getExecChannel() throws XenonException {

        if (openChannels == MAX_OPEN_CHANNELS) {
            return null;
        }

        ChannelExec channel = null;

        try {
            LOGGER.debug("SSHSESSION-{}: Creating EXEC channel {}", sessionID, openChannels);
            channel = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            LOGGER.debug("SSHSESSION-{}: Failed to create EXEC channel {}", sessionID, openChannels, e);
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

        incOpenChannels("EXEC");
        return channel;
    }

    ChannelSftp getSftpChannel() throws XenonException {

        ChannelSftp channel = getSftpChannelFromCache();

        if (channel != null) {
            LOGGER.debug("SSHSESSION-{}: Reusing SFTP channel {}", sessionID, openChannels);
            return channel;
        }

        if (openChannels == MAX_OPEN_CHANNELS) {
            return null;
        }

        try {
            LOGGER.debug("SSHSESSION-{}: Creating SFTP channel {}", sessionID, openChannels);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        } catch (JSchException e) {
            LOGGER.debug("SSHSESSION-{}: Failed to create SFTP channel {}", sessionID, openChannels, e);
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

        incOpenChannels("SFTP");
        return channel;
    }

    int addTunnel(int localPort, String targetHost, int targetPort) throws XenonException {

        LOGGER.debug("SSHSESSION-{}: Creating tunnel from localhost:{} via {}:{} to {}:{}", sessionID, localPort,
                session.getHost(), session.getPort(), targetHost, targetPort);

        if (tunnelPort > 0) {
            LOGGER.debug("SSHSESSION-{}: Tunnel already in used for this session!", sessionID);
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Tunnel already in use!");
        }

        try {
            return session.setPortForwardingL(0, targetHost, targetPort);
        } catch (JSchException e) {
            LOGGER.debug("SSHSESSION-{}: Failed to create tunnel to {}:{}", sessionID, targetHost, targetPort, e);
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    void removeTunnel(int localPort) throws XenonException {

        LOGGER.debug("SSHSESSION-{}: Removing tunnel at localhost:{}", sessionID, localPort);

        try {
            session.delPortForwardingL(localPort);
        } catch (JSchException e) {
            LOGGER.debug("SSHSESSION-{}: Failed to remove tunnel at localhost:{}", sessionID, localPort, e);
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }
}