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
package nl.esciencecenter.xenon.adaptors.shared.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.sshd.agent.local.ProxyAgentFactory;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelDirectTcpip;
import org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver;
import org.apache.sshd.client.config.hosts.KnownHostEntry;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HostAndPort;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.CredentialMap;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.credentials.UserCredential;
import nl.esciencecenter.xenon.utils.StreamForwarder;

public class SSHUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHUtil.class);

    private static final String VIA_TAG = " via:";

    public static final int DEFAULT_SSH_PORT = 22;

    static class PasswordProvider implements FilePasswordProvider {

        private final char[] password;

        public PasswordProvider(char[] password) {
            this.password = password.clone();
        }

        @Override
        public String getPassword(String resourceKey) throws IOException {
            return new String(password);
        }
    }

    static class Tunnel extends Thread {

        private final ServerSocket server;
        private final ChannelDirectTcpip channel;

        private final int bufferSize;

        private Socket socket;
        private Exception ex;

        Tunnel(ServerSocket ss, ChannelDirectTcpip tmp, int bufferSize) {
            this.server = ss;
            this.channel = tmp;
            this.bufferSize = bufferSize;
        }

        public synchronized Exception getException() {
            return ex;
        }

        public synchronized void close() {
            try {
                socket.close();
            } catch (Exception e) {
                // ignore
            }
        }

        private void closeServer() {
            try {
                server.close();
            } catch (Exception e) {
                // ignored
            }
        }

        @Override
        public void run() {
            try {
                Socket s = server.accept();
                s.setTcpNoDelay(true);

                closeServer();

                synchronized (this) {
                    socket = s;
                }

                new StreamForwarder("LOCAL TO REMOTE", s.getInputStream(), channel.getInvertedIn(), bufferSize);
                new StreamForwarder("REMOTE TO LOCAL", channel.getInvertedOut(), s.getOutputStream(), bufferSize);

            } catch (IOException e) {
                synchronized (this) {
                    ex = e;
                }
            }
        }
    }

    /**
     * This constructor is only needed for testing. Users should use the static methods instead.
     */
    public SSHUtil() {
        // for coverage
    }

    /**
     * Create a new {@link SshClient} with a default configuration similar to a stand-alone SSH client.
     * <p>
     * The default configuration loads the SSH known_hosts and config file and uses strict host key checking.
     * </p>
     *
     * @return the configured {@link SshClient}
     **/
    public static SshClient createSSHClient() {
        return createSSHClient(true, true, true, false, false);
    }

    /**
     * Create a new {@link SshClient} with the desired configuration.
     * <p>
     * SSH clients have a significant number of options. This method will create a <code>SshClient</code> providing the most important settings.
     * </p>
     *
     * @param useKnownHosts
     *            Load the SSH known_hosts file from the default location (for OpenSSH this is typically found in $HOME/.ssh/known_hosts).
     * @param loadSSHConfig
     *            Load the SSH config file from the default location (for OpenSSH this is typically found in $HOME/.ssh/config).
     * @param stricHostCheck
     *            Perform a strict host key check. When setting up a connection, the key presented by the server is compared to the default known_hosts file
     *            (for OpenSSH this is typically found in $HOME/.ssh/known_hosts).
     * @param useSSHAgent
     *            When setting up a connection, handoff authentication to a separate SSH agent process.
     * @param useAgentForwarding
     *            Support agent forwarding, allowing remote SSH servers to use the local SSH agent process to authenticate connections to other servers.
     * @return the configured {@link SshClient}
     */
    public static SshClient createSSHClient(boolean useKnownHosts, boolean loadSSHConfig, boolean stricHostCheck, boolean useSSHAgent,
            boolean useAgentForwarding) {

        SshClient client = SshClient.setUpDefaultClient();

        if (useKnownHosts) {
            DefaultKnownHostsServerKeyVerifier tmp;

            if (stricHostCheck) {
                tmp = new DefaultKnownHostsServerKeyVerifier(RejectAllServerKeyVerifier.INSTANCE, true);
            } else {
                tmp = new DefaultKnownHostsServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE, true);
                tmp.setModifiedServerKeyAcceptor(
                        (ClientSession clientSession, SocketAddress remoteAddress, KnownHostEntry entry, PublicKey expected, PublicKey actual) -> true);
            }

            client.setServerKeyVerifier(tmp);
        } else {
            client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        }

        if (loadSSHConfig) {
            client.setHostConfigEntryResolver(DefaultConfigFileHostEntryResolver.INSTANCE);
        }

        if (useSSHAgent) {
            client.setAgentFactory(new ProxyAgentFactory());
        }

        if (useAgentForwarding) {
            // Enable ssh-agent-forwarding
            LOGGER.debug("(UNIMPLEMENTED) Enabling ssh-agent-forwarding");
        }

        client.start();

        return client;
    }

    /**
     * Weak validation of a host string containing either a hostame of IP adres.
     *
     * @param adaptorName
     *            the name of the adaptor using this method.
     * @param host
     *            the hostname to validate
     * @return the value of <code>host</code> if the validation succeeded.
     * @throws InvalidLocationException
     *             if the validation failed
     */
    public static String validateHost(String adaptorName, String host) throws InvalidLocationException {
        if (host == null || host.isEmpty()) {
            throw new InvalidLocationException(adaptorName, "Failed to parse host: " + host);
        }

        return host;
    }

    public static String getHost(String adaptorName, String location) throws InvalidLocationException {
        // Parse locations of the format: hostname[:port] and return the host
        try {
            return validateHost(adaptorName, HostAndPort.fromString(location).getHostText().trim());
        } catch (Exception e) {
            // TODO: could be a name in ssh_config instead ??
            throw new InvalidLocationException(adaptorName, "Failed to parse location: " + location);
        }
    }

    public static int getPort(String adaptorName, String location) throws InvalidLocationException {
        // Parse locations of the format: hostname[:port] and return the host
        try {
            return HostAndPort.fromString(location).getPortOrDefault(DEFAULT_SSH_PORT);
        } catch (Exception e) {
            // TODO: could be a name in ssh_config instead ??
            throw new InvalidLocationException(adaptorName, "Failed to parse location: " + location);
        }
    }

    public static UserCredential extractCredential(SshdSocketAddress location, Credential credential) {

        // Figure out which type of credential we are using
        if (credential instanceof CredentialMap) {

            CredentialMap map = (CredentialMap) credential;

            String key = location.toString();

            if (map.containsCredential(key)) {
                return map.get(key);
            }

            // May return null!
            return map.get(location.getHostName());
        }

        return (UserCredential) credential;
    }

    public static UserCredential[] extractCredentials(String adaptorName, SshdSocketAddress[] locations, Credential credentials)
            throws CredentialNotFoundException {

        UserCredential[] result = new UserCredential[locations.length];

        for (int i = 0; i < locations.length; i++) {
            result[i] = extractCredential(locations[i], credentials);

            if (result[i] == null) {
                throw new CredentialNotFoundException(adaptorName, "No credential provided for location: " + locations[i]);
            }
        }

        return result;
    }

    /**
     * Extract a series of locations from a location string.
     *
     * This method will split the location string into the destination and any number of intermediate hops. The accepted format is:
     * <p>
     * "host[:port][/workdir] [via:otherhost[:port]]*"
     * </p>
     * The locations will be returned in connection setup order, which is the reverse order in which they are listed in the location string.
     *
     * @param adaptorName
     *            the adaptor calling this method (used in exceptions).
     * @param location
     *            the location to parse
     * @return the location string split into its individual locations.
     * @throws InvalidLocationException
     *             if the provided location is could not be parsed.
     */
    public static SshdSocketAddress[] extractLocations(String adaptorName, String location) throws InvalidLocationException {

        if (location == null) {
            throw new IllegalArgumentException("Location may nor be null");
        }

        ArrayList<SshdSocketAddress> result = new ArrayList<>();

        String tmp = location;

        int index = tmp.lastIndexOf(VIA_TAG);

        while (index != -1) {

            if (index == 0) {
                throw new InvalidLocationException(adaptorName, "Could not parse location: " + location);
            }

            result.add(extractSocketAddress(adaptorName, tmp.substring(index + VIA_TAG.length()).trim()));
            tmp = tmp.substring(0, index);

            index = tmp.lastIndexOf(VIA_TAG);
        }

        if (tmp.isEmpty()) {
            throw new InvalidLocationException(adaptorName, "Could not parse location: " + location);
        }

        result.add(extractSocketAddress(adaptorName, tmp.trim()));

        return result.toArray(new SshdSocketAddress[result.size()]);
    }

    private static SshdSocketAddress extractSocketAddress(String adaptorName, String location) throws InvalidLocationException {

        URI uri;

        try {
            uri = new URI("sftp://" + location);
        } catch (Exception e) {
            throw new InvalidLocationException(adaptorName, "Failed to parse location: " + location, e);
        }

        String host = uri.getHost();
        int port = uri.getPort();

        if (port == -1) {
            port = DEFAULT_SSH_PORT;
        }

        return new SshdSocketAddress(host, port);
    }

    private static ClientSession connectAndAuthenticate(String adaptorName, SshClient client, String host, int port, UserCredential credential, long timeout)
            throws XenonException {

        if (host == null) {
            throw new IllegalArgumentException("Target host may not be null");
        }

        String username = credential.getUsername();

        if (username == null) {
            throw new InvalidCredentialException(adaptorName, "Failed to retrieve username from credential");
        }

        ClientSession session = null;

        try { // Connect to remote machine and retrieve a session. Will throw exception on timeout
            session = client.connect(username, host, port).verify(timeout).getSession();
        } catch (IOException e) {
            throw new XenonException(adaptorName, "Connection setup to " + host + ":" + port + " failed!", e);
        }

        // Figure out which type of credential we are using
        if (credential instanceof DefaultCredential) {
            // do nothing

        } else if (credential instanceof CertificateCredential) {

            CertificateCredential c = (CertificateCredential) credential;

            String certfile = c.getCertificateFile();

            Path path = Paths.get(certfile).toAbsolutePath().normalize();

            if (!path.toFile().exists()) {
                throw new CertificateNotFoundException(adaptorName, "Certificate not found: " + path);
            }

            KeyPair pair = null;

            try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {

                char[] password = c.getPassword();

                if (password.length == 0) {
                    pair = SecurityUtils.loadKeyPairIdentity(path.toString(), inputStream, null);
                } else {
                    pair = SecurityUtils.loadKeyPairIdentity(path.toString(), inputStream, new PasswordProvider(password));
                }

            } catch (Exception e) {
                throw new XenonException(adaptorName, "Failed to load certificate: " + path, e);
            }

            session.addPublicKeyIdentity(pair);

        } else if (credential instanceof PasswordCredential)

        {

            PasswordCredential c = (PasswordCredential) credential;
            session.addPasswordIdentity(new String(c.getPassword()));

        } else {
            throw new InvalidCredentialException(adaptorName, "Unsupported credential type: " + credential.getClass().getName());
        }

        // Will throw exception on timeout
        try {
            session.auth().verify(timeout);
        } catch (IOException e) {
            throw new XenonException(adaptorName, "Connection authentication failed", e);
        }

        return session;
    }

    /**
     * Connect an existing {@link SshClient} to the server at <code>location</code> and authenticate using the given <code>credential</code>.
     *
     * @param adaptorName
     *            the adaptor where this method was called from.
     * @param client
     *            the client to connect.
     * @param location
     *            the server to connect to
     * @param credential
     *            the credential to authenticate with.
     * @param bufferSize
     *            the buffer size used for the (optional) SSH tunnels.
     * @param timeout
     *            the timeout to use in connection setup (in milliseconds).
     * @return the connected {@link ClientSession}
     * @throws XenonException
     *             if the connection setup or authentication failed.
     */
    public static SSHConnection connect(String adaptorName, SshClient client, String location, Credential credential, int bufferSize, long timeout)
            throws XenonException {

        if (credential == null) {
            throw new IllegalArgumentException("Credential may not be null");
        }

        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid timeout: " + timeout);
        }

        if (location == null) {
            throw new IllegalArgumentException("Location may not be null");
        }

        SshdSocketAddress[] locations = extractLocations(adaptorName, location);
        UserCredential[] creds = extractCredentials(adaptorName, locations, credential);

        SSHConnection connection = new SSHConnection(locations.length - 1);

        // Connect to the last location. This is either the destination (without tunneling) or the first hop.
        ClientSession session = connectAndAuthenticate(adaptorName, client, locations[0].getHostName(), locations[0].getPort(), creds[0], timeout);

        try {
            // If we have more that one location we need to tunnel via another location.
            for (int i = 1; i < locations.length; i++) {
                ChannelDirectTcpip channel = session.createDirectTcpipChannel(null, locations[i]);
                channel.open().await(timeout);

                ServerSocket server = new ServerSocket(0);
                int port = server.getLocalPort();

                Tunnel tunnel = new Tunnel(server, channel, bufferSize);
                tunnel.start();

                connection.addHop(i - 1, session, tunnel);

                session = connectAndAuthenticate(adaptorName, client, "localhost", port, creds[i], timeout);
            }

        } catch (IOException e) {
            // Attempt to cleanup the mess
            connection.close();

            try {
                session.close();
            } catch (IOException e1) {
                // ignored
            }

            throw new XenonException(adaptorName, "Failed to set up SSH forwarding", e);

        } catch (XenonException xe) {
            // Attempt to cleanup the mess
            connection.close();
            throw xe;
        }

        connection.setSession(session);
        return connection;
    }

    public static Map<String, String> translateProperties(Map<String, String> providedProperties, String orginalPrefix,
            XenonPropertyDescription[] supportedProperties, String newPrefix) {

        Set<String> valid = validProperties(supportedProperties);

        HashMap<String, String> result = new HashMap<>();

        int start = orginalPrefix.length();

        for (Map.Entry<String, String> e : providedProperties.entrySet()) {

            String key = e.getKey();

            if (key.startsWith(orginalPrefix)) {

                String newKey = newPrefix + key.substring(start, key.length());

                if (valid.contains(newKey)) {
                    result.put(newKey, e.getValue());
                }
            }
        }

        return result;
    }

    public static Set<String> validProperties(XenonPropertyDescription[] properties) {

        HashSet<String> result = new HashSet<>();

        for (XenonPropertyDescription p : properties) {
            result.add(p.getName());
        }

        return result;
    }
}
