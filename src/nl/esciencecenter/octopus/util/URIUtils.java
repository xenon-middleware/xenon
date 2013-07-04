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
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class URIUtils {

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
