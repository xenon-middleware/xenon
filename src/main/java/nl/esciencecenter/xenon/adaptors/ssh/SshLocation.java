package nl.esciencecenter.xenon.adaptors.ssh;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

import com.jcraft.jsch.ConfigRepository;

public class SshLocation extends Location {

    public SshLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected SshLocation(String location) throws InvalidLocationException {
        super(location, SshAdaptor.ADAPTOR_SCHEME.get(0));
    }

    @Override
    protected String getAdaptorName() {
        return SshAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return SshAdaptor.DEFAULT_PORT;
    }

    public static SshLocation parse(String locationString, ConfigRepository sshConfig) throws InvalidLocationException {
        SshLocation location = new SshLocation(locationString);
        ConfigRepository.Config sshHostConfig = sshConfig.getConfig(location.getHost());
        if (sshHostConfig != null) {
            // this test must come first, otherwise the location will not use default port anymore.
            if (sshHostConfig.getPort() != -1 && location.usesDefaultPort()) {
                location = new SshLocation(location.getUser(), location.getHost(), sshHostConfig.getPort());
            }
            if (sshHostConfig.getUser() != null && location.getUser() == null) {
                location = new SshLocation(sshHostConfig.getUser(), location.getHost(), location.getPort());
            }
            if (sshHostConfig.getHostname() != null) {
                location = new SshLocation(location.getUser(), sshHostConfig.getHostname(), location.getPort());
            }
            // Unfortunately, JSch does not recognize OpenSSH standard
            // HostName, only Hostname.
            if (sshHostConfig.getValue("HostName") != null) {
                location = new SshLocation(location.getUser(), sshHostConfig.getValue("HostName"), location.getPort());
            }
        }
        return location;
    }
}
