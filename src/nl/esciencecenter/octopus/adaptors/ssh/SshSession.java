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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.CredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
class SshSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshSession.class);

    private static final int MAX_OPEN_CHANNELS = 7;

    static class SessionInfo {

        private final int sessionID;
        private final Session session;
        private ChannelSftp sftpChannelCache;
        private int openChannels = 0;
        private int tunnelPort = -1; 

        SessionInfo(Session session, int sessionID) {
            this.session = session;
            this.sessionID = sessionID;
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

        ChannelExec getExecChannel() throws OctopusIOException {

            if (openChannels == MAX_OPEN_CHANNELS) {
                return null;
            }

            ChannelExec channel = null;

            try {
                LOGGER.debug("SSHSESSION-{}: Creating EXEC channel {}", sessionID, openChannels);
                channel = (ChannelExec) session.openChannel("exec");
            } catch (JSchException e) {
                LOGGER.debug("SSHSESSION-{}: Failed to create EXEC channel {}", sessionID, openChannels, e);
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
            }

            incOpenChannels("EXEC");
            return channel;
        }

        ChannelSftp getSftpChannel() throws OctopusIOException {

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
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
            }

            incOpenChannels("SFTP");
            return channel;
        }
        
        int addTunnel(int localPort, String targetHost, int targetPort) throws OctopusIOException { 

            LOGGER.debug("SSHSESSION-{}: Creating tunnel from localhost:{} via {}:{} to {}:{}", sessionID, localPort, 
                    session.getHost(), session.getPort(), targetHost, targetPort);
            
            if (tunnelPort > 0) { 
                LOGGER.debug("SSHSESSION-{}: Tunnel already in used for this session!", sessionID);
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Tunnel already in use!");
            }
            
            try {
                tunnelPort = session.setPortForwardingL(0, targetHost, targetPort);
            } catch (JSchException e) {
                LOGGER.debug("SSHSESSION-{}: Failed to create tunnel to {}:{}", sessionID, targetHost, targetPort, e);
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
            }
            
            return tunnelPort;
        }

        void removeTunnel(int localPort) throws OctopusIOException {
            
            LOGGER.debug("SSHSESSION-{}: Removing tunnel at localhost:{}", sessionID, localPort);
            
            if (localPort != tunnelPort) { 
                LOGGER.debug("SSHSESSION-{}: Tunnel is not at port {} but at port {}!", sessionID, localPort, tunnelPort);
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Tunnel is not at port " + localPort + " but at port " 
                        + tunnelPort + "!");                
            }
            
            try {
                tunnelPort = -1;
                session.delPortForwardingL(localPort);
            } catch (JSchException e) {
                LOGGER.debug("SSHSESSION-{}: Failed to remove tunnel at localhost:{}", sessionID, localPort, e);
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
            }
        }
    }

    private final JSch jsch;
    private final OctopusProperties properties;

    private Credential credential;
    
    private String user;
    private String host;
    private int port;

    private SshSession gatewaySession;
    private URI gatewayURI;
    private int localTunnelPort;
    
    private int nextSessionID = 0;

    private List<SessionInfo> sessions = new ArrayList<>();

    static class Robot implements UserInfo {

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

    SshSession(SshAdaptor adaptor, JSch jsch, URI location, Credential cred, OctopusProperties properties)
            throws OctopusException, OctopusIOException {

        LOGGER.debug("SSHSESSION(..,..,{},..,{}", location, properties);
        
        this.jsch = jsch;
        this.properties = properties;
        credential = cred;

        user = location.getUserInfo();
        host = location.getHost();
        port = location.getPort();
        
        if (credential == null) {
            credential = adaptor.credentialsAdaptor().getDefaultCredential("ssh");
        }

        if (credential instanceof CertificateCredentialImplementation) {
            CertificateCredentialImplementation certificate = (CertificateCredentialImplementation) credential;
            try {
                jsch.addIdentity(certificate.getCertfile(), Arrays.toString(certificate.getPassword()));
            } catch (JSchException e) {
                throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Could not read private key file.", e);
            }
        } else if (credential instanceof PasswordCredentialImplementation) {
            // handled per session
        } else {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Unknown credential type.");
        }

        if (port <= 0) {
            port = SshAdaptor.DEFAULT_PORT;
        }

        if (host == null) {
            host = "localhost";
        }

        String credentialUserName = ((CredentialImplementation) credential).getUsername();

        if (user == null) {
            user = credentialUserName;
        }

        if (user == null) {
            throw new BadParameterException(SshAdaptor.ADAPTOR_NAME, "No user name given. Specify it in URI or credential.");
        }

        LOGGER.debug("Checking property: " + SshAdaptor.GATEWAY);
        
        if (properties.propertySet(SshAdaptor.GATEWAY)) {
            try {
                gatewayURI = new URI(properties.getStringProperty(SshAdaptor.GATEWAY));
                gatewaySession = new SshSession(adaptor, jsch, gatewayURI, cred, properties.clear(SshAdaptor.GATEWAY));
            } catch (URISyntaxException e) {
                throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Failed to create gateway!", e);
            }
        }
        
        createSession();
    }

    private SessionInfo storeSession(Session s) {
        SessionInfo info = new SessionInfo(s, nextSessionID++);
        sessions.add(info);
        return info;
    }

    private SessionInfo findSession(Channel c) throws OctopusIOException {
        try {
            return findSession(c.getSession());
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to retrieve Session from SSH Channel!", e);
        }
    }

    private SessionInfo findSession(Session s) throws OctopusIOException {

        for (int i = 0; i < sessions.size(); i++) {
            SessionInfo info = sessions.get(i);

            if (info != null && info.session == s) {
                return info;
            }
        }

        throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "SSH Session not found!");
    }

    private SessionInfo createSession() throws OctopusIOException, OctopusException {

        String sessionHost = host;
        int sessionPort = port;
        
        LOGGER.debug("SSHSESSION: Creating new session to " + user + "@" + host + ":" + port);
        
        if (gatewaySession != null) { 
            LOGGER.debug("SSHSESSION: Using tunnel " + gatewayURI);
    
            sessionPort = gatewaySession.createTunnel(0, host, port);
            sessionHost = "localhost";
        
            LOGGER.debug("SSHSESSION: Rerouting session via " + user + "@" + sessionHost + ":" + sessionPort);
        }
        
        Session session = null;
        
        try {
            session = jsch.getSession(user, sessionHost, sessionPort);
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to create SSH session!", e);
        }

        if (credential instanceof PasswordCredentialImplementation) {
            PasswordCredentialImplementation passwordCredential = (PasswordCredentialImplementation) credential;
            session.setPassword(new String(passwordCredential.getPassword()));
        }
        
        if (properties.getBooleanProperty(SshAdaptor.STRICT_HOST_KEY_CHECKING)) {
            LOGGER.debug("SSHSESSION: Strict host key checking enabled");

            if (properties.getBooleanProperty(SshAdaptor.AUTOMATICALLY_ADD_HOST_KEY)) {
                LOGGER.debug("SSHSESSION: Automatically add host key to known_hosts");
                session.setConfig("StrictHostKeyChecking", "ask");
                session.setUserInfo(new Robot(true));
            } else {
                session.setConfig("StrictHostKeyChecking", "yes");
            }
        } else {
            LOGGER.debug("SSHSESSION: Strict host key checking disabled");
            session.setConfig("StrictHostKeyChecking", "no");
        }
       
        try {
            session.connect();
        } catch (JSchException e) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

        return storeSession(session);
    }

    /**
     * Get a new exec channel. The channel is not connected yet, because the input and output streams should be set before
     * connecting.
     * 
     * @return the channel
     * @throws OctopusIOException
     */
    synchronized ChannelExec getExecChannel() throws OctopusIOException {

        for (int i = 0; i < sessions.size(); i++) {
            SessionInfo s = sessions.get(i);

            ChannelExec channel = s.getExecChannel();

            if (channel != null) {
                return channel;
            }
        }

        try {
            SessionInfo s = createSession();
            return s.getExecChannel();
        } catch (OctopusException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to create new SSH session!", e);
        }
    }

    synchronized void releaseExecChannel(ChannelExec channel) throws OctopusIOException {
        findSession(channel).releaseExecChannel(channel);
    }

    synchronized void failedExecChannel(ChannelExec channel) throws OctopusIOException {
        findSession(channel).failedExecChannel(channel);
    }

    /**
     * Get a connected channel for doing sftp operations.
     * 
     * @return the channel
     * @throws OctopusIOException
     */
    synchronized ChannelSftp getSftpChannel() throws OctopusIOException {

        for (int i = 0; i < sessions.size(); i++) {
            SessionInfo s = sessions.get(i);

            ChannelSftp channel = s.getSftpChannel();

            if (channel != null) {
                return channel;
            }
        }

        try {
            SessionInfo s = createSession();
            return s.getSftpChannel();
        } catch (OctopusException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to create new SSH session!", e);
        }
    }

    synchronized void releaseSftpChannel(ChannelSftp channel) throws OctopusIOException {
        findSession(channel).releaseSftpChannel(channel);
    }

    synchronized void failedSftpChannel(ChannelSftp channel) throws OctopusIOException {
        findSession(channel).failedSftpChannel(channel);
    }

    synchronized int createTunnel(int localPort, String targetHost, int targetPort) throws OctopusIOException {

        for (int i = 0; i < sessions.size(); i++) {
            SessionInfo s = sessions.get(i);

            int resultPort = s.addTunnel(localPort, targetHost, targetPort);

            if (resultPort > 0) { 
                return resultPort;
            }
        }

        try {
            SessionInfo s = createSession();
            return s.addTunnel(localPort, targetHost, targetPort);
        } catch (OctopusException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to create new SSH session!", e);
        }
    }

    synchronized void removeTunnel(int localPort) throws OctopusIOException {

        for (int i = 0; i < sessions.size(); i++) {
            SessionInfo s = sessions.get(i);
            
            if (s.tunnelPort == localPort) { 
                s.removeTunnel(localPort);
                return;
            } 
        }

        throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to find tunnel at !" + localPort);
    }
    
    synchronized void disconnect() {

        while (sessions.size() > 0) {
            SessionInfo s = sessions.remove(0);

            if (s != null) {
                s.disconnect();
            }
        }
    }
}
