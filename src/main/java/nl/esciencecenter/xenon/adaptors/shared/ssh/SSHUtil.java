package nl.esciencecenter.xenon.adaptors.shared.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;

import org.apache.sshd.agent.local.ProxyAgentFactory;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.InvalidCredentialException;
import nl.esciencecenter.xenon.schedulers.InvalidLocationException;

public class SSHUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SSHUtil.class);

	private static final int DEFAULT_SSH_PORT = 22;
	
	private static class PasswordProvider implements FilePasswordProvider {

		private final char [] password;
		
		public PasswordProvider(char [] password) {
			this.password = password;
		}
		
		@Override
		public String getPassword(String resourceKey) throws IOException {
			return new String(password);
		} 
	}
	
	public static SshClient createSSHClient(boolean loadKnownHosts, boolean loadSSHConfig, boolean useSSHAgent, boolean useAgentForwarding) { 

		SshClient client = SshClient.setUpDefaultClient();
		
		if (loadKnownHosts) {
			client.setServerKeyVerifier(new DefaultKnownHostsServerKeyVerifier(RejectAllServerKeyVerifier.INSTANCE));
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
	
	private static String getHost(String adaptorName, String location) throws InvalidLocationException {
		
		// Parse locations of the format: hostname[:port]
		
		if (location == null || location.trim().length() == 0) { 
			throw new InvalidLocationException(adaptorName, "Failed to parse location: " + location);
		}
		
		String hostname = null;
		
		if (location.contains(":")) {
			hostname = location.substring(0, location.indexOf(":"));
		} else { 
			hostname = location;
		}
		
		// TODO: verify hostname here? -- could be name in ssh_config instead ?
		return hostname;
	}
	
	private static int getPort(String adaptorName, String location) throws InvalidLocationException { 
	
		// Parse locations of the format: hostname[:port]
		if (location == null || location.trim().length() == 0) { 
			throw new InvalidLocationException(adaptorName, "Failed to parse location: " + location);
		}
		
		int port = DEFAULT_SSH_PORT;
		
		if (location.contains(":")) {
			port = Integer.parseInt(location.substring(location.indexOf(":")+1, location.length()));
		} 
		
		if (port <= 0 || port > 65535) { 
			throw new InvalidLocationException(adaptorName, "Invalid port number: " + port);
		}
		
		return port;
	}
		
	public static ClientSession connect(String adaptorName, SshClient client, String location, Credential credential, long timeout) throws XenonException {
		
		// location should be hostname or hostname:port. If port unset it defaults to port 22
		
		// Credential may be DEFAULT with username, username/password or username / certificate / passphrase.   
		
		// TODO: Add option DEFAULT with password ? 
		
		if (credential == null) { 
			throw new IllegalArgumentException("Credential may not be null");
		}
		
		if (timeout <= 0) { 
			throw new IllegalArgumentException("Invalid timeout: " + timeout);
		}
		
		String username = credential.getUsername();
		
		// TODO: Are there cases where we do not want a user name ?
		if (username == null) { 
			throw new XenonException(adaptorName, "Failed to retrieve username from credential");
		}
		
		String host = getHost(adaptorName, location);
		int port = getPort(adaptorName, location);
		
		ClientSession session = null;
		
		try {
			// Connect to remote machine and retrieve a session. Will throw exception on timeout

			System.out.println("CONNECT TO " + host + ":" + port + " as " + username + " within " + timeout + " ms.");
			
			
			session = client.connect(username, host, port).verify(timeout).getSession();
		} catch (IOException e) {
			throw new XenonException(adaptorName, "Connection setup timeout: " + host + ":" + port, e);
		}
		
		// Figure out which type of credential we are using
		if (credential instanceof DefaultCredential) { 
			// do nothing
		} else if (credential instanceof PasswordCredential) { 
			PasswordCredential c = (PasswordCredential) credential;
			session.addPasswordIdentity(new String(c.getPassword()));

		} else if (credential instanceof CertificateCredential) { 
			CertificateCredential c = (CertificateCredential) credential;
			
			String certfile = c.getCertificateFile();
			
			Path path = Paths.get(certfile).toAbsolutePath().normalize();
			
			if (!Files.exists(path, IoUtils.EMPTY_LINK_OPTIONS)) {
				throw new CertificateNotFoundException(adaptorName, "Certificate not found: " + path);
			}
		
			KeyPair pair = null;
			
			try { 
				InputStream inputStream = Files.newInputStream(path, IoUtils.EMPTY_OPEN_OPTIONS);
				pair = SecurityUtils.loadKeyPairIdentity(path.toString(), inputStream, new PasswordProvider(c.getPassword()));
			} catch (Exception e) {
				throw new XenonException(adaptorName, "Failed to load certificate: " + path, e);
			}

			session.addPublicKeyIdentity(pair);
		} else { 
			throw new InvalidCredentialException(adaptorName, "Unsupported credential type: " + credential.getClass().getName());
		}
		
		// Will throw exception on timeout
		try { 
			session.auth().verify(timeout);
		} catch (IOException e) {
			throw new XenonException(adaptorName, "Connection authentication timeout", e);
		}
		
		// session.createSftpFileSystem(); 
		
		
		return session;
	}
	
	
}