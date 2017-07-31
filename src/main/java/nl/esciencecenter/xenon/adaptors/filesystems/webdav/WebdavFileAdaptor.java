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
package nl.esciencecenter.xenon.adaptors.filesystems.webdav;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class WebdavFileAdaptor extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFileAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "webdav";

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The webdav file adaptor implements file access to remote webdav servers.";

    /** The locations supported by this adaptor */
    public static final String [] ADAPTOR_LOCATIONS = new String [] { "http://host[:port]", "https://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "webdav.";

    /** List of properties supported by this FTP adaptor */
    public static final XenonPropertyDescription [] VALID_PROPERTIES = new XenonPropertyDescription[0];

    /** The default buffer size for copy. Webdav doesn't use the standard copy engine. */
    protected static final int BUFFER_SIZE = 4 * 1024;

    public static final int OK_CODE = 200;

    private static final int DEFAULT_PORT = 80;

    public WebdavFileAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public boolean canReadSymboliclinks() {
        // Webdav cannot read symbolic links.
        return false;
    }

    protected static boolean isOkish(int response) {
        return response == HttpStatus.SC_OK || response == HttpStatus.SC_CREATED || response == HttpStatus.SC_MULTI_STATUS
                || response == HttpStatus.SC_NO_CONTENT;
    }

//    protected boolean isFilePath(String path) {
//        return !isFolderPath(path);
//    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties)
            throws XenonException {
        LOGGER.debug("newFileSystem location = {} credential = {} properties = {}", location, credential,
                properties);

        if (!(credential instanceof PasswordCredential)) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Only password credentials supported");
        }


        URI uri;

        try {
            uri = new URI(location);
        } catch (Exception e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
        }

        String host = uri.getHost();
        int port = uri.getPort();

        if (port == -1) {
            port = DEFAULT_PORT;
        }

        String hostPort = uri.getScheme() + "://" + host + ":" + port;

        HttpClient client = getClient(host, port, (PasswordCredential) credential);
        HttpMethod method;
        try {
            String loc = uri.toString();

            if (!loc.endsWith("/")) {
                loc = loc + "/";
            }

            method = new OptionsMethod(loc);
            int response = client.executeMethod(method);
            String responseBodyAsString = method.getStatusLine().toString();
            method.releaseConnection();
            if (!isOkish(response)) {
                throw new IOException(responseBodyAsString);
            }
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Could not open connection to " + location, e);
        }

        String cwd = uri.getPath();

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        return new WebdavFileSystem(getNewUniqueID(), ADAPTOR_NAME, location, hostPort, new Path(cwd), client, xp);
    }

    private HttpClient getClient(String host, int port, PasswordCredential credential) {
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(host, port);
        HttpConnectionManager connectionManager = getConnectionManager(hostConfig);
        HttpClient client = new HttpClient(connectionManager);
        Credentials creds = new UsernamePasswordCredentials(credential.getUsername(), new String(credential.getPassword()));
        client.getState().setCredentials(AuthScope.ANY, creds);
        client.setHostConfiguration(hostConfig);
        return client;
    }

    private HttpConnectionManager getConnectionManager(HostConfiguration hostConfig) {
        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        int maxHostConnections = 20;
        params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
        connectionManager.setParams(params);
        return connectionManager;
    }

    public void end() {
        LOGGER.debug("end OK");
    }

    @Override
    public boolean isConnectionless() {

        return true;
    }

}
