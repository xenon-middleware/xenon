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

package nl.esciencecenter.xenon.adaptors.ssh;

import java.io.File;
import java.io.IOException;

import nl.esciencecenter.xenon.adaptors.CredentialTestConfig;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 *
 */
public class SSHCredentialTestConfig implements CredentialTestConfig {

    @Override
    public boolean supportsCertificateCredentials() {
        return true;
    }

    @Override
    public boolean supportsPasswordCredentials() {
        return true;
    }

    @Override
    public String getCorrectCertFile() throws IOException {
        
        String location = System.getProperty("user.home");
        
        File rsa = new File(location + File.separator + ".ssh" + File.separator + "id_rsa");
        
        if (rsa.exists()) { 
            return rsa.getAbsolutePath();
        }
        
        File dsa = new File(location + File.separator + ".ssh" + File.separator + "id_dsa");
        
        if (rsa.exists()) { 
            return dsa.getAbsolutePath();
        }

        throw new IOException("Cannot find an SSH key in " + location);
    }

    @Override
    public String getIncorrectCertFile() {
        return "aap";
    }

    @Override
    public String getUserName() {
        return "username";
    }

    @Override
    public char[] getPassword() {
        return "password".toCharArray();
    }

    @Override
    public String[] supportedSchemes() {
        return new String [] { "ssh", "sftp" };
    }

}
