package nl.esciencecenter.xenon.adaptors.job.ssh;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

public class SshProperties {

	/** The name of this adaptor */
    public static final String ADAPTOR_NAME = "ssh";

    /** The default SSH port */
    public static final int DEFAULT_PORT = 22;

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The SSH job adaptor implements all functionality to start jobs on ssh servers.";

    /** The schemes supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("ssh");

    /** The locations supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("host[:port]");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + "ssh.";

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

    /** Add gateway to access machine. */
    public static final String TIMEOUT = PREFIX + "timeout";
    
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
    protected static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(
            new XenonPropertyDescriptionImplementation(AUTOMATICALLY_ADD_HOST_KEY, Type.BOOLEAN,
            		"true", "Automatically add unknown host keys to known_hosts."),
            new XenonPropertyDescriptionImplementation(STRICT_HOST_KEY_CHECKING, Type.BOOLEAN, 
            		"true", "Enable strict host key checking."),
            new XenonPropertyDescriptionImplementation(LOAD_STANDARD_KNOWN_HOSTS, Type.BOOLEAN, 
                    "true", "Load the standard known_hosts file."),
            new XenonPropertyDescriptionImplementation(LOAD_SSH_CONFIG, Type.BOOLEAN, 
                    "true", "Load the OpenSSH config file."),
            new XenonPropertyDescriptionImplementation(SSH_CONFIG_FILE, Type.STRING, 
                    null, "OpenSSH config filename."),
            new XenonPropertyDescriptionImplementation(AGENT, Type.BOOLEAN,
                    "false", "Use a (local) ssh-agent."),
            new XenonPropertyDescriptionImplementation(AGENT_FORWARDING, Type.BOOLEAN,  
            		"false", "Use ssh-agent forwarding"),
            new XenonPropertyDescriptionImplementation(TIMEOUT, Type.LONG,  
            		"10000", "The timeout for the connection setup and authetication (in milliseconds)."),
            new XenonPropertyDescriptionImplementation(POLLING_DELAY, Type.LONG,  
            		"1000", "The polling delay for monitoring running jobs (in milliseconds)."),
            new XenonPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER,  
            		"4", "The maximum number of concurrent jobs in the multiq.."),
            new XenonPropertyDescriptionImplementation(GATEWAY, Type.STRING, 
            		null, "The gateway machine used to create an SSH tunnel to the target."));

}
