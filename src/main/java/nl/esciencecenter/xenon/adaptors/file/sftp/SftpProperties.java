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
package nl.esciencecenter.xenon.adaptors.file.sftp;

import java.util.EnumSet;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

public class SftpProperties {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "sftp";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 22;

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The SFTP adaptor implements all file access functionality to remote SFTP servers";

    /** The schemes supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("sftp");

    /** The locations supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("sftp://[user@]host[:port]");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** Enable strict host key checking. */
    public static final String STRICT_HOST_KEY_CHECKING = PREFIX + "strictHostKeyChecking";

    /** Enable the use of an ssh-agent */
    public static final String AGENT = PREFIX + "agent";

    /** Enable the use of ssh-agent-forwarding */
    public static final String AGENT_FORWARDING = PREFIX + "agentForwarding";

    /** Load the known_hosts file by default. */
    public static final String LOAD_STANDARD_KNOWN_HOSTS = PREFIX + "loadKnownHosts";

    /** Load the OpenSSH config file by default. */
    public static final String LOAD_SSH_CONFIG = PREFIX + "loadSshConfig";

    /** OpenSSH config filename. */
    public static final String SSH_CONFIG_FILE = PREFIX + "sshConfigFile";

    /** Enable strict host key checking. */
    public static final String AUTOMATICALLY_ADD_HOST_KEY = PREFIX + "autoAddHostKey";

    /** Add gateway to access machine. */
    public static final String GATEWAY = PREFIX + "gateway";

    /** Property for maximum history length for finished jobs */
    public static final String CONNECTION_TIMEOUT = PREFIX + "connection.timeout";

    /** List of properties supported by this SSH adaptor */
    protected static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(
            new XenonPropertyDescriptionImplementation(AUTOMATICALLY_ADD_HOST_KEY, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), 
            		"true", "Automatically add unknown host keys to known_hosts."),
            new XenonPropertyDescriptionImplementation(STRICT_HOST_KEY_CHECKING, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), 
            		"true", "Enable strict host key checking."),
            new XenonPropertyDescriptionImplementation(LOAD_STANDARD_KNOWN_HOSTS, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM),
                    "true", "Load the standard known_hosts file."),
            new XenonPropertyDescriptionImplementation(LOAD_SSH_CONFIG, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM),
                    "true", "Load the OpenSSH config file."),
            new XenonPropertyDescriptionImplementation(SSH_CONFIG_FILE, Type.STRING, EnumSet.of(Component.FILESYSTEM),
                    null, "OpenSSH config filename."),
            new XenonPropertyDescriptionImplementation(AGENT, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM),
                    "false", "Use a (local) ssh-agent."),
            new XenonPropertyDescriptionImplementation(AGENT_FORWARDING, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "false", 
                    "Use ssh-agent forwarding"),
            new XenonPropertyDescriptionImplementation(CONNECTION_TIMEOUT, Type.LONG, EnumSet.of(Component.FILESYSTEM), "10000",
                    "The timeout for creating and authenticating connections (in milliseconds)."));

/*    
    
    private final SshFiles filesAdaptor;

    private final SshJobs jobsAdaptor;

    private final SshCredentials credentialsAdaptor;

    private final boolean useAgent;

    private final boolean useAgentForwarding;
   
    private JSch jsch;

    public SftpProperties(XenonEngine xenonEngine, Map<String, String> properties) throws XenonException {
        this(xenonEngine, new JSch(), properties);
    }

    public SftpProperties(XenonEngine xenonEngine, JSch jsch, Map<String, String> properties) throws XenonException {
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties));

        this.filesAdaptor = new SshFiles(this, xenonEngine);
        this.jobsAdaptor = new SshJobs(getProperties(), this, xenonEngine);
        this.credentialsAdaptor = new SshCredentials(this);
        this.jsch = jsch;

        String openSshDir = System.getProperty("user.home") + File.separator + ".ssh";

        if (getProperties().getBooleanProperty(SftpProperties.LOAD_STANDARD_KNOWN_HOSTS)) {
            String knownHosts = openSshDir + File.separator + "known_hosts";
            LOGGER.debug("Setting ssh known hosts file to: " + knownHosts);
            if (new File(knownHosts).canRead()) {
                setKnownHostsFile(knownHosts);
            } else if (getProperties().propertySet(SftpProperties.LOAD_STANDARD_KNOWN_HOSTS)) {
                // property was explicitly set, throw an error because it does not exist
                throw new XenonException(SftpProperties.ADAPTOR_NAME, "Cannot load known_hosts file: " + knownHosts + " does not exist");
            } else {
                // implictly ignore
                LOGGER.debug("known_hosts file {} is not readable, not checking hosts with signatures of known hosts.", knownHosts);
            }
        }

        if (getProperties().getBooleanProperty(SftpProperties.LOAD_SSH_CONFIG)) {
            String sshConfig = getProperties().getProperty(SftpProperties.SSH_CONFIG_FILE);
            if (sshConfig == null) {
                sshConfig = openSshDir + File.separator + "config";
            }
            File sshConfigFile = new File(sshConfig);
            if (sshConfigFile.canRead()) {
                LOGGER.debug("Setting ssh config file to: " + sshConfigFile.getAbsolutePath());
                setConfigFile(sshConfigFile.getAbsolutePath());
            } else if (getProperties().propertySet(SftpProperties.LOAD_SSH_CONFIG) || getProperties().propertySet(SftpProperties.SSH_CONFIG_FILE)) {
                // property was explicitly set, throw an error because it does not exist
                throw new XenonException(SftpProperties.ADAPTOR_NAME, "Cannot read OpenSSH config file at " + sshConfigFile.getAbsolutePath());
            } else {
                // implictly ignore
                LOGGER.debug("OpenSSH config file " + sshConfigFile.getAbsolutePath() + " is not readable, not setting any default SSH values.");
            }
        }

        if (getProperties().getBooleanProperty(SftpProperties.AGENT)) {
            // Connect to the local ssh-agent
            LOGGER.debug("Connecting to ssh-agent");

            Connector connector;

            try {
                ConnectorFactory cf = ConnectorFactory.getDefault();
                connector = cf.createConnector();
            } catch(AgentProxyException e){
                throw new XenonException(SftpProperties.ADAPTOR_NAME, "Failed to connect to ssh-agent", e);
            }

            IdentityRepository ident = new RemoteIdentityRepository(connector);
            jsch.setIdentityRepository(ident);

            useAgent = true;
        } else {
            useAgent = false;
        }

        if (getProperties().getBooleanProperty(SftpProperties.AGENT_FORWARDING)) {
            // Enable ssh-agent-forwarding
            LOGGER.debug("Enabling ssh-agent-forwarding");

            useAgentForwarding = true;            
        } else { 
            useAgentForwarding = false;
        }

        if (jsch.getConfigRepository() == null) {
            jsch.setConfigRepository(ConfigRepository.nullConfig);
        }
    }

    /**
     * Returns if agent forwarding should be used according to the adaptor properties.  
     * 
     * @return if agent forwarding should be used.
     */
    
/*    
    protected boolean useAgentForwarding() {
        return useAgentForwarding;
    }
    
    private void setKnownHostsFile(String knownHostsFile) throws XenonException {
        try {
            jsch.setKnownHosts(knownHostsFile);
        } catch (JSchException e) {
            throw new XenonException(SftpProperties.ADAPTOR_NAME, "Could not set known_hosts file", e);
        }

        if (LOGGER.isDebugEnabled()) {
            HostKeyRepository hkr = jsch.getHostKeyRepository();
            HostKey[] hks = hkr.getHostKey();
            if (hks != null) {
                LOGGER.debug("Host keys in " + hkr.getKnownHostsRepositoryID());
                for (HostKey hk : hks) {
                    LOGGER.debug(hk.getHost() + " " + hk.getType() + " " + hk.getFingerPrint(jsch));
                }
                LOGGER.debug("");
            } else {
                LOGGER.debug("No keys in " + knownHostsFile);
            }
        }
    }

    private void setConfigFile(String sshConfigFile) throws XenonException {
        try {
            ConfigRepository configRepository = OpenSSHConfig.parse(new File(sshConfigFile));
            jsch.setConfigRepository(configRepository);
        } catch (IOException|XenonException ex) {
            throw new XenonException(SftpProperties.ADAPTOR_NAME, "Cannot read OpenSSH config file", ex);
        }
    }

    @Override
    public XenonPropertyDescription[] getSupportedProperties() {
        return VALID_PROPERTIES.asArray();
    }

    @Override
    public Files filesAdaptor() {
        return filesAdaptor;
    }

    @Override
    public Jobs jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public Credentials credentialsAdaptor() {
        return credentialsAdaptor;
    }

    @Override
    public void end() {
        jobsAdaptor.end();
        filesAdaptor.end();
    }

    public ConfigRepository getSshConfig() {
        return jsch.getConfigRepository();
    }

    protected SshMultiplexedSession createNewSession(SshLocation location, Credential credential, XenonProperties properties)
            throws XenonException {
        return new SshMultiplexedSession(this, jsch, location, credential, properties);
    }

    protected boolean usingAgent() {
        return useAgent;
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        Map<String,String> result = new HashMap<>(2);
        jobsAdaptor.getAdaptorSpecificInformation(result);
        return result;
    }

*/
}


