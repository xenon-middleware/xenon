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

package nl.esciencecenter.xenon.engine.files;

import java.util.Map;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.AdaptorDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.FileAdaptorDescription;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class FileAdaptorDescriptionImplementation extends AdaptorDescriptionImplementation implements FileAdaptorDescription {

    private final boolean supportsThirdPartyCopy;
    
    /**
     * 
     * 
     * @param name
     * @param description
     * @param supportedLocations
     * @param supportedProperties
     * @param adaptorSpecificInformation
     */
    public FileAdaptorDescriptionImplementation(String name, String description, ImmutableArray<String> supportedLocations,
            ImmutableArray<XenonPropertyDescription> supportedProperties, Map<String, String> adaptorSpecificInformation, 
            boolean supportsThirdPartyCopy) {
        super(name, description, supportedLocations, supportedProperties, adaptorSpecificInformation);
        this.supportsThirdPartyCopy = supportsThirdPartyCopy;
    }

    @Override
    public boolean supportsThirdPartyCopy() {
        return supportsThirdPartyCopy;
    }
}
