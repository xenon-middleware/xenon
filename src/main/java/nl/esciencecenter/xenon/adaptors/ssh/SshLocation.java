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

    /**
     * Parses a location string resembling an URI to a SSHLocation.
     *
     * A ConfigRepository may provide an SSH config with a default host name,
     * user, and port, for a given host.
     *
     * @param locationString location string resembling an URI
     * @param sshConfig general SSH config for SSH hosts
     * @return a matching SSHLocation
     * @throws InvalidLocationException when the locationString cannot be parsed
     * @throws NullPointerException when sshConfig is null.
     */
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
