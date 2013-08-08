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
package nl.esciencecenter.octopus.engine;

import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.OctopusPropertyDescription;

public class AdaptorStatusImplementation implements AdaptorStatus {

    private final String name;
    private final String description;
    private final String[] supportedSchemes;
    private final OctopusPropertyDescription[] supportedProperties;
    private final Map<String, String> adaptorSpecificInformation;

    public AdaptorStatusImplementation(String name, String description, String[] supportedSchemes,
            OctopusPropertyDescription[] supportedProperties, Map<String, String> adaptorSpecificInformation) {

        super();
        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes;
        this.supportedProperties = supportedProperties;
        this.adaptorSpecificInformation = adaptorSpecificInformation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getSupportedSchemes() {
        return supportedSchemes.clone();
    }

    @Override
    public OctopusPropertyDescription[] getSupportedProperties() {

        if (supportedProperties == null) {
            return new OctopusPropertyDescription[0];
        }

        return supportedProperties.clone();
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return adaptorSpecificInformation;
    }

    @Override
    public String toString() {
        return "AdaptorStatusImplementation [name=" + name + ", description=" + description + ", supportedSchemes="
                + Arrays.toString(supportedSchemes) + ", supportedProperties=" + Arrays.toString(supportedProperties)
                + ", adaptorSpecificInformation=" + adaptorSpecificInformation + "]";
    }
}
