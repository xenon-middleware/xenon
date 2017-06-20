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

package nl.esciencecenter.xenon.adaptors.file.sftp;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.files.FileAdaptor;
import nl.esciencecenter.xenon.engine.files.FileAdaptorFactory;
import nl.esciencecenter.xenon.engine.files.FilesEngine;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class SftpFileAdaptorFactory implements FileAdaptorFactory {

    @Override
    public String getPropertyPrefix() {
        return SftpProperties.PREFIX;
    }

    @Override
    public XenonPropertyDescription [] getSupportedProperties() {
        return SftpProperties.VALID_PROPERTIES.asArray();
    }

    @Override
    public FileAdaptor createAdaptor(FilesEngine engine) throws XenonException {
        return new SftpFiles(engine);
    }
}
