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

package nl.esciencecenter.octopus.util;

import java.net.URI;

/**
 * URIUtils contains various URI utilities.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0 
 */
public final class URIUtils {

    private URIUtils() { 
        // DO NOTE USE
    }
    
    /**
     * Strips the given URI from all elements that are not needed when creating a FileSystem. 
     * 
     * The new URI will only contain the scheme, host, user info and port number of the original URI.  
     * 
     * @param uri 
     *          the original URI. 
     * @return 
     *          the stripped URI suitable to create a FileSystem.
     */    
    public static URI getFileSystemURI(URI uri) {

        // Strip the URI of all the parts we do not need for octopus. 
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String user = uri.getRawUserInfo();
        int port = uri.getPort();

        StringBuilder tmp = new StringBuilder(scheme);
        tmp.append("://");

        if (user != null && user.length() > 0) {
            tmp.append(user);
            tmp.append("@");
        }

        if (host != null) {
            tmp.append(host);
        }

        if (port >= 0) {
            tmp.append(":");
            tmp.append(port);
        }

        tmp.append("/");

        return URI.create(tmp.toString());
    }
}
