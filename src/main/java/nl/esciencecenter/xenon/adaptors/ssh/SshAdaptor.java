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

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;

public class SshAdaptor extends Adaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "ssh";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 22;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The SSH adaptor implements all functionality with remote ssh servers.";

    /** The schemes supported by this adaptor */
    protected static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("ssh", "sftp");

    /** The locations supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("[user@]host[:port]");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS + "ssh.";

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

    /** All our own queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** Property for maximum history length for finished jobs */
    public static final String POLLING_DELAY = QUEUE + "pollingDelay";

    /** Local multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Property for the maximum number of concurrent jobs in the multi queue. */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** Ssh adaptor information start with this prefix. */
    public static final String INFO = PREFIX + "info.";

    /** Ssh job information start with this prefix. */
    public static final String JOBS = INFO + "jobs.";

    /** How many jobs have been submitted using this adaptor. */
    public static final String SUBMITTED = JOBS + "submitted";

    /** List of properties supported by this SSH adaptor */
    private static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(
            new XenonPropertyDescriptionImplementation(AUTOMATICALLY_ADD_HOST_KEY, Type.BOOLEAN, EnumSet.of(Component.SCHEDULER,
                    Component.FILESYSTEM), "true", "Automatically add unknown host keys to known_hosts."),
            new XenonPropertyDescriptionImplementation(STRICT_HOST_KEY_CHECKING, Type.BOOLEAN, EnumSet.of(Component.SCHEDULER,
                    Component.FILESYSTEM), "true", "Enable strict host key checking."),
            new XenonPropertyDescriptionImplementation(LOAD_STANDARD_KNOWN_HOSTS, Type.BOOLEAN, EnumSet.of(Component.XENON),
                    "true", "Load the standard known_hosts file."),
            new XenonPropertyDescriptionImplementation(LOAD_SSH_CONFIG, Type.BOOLEAN, EnumSet.of(Component.XENON),
                    "true", "Load the OpenSSH config file."),
            new XenonPropertyDescriptionImplementation(SSH_CONFIG_FILE, Type.STRING, EnumSet.of(Component.XENON),
                    null, "OpenSSH config filename."),
            new XenonPropertyDescriptionImplementation(AGENT, Type.BOOLEAN, EnumSet.of(Component.XENON),
                    "false", "Use a (local) ssh-agent."),
            new XenonPropertyDescriptionImplementation(AGENT_FORWARDING, Type.BOOLEAN, EnumSet.of(Component.XENON),
                    "false", "Use ssh-agent forwarding"),
            new XenonPropertyDescriptionImplementation(POLLING_DELAY, Type.LONG, EnumSet.of(Component.SCHEDULER), "1000",
                    "The polling delay for monitoring running jobs (in milliseconds)."),
            new XenonPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER, EnumSet.of(Component.SCHEDULER), "4",
                    "The maximum number of concurrent jobs in the multiq.."),
            new XenonPropertyDescriptionImplementation(GATEWAY, Type.STRING, EnumSet.of(Component.SCHEDULER, Component.FILESYSTEM),
                    null, "The gateway machine used to create an SSH tunnel to the target."));

    private final SshFiles filesAdaptor;

    private final SshJobs jobsAdaptor;

    private final SshCredentials credentialsAdaptor;

    private final boolean useAgent;

    private JSch jsch;

    public SshAdaptor(XenonEngine xenonEngine, Map<String, String> properties) throws XenonException {
        this(xenonEngine, new JSch(), properties);
    }

    public SshAdaptor(XenonEngine xenonEngine, JSch jsch, Map<String, String> properties) throws XenonException {
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties));

        this.filesAdaptor = new SshFiles(this, xenonEngine);
        this.jobsAdaptor = new SshJobs(getProperties(), this, xenonEngine);
        this.credentialsAdaptor = new SshCredentials(this);
        this.jsch = jsch;

        if (getProperties().getBooleanProperty(SshAdaptor.LOAD_STANDARD_KNOWN_HOSTS)) {
            String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
            LOGGER.debug("Setting ssh known hosts file to: " + knownHosts);
            if (new File(knownHosts).exists()) {
                setKnownHostsFile(knownHosts);
            } else if (getProperties().propertySet(SshAdaptor.LOAD_STANDARD_KNOWN_HOSTS)) {
                // property was explicitly set, throw an error because it does not exist
                throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Cannot load known_hosts file: " + knownHosts + " does not exist");
            } else {
                // implictly ignore
                LOGGER.debug("known_hosts file " + knownHosts + " does not exist, not checking hosts with signatures of known hosts.");
            }
        }

        if (getProperties().getBooleanProperty(SshAdaptor.LOAD_SSH_CONFIG)) {
            String sshConfig = getProperties().getProperty(SshAdaptor.SSH_CONFIG_FILE);
            if (sshConfig == null) {
                sshConfig = System.getProperty("user.home") + "/.ssh/config";
            }
            LOGGER.debug("Setting ssh known hosts file to: " + sshConfig);
            setConfigFile(sshConfig, !getProperties().propertySet(SshAdaptor.LOAD_SSH_CONFIG));
        }

        if (getProperties().getBooleanProperty(SshAdaptor.AGENT)) {
            // Connect to the local ssh-agent
            LOGGER.debug("Connecting to ssh-agent");

            Connector connector;

            try {
                ConnectorFactory cf = ConnectorFactory.getDefault();
                connector = cf.createConnector();
            } catch(AgentProxyException e){
                throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Failed to connect to ssh-agent", e);
            }

            IdentityRepository ident = new RemoteIdentityRepository(connector);
            jsch.setIdentityRepository(ident);

            useAgent = true;
        } else {
            useAgent = false;
        }

        if (getProperties().getBooleanProperty(SshAdaptor.AGENT_FORWARDING)) {
            // Enable ssh-agent-forwarding
            LOGGER.warn("TODO: Enabling ssh-agent-forwarding");
        }

        if (jsch.getConfigRepository() == null) {
            jsch.setConfigRepository(ConfigRepository.nullConfig);
        }
    }

    private void setKnownHostsFile(String knownHostsFile) throws XenonException {
        try {
            jsch.setKnownHosts(knownHostsFile);
        } catch (JSchException e) {
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Could not set known_hosts file", e);
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

    private void setConfigFile(String sshConfigFile, boolean ignoreFail) throws XenonException {
        try {
            ConfigRepository configRepository = OpenSSHConfig.parse(new File(sshConfigFile));
            jsch.setConfigRepository(configRepository);
        } catch (IOException|XenonException ex) {
            if (ignoreFail) {
                LOGGER.warn("OpenSSH config file cannot be read.");
            } else {
                throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Cannot read OpenSSH config file", ex);
            }
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
}
