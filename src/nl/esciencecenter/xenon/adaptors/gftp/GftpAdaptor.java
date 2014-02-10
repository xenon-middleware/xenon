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
package nl.esciencecenter.xenon.adaptors.gftp;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Grid FTP Adaptor. Uses cog-jglobus-1.7/1.8 API.
 * 
 * @author Piter T. de Boer
 */
public class GftpAdaptor extends Adaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GftpAdaptor.class);

    static {
        GlobusUtil.staticInit();
    }

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "gftp";

    /** The default Grid FTP port */
    protected static final int DEFAULT_PORT = 2811;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Grid FTP adaptor based on Globus Grid FTP";

    /**
     * The schemes supported by this adaptor. Both "gftp" and "gsiftp" are supported. "gsiftp "is the default scheme for Grid FTP.
     */
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>(GftpUtil.GSIFTP_SCHEME,
            GftpUtil.GFTP_SCHEME);

    /** The locations supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("host[:port]");

    /** All Grid FTP properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS + ADAPTOR_NAME + ".";

    /**
     * Whether Active mode Grid FTP must be used by the remote server. <br>
     * This means the remote Grid FTP Server will the active party when connecting back to the (local) client. This mode has a
     * better performance, but is only possible when the local client is not behind a firewall.
     */
    public static final String USE_ACTIVE_MODE = PREFIX + "useActiveMode";

    /**
     * SRM compatible GridFTP mode. Grid FTP URLs are only used as transport URLs to copy files. This means all URLs are assumed
     * to be files and assumed to exist on the remote server. File won't be 'stat'-ed (mlst will return default values). <br>
     * Also listing of remote directories is not possible. The list()/mlsd() methods will return no entries.
     */
    public static final String USE_BLIND_GFTP = PREFIX + "useBlindMode";

    /**
     * Explicitly set to Grid FTP version 1.x. Needed for old Grid FTP servers and some SRM back-end Grid FTP servers.
     */
    public static final String USE_GFTP_V1 = PREFIX + "useGftpV1";

    /**
     * Enforce the use of Data Channel Authentication. Some Grid FTP servers do not support this.<br>
     * If this is not supported but enforceDCAUE==true, an exception will be thrown. If set to false DCAU will be disabled if not
     * supported by the GFTP Server.
     */
    public static final String ENFORCE_DATA_CHANNEL_AUTHENTICATION = PREFIX + "enforceDCAU";

    /**
     * List of properties supported by this GfridFTP adaptor
     */
    protected static final ImmutableArray<XenonPropertyDescription> VALID_GFTP_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(

    new XenonPropertyDescriptionImplementation(USE_ACTIVE_MODE, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "false",
            "Whether the remote server should use active mode. This means the remote server connects back to the local client"
                    + "Default is false (= passive mode). "),

    new XenonPropertyDescriptionImplementation(USE_BLIND_GFTP, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "false",
            "Whether to use Blind mode GFTP: stat and list methods are not supported, only get and put."),

    new XenonPropertyDescriptionImplementation(USE_GFTP_V1, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "false",
            "Enforce the use of old Grid FTP V1.0 methods: Mlst and Mlsd methods are not supported in V1."),

    new XenonPropertyDescriptionImplementation(ENFORCE_DATA_CHANNEL_AUTHENTICATION, Type.BOOLEAN,
            EnumSet.of(Component.FILESYSTEM), "false",
            "Enforce the use of Data Channel Authentication (DCAU) and throw exceptions if not supported by the Grid FTP Server."
                    + "Default behaviour is to switch off DCAU if not support by remote server."));

    public static Map<String, String> filterProps(ImmutableArray<XenonPropertyDescription> validProperties,
            Map<String, String> properties) {

        Map<String, String> props = new Hashtable<String, String>();

        for (XenonPropertyDescription entry : validProperties) {
            String name = entry.getName();
            entry.getType();
            String val = properties.get(name);

            if (val != null) {
                props.put(name, val);
            }
        }

        return props;
    }

    // ========
    // Instance 
    // ========

    private final GftpFiles filesAdaptor;

    private final GlobusProxyCredentials credentialsAdaptor;

    public GftpAdaptor(XenonEngine xenonEngine, Map<String, String> properties) throws XenonException {
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_GFTP_PROPERTIES,
                new XenonProperties(VALID_GFTP_PROPERTIES, Component.FILESYSTEM, filterProps(VALID_GFTP_PROPERTIES, properties)));

        this.filesAdaptor = new GftpFiles(this, xenonEngine);

        // filter out credentials properties: 
        Map<String, String> credProps = filterProps(GlobusProxyCredentials.GLOBUS_CREDENTIAL_PROPERTIES, properties);

        XenonProperties xenonCredProps = new XenonProperties(GlobusProxyCredentials.GLOBUS_CREDENTIAL_PROPERTIES,
                Component.CREDENTIALS, filterProps(GlobusProxyCredentials.GLOBUS_CREDENTIAL_PROPERTIES, credProps));

        // Custom Globus Properties factory for this FileSystem, which should be linked to one user credential configuration. 
        this.credentialsAdaptor = new GlobusProxyCredentials(xenonCredProps, this);
    }

    @Override
    public XenonPropertyDescription[] getSupportedProperties() {
        return VALID_GFTP_PROPERTIES.asArray();
    }
    

    public ImmutableArray<XenonPropertyDescription> getSupportedFileSystemProperties() {
        return VALID_GFTP_PROPERTIES;
    }

    public ImmutableArray<XenonPropertyDescription> getSupportedCredentialProperties() {
        return GlobusProxyCredentials.GLOBUS_CREDENTIAL_PROPERTIES;
    }

    @Override
    public GftpFiles filesAdaptor() {
        return filesAdaptor;
    }

    @Override
    public GlobusProxyCredentials credentialsAdaptor() {
        return credentialsAdaptor;
    }

    @Override
    public void end() {
        filesAdaptor.end();
    }

    protected GftpSession createNewSession(GftpLocation location, GlobusProxyCredential credential, XenonProperties properties)
            throws XenonException {
        return new GftpSession(this, location, credential, properties);
    }

    /**
     * Helper method to create new Session.
     * 
     * @param hostname
     *            - hostname of Grid FTP Server.
     * @param port
     *            - port of Grid FTP server.
     * @param proxyFilepath
     *            - location of (globus) proxy file
     * @param props
     *            - map of properties
     * @return New connected GftpSession
     * @throws XenonException
     */
    public GftpSession createNewSession(String host, int port, String proxyFilepath, Map<String, String> props)
            throws XenonException {

        GftpLocation gftpLocation = new GftpLocation(host, port);
        GlobusProxyCredential cred = credentialsAdaptor.loadProxy(proxyFilepath);
        XenonProperties xenonProperties = new XenonProperties(this.getSupportedProperties(Component.FILESYSTEM), props);
        return createNewSession(gftpLocation, cred, xenonProperties);
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        Map<String, String> result = new HashMap<String, String>();
        return result;
    }

    @Override
    public Jobs jobsAdaptor() throws XenonException {
        throw new XenonException(this.getName(), "jobsAdaptor(): Not implemented");
    }

    public GlobusProxyCredentials getGlobusProxyCredentials() {
        return this.credentialsAdaptor;
    }

}
