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

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more create functions of the Xenon interface,
 * depending on which functionality they provide.
 * 
 */
public abstract class Adaptor {

    private final String name;
    private final String description;
    private final ImmutableArray<String> supportedSchemes;
    private final ImmutableArray<String> supportedLocations;
    
    protected final ImmutableArray<XenonPropertyDescription> validProperties;
    
    private final XenonProperties properties;
    
    protected Adaptor(String name, String description, ImmutableArray<String> supportedSchemes,
            ImmutableArray<String> supportedLocations, ImmutableArray<XenonPropertyDescription> validProperties, 
            XenonProperties properties) {

        super();
        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes;
        this.supportedLocations = supportedLocations;
        
        if (validProperties == null) {
            this.validProperties = new ImmutableArray<>();
        } else { 
            this.validProperties = validProperties;
        }
         
        this.properties = properties;
    }

    public XenonProperties getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    public boolean supports(String scheme) {

        for (String s : supportedSchemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    }

    public XenonPropertyDescription[] getSupportedProperties() {
        return validProperties.asArray();
    }

    public String[] getSupportedSchemes() {
        return supportedSchemes.asArray();
    }

    public String[] getSupportedLocations() {
        return supportedLocations.asArray();
    }
    
    @Override
    public String toString() {
        return "Adaptor [name=" + name + "]";
    }

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract void end();
}
