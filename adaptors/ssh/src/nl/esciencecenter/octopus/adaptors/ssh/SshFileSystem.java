package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshFileSystem extends FileSystemImplementation {
    private Session session;
    private SshAdaptor adaptor;
    private boolean isOpen;
    
    public SshFileSystem(String adaptorName, String uniqueID, URI location, RelativePath entryPath, Credential credential,
            OctopusProperties properties, SshAdaptor adaptor, Session session) {
        super(adaptorName, uniqueID, location, entryPath, credential, properties);
        this.adaptor = adaptor;
        this.session = session;
        isOpen = true;
    }
    
    protected void close() {
        session.disconnect();
        isOpen = false;
    }
    
    protected boolean isOpen() {
        return isOpen;
    }
    
    protected ChannelSftp getSftpChannel() throws OctopusIOException {
        return getSftpChannel(session);
    }

    protected static ChannelSftp getSftpChannel(Session session) throws OctopusIOException {
        Channel channel;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        } catch (JSchException e) {
            throw new OctopusIOException("ssh", e.getMessage(), e);
        }
    }
    
    protected void putSftpChannel(ChannelSftp channel) {
        channel.disconnect();
    }
}
