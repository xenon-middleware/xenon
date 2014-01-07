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

import org.globus.gsi.GlobusCredential;
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

    /** The schemes supported by this adaptor. both "gftp" and "gsiftp" are supported. */
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>(GftpUtil.GSIFTP_SCHEME,
            GftpUtil.GFTP_SCHEME);

    /** The locations supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("host[:port]");

    /** All Grid FTP properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS + ADAPTOR_NAME + ".";

    /** Some data channels must use channel authentication when setting up a Grid FTP connection. */
    public static final String USE_PASSIVE_MODE = PREFIX + "usePassiveMode";

    /**
     * Do not 'stat' the remote file and assume remote files exists when statted. <br>
     * This is needed to access SRM Grid FTP Servers. Also listing of remote directories is not possible. The msld() method(s)
     * will return no entries.
     */
    public static final String USE_BLIND_GFTP = PREFIX + "useBlindMode";

    /**
     * Explicitly set to gftp version 1.x. Needed for old Grid FTP servers and some SRM back-end gftp servers.
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
    private static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(

            new XenonPropertyDescriptionImplementation(USE_PASSIVE_MODE, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "true",
                    "Whether to use Active Mode or Passive Mode Grid FTP. Default is Passive mode. "),

            new XenonPropertyDescriptionImplementation(USE_BLIND_GFTP, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "false",
                    "Whether to use Blind mode GFTP: stat and list methods are not supported, only get and put."),

            new XenonPropertyDescriptionImplementation(USE_GFTP_V1, Type.BOOLEAN, EnumSet.of(Component.FILESYSTEM), "false",
                    "Enforce the use of old Grid FTP V1.0 methods: Mlst and Mlsd methods are not supported in V1."),

            new XenonPropertyDescriptionImplementation(ENFORCE_DATA_CHANNEL_AUTHENTICATION, Type.BOOLEAN, EnumSet
                    .of(Component.FILESYSTEM), "false",
                    "Enforce the use of Data Channel Authentication (DCAU) and throw exceptions if not supported by the Grid FTP Server."));

    private final GftpFiles filesAdaptor;

    private final GlobusProxyCredentials credentialsAdaptor;

    public GftpAdaptor(XenonEngine xenonEngine, Map<String, String> properties) throws XenonException {
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties));

        this.filesAdaptor = new GftpFiles(this, xenonEngine);
        // Custom Globus Properties factory for this FileSystem, which should be linked to one user credential configuration. 
        this.credentialsAdaptor = new GlobusProxyCredentials(getProperties(), this);
    }

    @Override
    public XenonPropertyDescription[] getSupportedProperties() {
        return VALID_PROPERTIES.asArray();
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
     * @param hostname - hostname of Grid FTP Server. 
     * @param port - port of Grid FTP server. 
     * @param proxyFilepath - location of (globus) proxy file 
     * @param props - map of properties 
     * @return
     * @throws XenonException
     */
    public GftpSession createNewSession(String host, int port, String proxyFilepath, Map<String,String> props) throws XenonException {

        GftpLocation gftpLocation=new GftpLocation(host,port); 
        GlobusProxyCredential cred = credentialsAdaptor.loadProxy(proxyFilepath);
        XenonProperties xenonProperties = new XenonProperties(this.getSupportedProperties(Component.FILESYSTEM), props);
        return createNewSession(gftpLocation,cred,xenonProperties); 
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
