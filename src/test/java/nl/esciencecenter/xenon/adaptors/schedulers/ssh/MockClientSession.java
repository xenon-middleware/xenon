/**
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
package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.FileSystem;
import java.security.KeyPair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.client.auth.AuthenticationIdentitiesProvider;
import org.apache.sshd.client.auth.UserAuth;
import org.apache.sshd.client.auth.keyboard.UserInteraction;
import org.apache.sshd.client.auth.password.PasswordIdentityProvider;
import org.apache.sshd.client.channel.ChannelDirectTcpip;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.session.ClientProxyConnector;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.client.subsystem.sftp.SftpVersionSelector;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.PropertyResolver;
import org.apache.sshd.common.Service;
import org.apache.sshd.common.channel.ChannelListener;
import org.apache.sshd.common.cipher.Cipher;
import org.apache.sshd.common.cipher.CipherInformation;
import org.apache.sshd.common.compression.Compression;
import org.apache.sshd.common.compression.CompressionInformation;
import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.KeyExchangeFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.kex.KeyExchange;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.mac.Mac;
import org.apache.sshd.common.mac.MacInformation;
import org.apache.sshd.common.scp.ScpFileOpener;
import org.apache.sshd.common.scp.ScpTransferEventListener;
import org.apache.sshd.common.session.ReservedSessionMessagesHandler;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.net.SshdSocketAddress;

public class MockClientSession implements ClientSession {

	boolean closed = false;
	boolean closeFails = false;

	ChannelExec exec = null;
	
	boolean createChannelThrows;
	
	MockClientSession(boolean closeFails) { 
		this(closeFails, false);
	}
	
	MockClientSession(boolean closeFails, boolean createChannelThrows) { 
		this.closeFails = closeFails;
		this.createChannelThrows = createChannelThrows;
	}

    @Override
    public void close() throws IOException {
    	if (closeFails) { 
    		throw new IOException("Close failed");
    	}
    	closed = true;
    }
    
    @Override
    public boolean isOpen() {
        return !closed;
    }
    
	@Override
	public String getClientVersion() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getServerVersion() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getNegotiatedKexParameter(KexProposalOption paramType) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public CipherInformation getCipherInformation(boolean incoming) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public CompressionInformation getCompressionInformation(boolean incoming) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public MacInformation getMacInformation(boolean incoming) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Buffer createBuffer(byte cmd) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Buffer createBuffer(byte cmd, int estimatedSize) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Buffer prepareBuffer(byte cmd, Buffer buffer) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public IoWriteFuture sendDebugMessage(boolean display, Object msg, String lang) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public IoWriteFuture sendIgnoreMessage(byte... data) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public IoWriteFuture writePacket(Buffer buffer) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public IoWriteFuture writePacket(Buffer buffer, long timeout, TimeUnit unit) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Buffer request(String request, Buffer buffer, long timeout, TimeUnit unit) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void exceptionCaught(Throwable t) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public KeyExchangeFuture reExchangeKeys() throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T extends Service> T getService(Class<T> clazz) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public IoSession getIoSession() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void resetIdleTimeout() {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public TimeoutStatus getTimeoutStatus() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public long getAuthTimeout() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public long getIdleTimeout() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean isAuthenticated() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setAuthenticated() throws IOException {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public byte[] getSessionId() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public KeyExchange getKex() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void disconnect(int reason, String msg) throws IOException {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void startService(String name) throws Exception {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public List<NamedFactory<KeyExchange>> getKeyExchangeFactories() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setKeyExchangeFactories(List<NamedFactory<KeyExchange>> keyExchangeFactories) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public List<NamedFactory<Cipher>> getCipherFactories() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setCipherFactories(List<NamedFactory<Cipher>> cipherFactories) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public List<NamedFactory<Compression>> getCompressionFactories() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setCompressionFactories(List<NamedFactory<Compression>> compressionFactories) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public List<NamedFactory<Mac>> getMacFactories() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setMacFactories(List<NamedFactory<Mac>> macFactories) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public KeyPairProvider getKeyPairProvider() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setKeyPairProvider(KeyPairProvider keyPairProvider) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public List<NamedFactory<Signature>> getSignatureFactories() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setSignatureFactories(List<NamedFactory<Signature>> factories) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void addSessionListener(SessionListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void removeSessionListener(SessionListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public SessionListener getSessionListenerProxy() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ReservedSessionMessagesHandler getReservedSessionMessagesHandler() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setReservedSessionMessagesHandler(ReservedSessionMessagesHandler handler) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void addChannelListener(ChannelListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void removeChannelListener(ChannelListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public ChannelListener getChannelListenerProxy() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void addPortForwardingEventListener(PortForwardingEventListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void removePortForwardingEventListener(PortForwardingEventListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public PortForwardingEventListener getPortForwardingEventListenerProxy() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public PropertyResolver getParentPropertyResolver() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Map<String, Object> getProperties() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T> T getAttribute(AttributeKey<T> key) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T> T setAttribute(AttributeKey<T> key, T value) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T> T removeAttribute(AttributeKey<T> key) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T> T resolveAttribute(AttributeKey<T> key) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public CloseFuture close(boolean immediately) {
		closed = true;
	
		return new CloseFuture() {
			
			@Override
			public boolean isDone() {
				return true;
			}
			
			@Override
			public boolean awaitUninterruptibly(long timeoutMillis) {
				return true;
			}
			
			@Override
			public boolean await(long timeoutMillis) throws IOException {
				return true;
			}
			
			@Override
			public CloseFuture removeListener(SshFutureListener<CloseFuture> listener) {
				throw new RuntimeException("Not implemented");
			}
			
			@Override
			public CloseFuture addListener(SshFutureListener<CloseFuture> listener) {
				throw new RuntimeException("Not implemented");
			}
			
			@Override
			public void setClosed() {
			}
			
			@Override
			public boolean isClosed() {
				return true;
			}
		};
	}

	@Override
	public void addCloseFutureListener(SshFutureListener<CloseFuture> listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void removeCloseFutureListener(SshFutureListener<CloseFuture> listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public boolean isClosing() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setUsername(String username) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public String getUsername() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ScpClient createScpClient(ScpFileOpener opener, ScpTransferEventListener listener) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ScpTransferEventListener getScpTransferEventListener() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setScpTransferEventListener(ScpTransferEventListener listener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public ScpFileOpener getScpFileOpener() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setScpFileOpener(ScpFileOpener opener) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public SftpClient createSftpClient(SftpVersionSelector selector) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public FileSystem createSftpFileSystem() throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public FileSystem createSftpFileSystem(int version) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public FileSystem createSftpFileSystem(SftpVersionSelector selector) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public FileSystem createSftpFileSystem(int readBufferSize, int writeBufferSize) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public FileSystem createSftpFileSystem(int version, int readBufferSize, int writeBufferSize) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public FileSystem createSftpFileSystem(SftpVersionSelector selector, int readBufferSize, int writeBufferSize)
			throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ClientProxyConnector getClientProxyConnector() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setClientProxyConnector(ClientProxyConnector proxyConnector) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public AuthenticationIdentitiesProvider getRegisteredIdentities() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public PasswordIdentityProvider getPasswordIdentityProvider() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setPasswordIdentityProvider(PasswordIdentityProvider provider) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void addPasswordIdentity(String password) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public String removePasswordIdentity(String password) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void addPublicKeyIdentity(KeyPair key) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public KeyPair removePublicKeyIdentity(KeyPair kp) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ServerKeyVerifier getServerKeyVerifier() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setServerKeyVerifier(ServerKeyVerifier serverKeyVerifier) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public UserInteraction getUserInteraction() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setUserInteraction(UserInteraction userInteraction) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public List<NamedFactory<UserAuth>> getUserAuthFactories() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setUserAuthFactories(List<NamedFactory<UserAuth>> userAuthFactories) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public SshdSocketAddress startLocalPortForwarding(SshdSocketAddress local, SshdSocketAddress remote)
			throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void stopLocalPortForwarding(SshdSocketAddress local) throws IOException {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public SshdSocketAddress startRemotePortForwarding(SshdSocketAddress remote, SshdSocketAddress local)
			throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void stopRemotePortForwarding(SshdSocketAddress remote) throws IOException {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public SshdSocketAddress startDynamicPortForwarding(SshdSocketAddress local) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void stopDynamicPortForwarding(SshdSocketAddress local) throws IOException {
		throw new RuntimeException("Not implemented");

	}

	@Override
	public SocketAddress getConnectAddress() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public AuthFuture auth() throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ClientChannel createChannel(String type) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ClientChannel createChannel(String type, String subType) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ChannelShell createShellChannel() throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ChannelExec createExecChannel(String command) throws IOException {
		
		if (createChannelThrows) { 
			throw new IOException("Bang!");
		}
		
		exec = new MockChannelExec(command);
		return exec;
	}

	@Override
	public ChannelSubsystem createSubsystemChannel(String subsystem) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ChannelDirectTcpip createDirectTcpipChannel(SshdSocketAddress local, SshdSocketAddress remote)
			throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Set<ClientSessionEvent> waitFor(Collection<ClientSessionEvent> mask, long timeout) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Map<Object, Object> getMetadataMap() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ClientFactoryManager getFactoryManager() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public KeyExchangeFuture switchToNoneCipher() throws IOException {
		throw new RuntimeException("Not implemented");
	}

}
