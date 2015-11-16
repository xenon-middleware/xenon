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

package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.credentials.Credential;

/**
 * A dummy Credential for local use.
 *
 * @author Christiaan Meijer <C.Meijer@esciencecenter.nl>
 * @version 1.1
 * @since 1.1
 */
public class FtpCredential implements Credential {

    @Override
    public String getAdaptorName() {
        return FtpAdaptor.ADAPTOR_NAME;
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<>();
    }
}
