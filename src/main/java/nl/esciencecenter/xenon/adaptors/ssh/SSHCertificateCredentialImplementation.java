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

import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.credentials.CertificateCredentialImplementation;

/**
 * @version 1.0
 * @since 1.0
 */
public class SSHCertificateCredentialImplementation extends CertificateCredentialImplementation {

    private final boolean useAgent;
    
    /**
     * Create a new SSHCertificateCredentialImplementation. 
     * 
     * SSHCertificateCredentialImplementation simply extends CertificateCredentialImplementation by adding a single boolean 
     * value useAgent that determines if the credential access should be delegated to ssh-agent. 
     * 
     * @param adaptorName
     *          the name of the adaptor that created this SSHCertificateCredentialImplementation
     * @param uniqueID
     *          a unique ID for this SSHCertificateCredentialImplementation
     * @param properties
     *          properties that should be used by this SSHCertificateCredentialImplementation (ignored)
     * @param certfile
     *          the certificate file to use
     * @param username
     *          the username to use (ignored)
     * @param passphrase
     *          the passphrase needed to access the certificate. May be null if the certificate has no passphrase or if the 
     *          access is delegated to an ssh-agent. 
     */
    protected SSHCertificateCredentialImplementation(String adaptorName, String uniqueID, XenonProperties properties,
            String certfile, String username, char[] passphrase, boolean useAgent) {
        super(adaptorName, uniqueID, properties, certfile, username, passphrase);
        this.useAgent = useAgent;
    }

    /**
     * Returns if this SSHCertificateCredentialImplementation should use the ssh-agent to delegate certificate access.
     * 
     * @return
     *          if ssh-agent delegation should be used.         
     */
    protected final boolean usingAgent() {
        return useAgent;
    }
    
}
