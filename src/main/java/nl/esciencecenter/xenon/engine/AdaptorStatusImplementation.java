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
package nl.esciencecenter.xenon.engine;

import java.util.Map;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.util.Utils;

public class AdaptorStatusImplementation implements AdaptorStatus {

    private final String name;
    private final String description;
    private final ImmutableArray<String> supportedJobSchemes;
    private final ImmutableArray<String> supportedFileSchemes;

    private final ImmutableArray<String> supportedLocations;
    private final ImmutableArray<XenonPropertyDescription> supportedProperties;
    private final Map<String, String> adaptorSpecificInformation;

    public AdaptorStatusImplementation(String name, String description, ImmutableArray<String> supportedJobSchemes,
            ImmutableArray<String> supportedFileSchemes, ImmutableArray<String> supportedLocations, 
            ImmutableArray<XenonPropertyDescription> supportedProperties, Map<String, String> adaptorSpecificInformation) {

        super();
        this.name = name;
        this.description = description;
        
        if (supportedJobSchemes == null && supportedFileSchemes == null) { 
            throw new IllegalArgumentException("Both Job and File schemes are null!");
        }
        
        if (supportedJobSchemes == null) { 
            this.supportedJobSchemes = new ImmutableArray<>();
        } else { 
            this.supportedJobSchemes = supportedJobSchemes;
        }
            
        if (supportedFileSchemes == null) { 
            this.supportedFileSchemes = new ImmutableArray<>();
        } else { 
            this.supportedFileSchemes = supportedFileSchemes;
        }
        
        this.supportedLocations = supportedLocations;
        
        if (supportedProperties == null) { 
            throw new IllegalArgumentException("SupportedProperties is null!");
        }
        
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
    public String[] getSupportedJobSchemes() {
        return supportedJobSchemes.asArray();
    }

    @Override
    public String[] getSupportedFileSchemes() {
        return supportedFileSchemes.asArray();
    }
    
    @Override
    public String[] getSupportedSchemes() {
        return Utils.merge(getSupportedJobSchemes(), getSupportedFileSchemes());
    }

    @Override
    public XenonPropertyDescription[] getSupportedProperties() {
        return supportedProperties.asArray();
    }

    @Override
    public String[] getSupportedLocations() {
        return supportedLocations.asArray();
    }
    
    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return adaptorSpecificInformation;
    }

    @Override
    public String toString() {
        return "AdaptorStatusImplementation [name=" + name + ", description=" + description + ", supportedJobSchemes="
                + supportedJobSchemes + ", supportedFileSchemes=" + supportedFileSchemes +", supportedProperties=" 
                + supportedProperties + ", adaptorSpecificInformation=" + adaptorSpecificInformation + "]";
    }
}
