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
package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.credentials.PasswordCredentialImplementation;

public class WebdavCredentials implements Credentials {
    private static int currentID = 1;
    private final WebdavAdaptor adaptor;

    private static synchronized String getNewUniqueID() {
        String res = "webdav" + currentID;
        currentID++;
        return res;
    }

    public WebdavCredentials(XenonProperties properties, WebdavAdaptor webdavAdaptor) {
        if (webdavAdaptor == null) {
            throw new IllegalArgumentException("Adaptor can not be null!");
        }

        adaptor = webdavAdaptor;
    }

    @Override
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws XenonException {
        throw new XenonException(adaptor.getName(), "CertificateCredential not supported!");
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws XenonException {
        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.CREDENTIALS), properties);
        return new PasswordCredentialImplementation(adaptor.getName(), getNewUniqueID(), xenonProperties, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws XenonException {
        return null;
    }

    @Override
    public void close(Credential credential) throws XenonException {
        // ignored
    }

    @Override
    public boolean isOpen(Credential credential) throws XenonException {
        return true;
    }

}
