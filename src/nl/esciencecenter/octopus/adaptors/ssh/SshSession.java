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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.CredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
class SshSession {

    private static final Logger logger = LoggerFactory.getLogger(SshSession.class);
    
    private final SshAdaptor adaptor;
    private final JSch jsch;
    
    private final URI location;
    private final OctopusProperties properties;
    
    private Credential credential;
    private String user; 
    private String host; 
    private int port; 
    
    private Session session;
    
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
    
    SshSession(SshAdaptor adaptor, JSch jsch, URI location, Credential credential, OctopusProperties properties) throws OctopusException {
    
        this.adaptor = adaptor;
        this.jsch = jsch;
        this.location = location;
        this.properties = properties;
        this.credential = credential;
        
        user = location.getUserInfo();
        host = location.getHost();
        int port = location.getPort();

        if (credential == null) {
            credential = adaptor.credentialsAdaptor().getDefaultCredential("ssh");
        }

        if (port < 0) {
            port = SshAdaptor.DEFAULT_PORT;
        }
        
        if (host == null) {
            host = "localhost";
        }

        String credentialUserName = ((CredentialImplementation) credential).getUsername();
    
//        if (user != null && credentialUserName != null && !user.equals(credentialUserName)) {
//            throw new BadParameterException(SshAdaptor.ADAPTOR_NAME,
//                    "If a user name is given in the URI, it must match the one in the credential");
//        }

        if (user == null) {
            user = credentialUserName;
        }

        if (user == null) {
            throw new BadParameterException(SshAdaptor.ADAPTOR_NAME, "No user name given. Specify it in URI or credential.");
        }

        logger.debug("creating new session to " + user + "@" + host + ":" + port);

        
        try {
            session = jsch.getSession(user, host, port);
        } catch (JSchException e) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Failed to create SSH session!", e);
        }

        setCredential((CredentialImplementation) credential, session);

        if (properties.getBooleanProperty(SshAdaptor.STRICT_HOST_KEY_CHECKING)) {
            logger.debug("strict host key checking enabled");
       
            if (properties.getBooleanProperty(SshAdaptor.AUTOMATICALLY_ADD_HOST_KEY)) { 
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
        
        if (properties.getBooleanProperty(SshAdaptor.LOAD_STANDARD_KNOWN_HOSTS)) {
            String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
            logger.debug("setting ssh known hosts file to: " + knownHosts);
            setKnownHostsFile(knownHosts);
        }

        try {
            session.connect();
        } catch (JSchException e) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
        
    }
    
    private void setKnownHostsFile(String knownHostsFile) throws OctopusException {
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
            session.setPassword(new String(passwordCredential.getPassword()));
        } else {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Unknown credential type.");
        }
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
    ChannelExec getExecChannel() throws OctopusIOException {
        ChannelExec channel;

        try {
            channel = (ChannelExec) session.openChannel("exec");
            return channel;
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    void releaseExecChannel(ChannelExec channel) {
        channel.disconnect();
    }
        
    /**
     * Get a connected channel for doing sftp operations.
     * 
     * @param session
     *            The authenticated session.
     * @return the channel
     * @throws OctopusIOException
     */
    ChannelSftp getSftpChannel() throws OctopusIOException {
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return channel;
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }
    
    void releaseSftpChannel(ChannelSftp channel) {
        channel.disconnect();
    }
    
    void disconnect() { 
        session.disconnect();
    }
}
