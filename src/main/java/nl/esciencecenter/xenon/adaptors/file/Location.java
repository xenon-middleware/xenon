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
package nl.esciencecenter.xenon.adaptors.file;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.xenon.adaptors.InvalidLocationException;

public abstract class Location {
    private final String user;
    private final String host;
    private final int port;
    private final String scheme;
    private final String path;
    private static final String SCHEME_SEPARATOR = "://";
    private static final String DUMMY_SCHEME = "dummy";

    public Location(String user, String host, int port) {
        this(user, host, port, null, "");
    }

    public Location(String user, String host, int port, String scheme, String path) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        this.path = path;
    }

    /**
     * Parses a location URI as a Location.
     * @param location string containing a URI, the scheme may be omitted.
     * @param defaultScheme scheme to use if the default scheme is not found
     * @throws InvalidLocationException
     *      if the location is null or
     *          not a valid URI or
     *      if the port number is provided but not positive.
     */
    protected Location(String location, String defaultScheme) throws InvalidLocationException {
        if (location == null) {
            throw new InvalidLocationException(getAdaptorName(), "location may not be null.");
        }
        // Augment location with dummy scheme, if needed, to allow processing by URI
        boolean hasScheme = location.contains(SCHEME_SEPARATOR);
        String uriLocation = hasScheme ? location : DUMMY_SCHEME + SCHEME_SEPARATOR + location;

        try {
            URI url = new URI(uriLocation);
            String userPart = url.getUserInfo();
            if (userPart == null || userPart.isEmpty()) {
                user = null;
            } else {
                user = userPart;
            }
            host = url.getHost();
            port = url.getPort();
            scheme = hasScheme ? url.getScheme() : defaultScheme;
            path = url.getPath();
            validate();
        } catch (URISyntaxException e) {
            throw new InvalidLocationException(getAdaptorName(), "Could not parse location " + location, e);
        }
        if (port <= 0 && port != -1) {
            throw new InvalidLocationException(getAdaptorName(), "Port number of " + location + " must be positive or omitted");
        }
    }

    private void validate() throws URISyntaxException {
        if (host == null) {
            throw new URISyntaxException(getAdaptorName(), "Could not extract host from URI");
        }
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return usesDefaultPort() ? getDefaultPort() : port;
    }

    public String getScheme() {
        return scheme;
    }

    protected abstract String getAdaptorName();

    protected abstract int getDefaultPort();

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(100);
        if (scheme != null) {
            result.append(scheme).append(SCHEME_SEPARATOR);
        }
        if (user != null) {
            result.append(user).append('@');
        }
        result.append(host).append(':').append(getPort());
        result.append(path);

        return result.toString();
    }

    /* Whether the default port was used because none was provided. */
    public boolean usesDefaultPort() {
        return port == -1;
    }

    public String getPath() {
        return path;
    }
}
