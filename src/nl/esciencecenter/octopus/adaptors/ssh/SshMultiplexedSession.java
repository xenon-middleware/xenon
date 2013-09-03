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
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
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

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
class SshMultiplexedSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshMultiplexedSession.class);

    private final JSch jsch;
    private final OctopusProperties properties;

    private Credential credential;

    private SshLocation location;
    private SshLocation gatewayLocation;
    
    private SshSession gatewaySession;

    private int nextSessionID = 0;

    private List<SshSession> sessions = new ArrayList<>();

    SshMultiplexedSession(SshAdaptor adaptor, JSch jsch, SshLocation location, Credential cred, OctopusProperties properties)
            throws OctopusException, OctopusIOException {

        LOGGER.debug("SSHSESSION(..,..,{},..,{}", location, properties);

        this.jsch = jsch;
        this.location = location;
        this.properties = properties;
        
        credential = cred;
        
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

        String credentialUserName = ((CredentialImplementation) credential).getUsername();

        if (location.getUser() == null) { 
            if (credentialUserName == null) { 
                throw new BadParameterException(SshAdaptor.ADAPTOR_NAME, "No user name given. Specify it in location or credential.");
            }
        
            location = new SshLocation(credentialUserName, location.getHost(), location.getPort());
        }

        LOGGER.debug("Checking property: " + SshAdaptor.GATEWAY);

        if (properties.propertySet(SshAdaptor.GATEWAY)) {

            try {
                gatewayLocation = SshLocation.parse(properties.getStringProperty(SshAdaptor.GATEWAY));
            } catch (InvalidLocationException e) {
                throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Failed to parse gateway URI!", e);
            }

            if (gatewayLocation.getUser() == null) { 
                gatewayLocation = new SshLocation(location.getUser(), gatewayLocation.getHost(), gatewayLocation.getPort());
            }

            gatewaySession = createSession(jsch, -1, gatewayLocation, credential, null, null, properties);
        }

        createSession();
    }

    private synchronized SshSession findSession(Channel c) throws OctopusIOException {
        try {
            return findSession(c.getSession());
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to retrieve Session from SSH Channel!", e);
        }
    }

    private synchronized SshSession findSession(Session s) throws OctopusIOException {

        for (int i = 0; i < sessions.size(); i++) {
            SshSession info = sessions.get(i);

            if (info != null && info.getSession() == s) {
                return info;
            }
        }

        throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "SSH Session not found!");
    }

    private synchronized SshSession createSession() throws OctopusIOException, OctopusException {
        SshSession s = createSession(jsch, nextSessionID++, location, credential, gatewaySession, gatewayLocation, properties);
        sessions.add(s);
        return s;
    }

    private static synchronized SshSession createSession(JSch jsch, int sessionID, SshLocation location, Credential credential,
            SshSession gateway, SshLocation gatewayLocation, OctopusProperties properties) throws OctopusIOException, 
                OctopusException {

        String sessionHost = location.getHost();
        int sessionPort = location.getPort();
        int tunnelPort = -1;

        LOGGER.debug("SSHSESSION: Creating new session to " + location);

        if (gateway != null) {
            LOGGER.debug("SSHSESSION: Using tunnel to " + gatewayLocation);

            tunnelPort = gateway.addTunnel(0, location.getHost(), location.getPort());
            sessionPort = tunnelPort;
            sessionHost = "localhost";
        }

        Session session = null;

        try {
            session = jsch.getSession(location.getUser(), sessionHost, sessionPort);
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

        return new SshSession(session, tunnelPort, sessionID);
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
            SshSession s = sessions.get(i);

            ChannelExec channel = s.getExecChannel();

            if (channel != null) {
                return channel;
            }
        }

        try {
            SshSession s = createSession();
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
            SshSession s = sessions.get(i);

            ChannelSftp channel = s.getSftpChannel();

            if (channel != null) {
                return channel;
            }
        }

        try {
            SshSession s = createSession();
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

    synchronized void disconnect() {

        while (sessions.size() > 0) {

            SshSession s = sessions.remove(0);

            if (s != null) {
                s.disconnect();

                int tunnelPort = s.getTunnelPort();

                if (tunnelPort > 0) {
                    try {
                        gatewaySession.removeTunnel(tunnelPort);
                    } catch (OctopusIOException e) {
                        LOGGER.warn("Failed to remove SSH tunnel at localhost:" + tunnelPort);
                    }
                }
            }
        }

        if (gatewaySession != null) {
            gatewaySession.disconnect();
        }
    }
}
