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

package nl.esciencecenter.cobalt.adaptors.local;

import java.util.Map;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.credentials.Credentials;

/**
 * A Credentials for local use.
 * 
 * Only getDefaultCredential returns a (dummy) credential. All other methods throw an exception.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalCredentials implements Credentials {

    private final LocalCredential credential = new LocalCredential();

    @Override
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws CobaltException {
        throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "CertificateCredential not supported!");
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws CobaltException {
        throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "PasswordCredential not supported!");
    }
    
    @Override
    public Credential getDefaultCredential(String scheme) throws CobaltException {
        return credential;
    }

    @Override
    public void close(Credential credential) throws CobaltException {
        // ignored
    }

    @Override
    public boolean isOpen(Credential credential) throws CobaltException {
        return true;
    }
}
