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

package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.AdaptorFactory;
import nl.esciencecenter.xenon.engine.XenonEngine;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class FtpAdaptorFactory extends AdaptorFactory {

    @Override
    public String getPropertyPrefix() {
        return FtpAdaptor.PREFIX;
    }

    @Override
    public XenonPropertyDescription [] getSupportedProperties() {
        return FtpAdaptor.VALID_PROPERTIES.asArray();
    }

    @Override
    public Adaptor createAdaptor(XenonEngine engine, Map<String, String> properties) throws XenonException {
        return new FtpAdaptor(engine, properties);
    }
}
