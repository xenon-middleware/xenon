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
package nl.esciencecenter.xenon.adaptors;

import nl.esciencecenter.xenon.AdaptorDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription;

public abstract class Adaptor implements AdaptorDescription {

    private static int currentID = 1;

    private final String name;
    private final String description;
    private final String [] supportedLocations;
    private final XenonPropertyDescription [] supportedProperties;

    protected Adaptor(String name, String description, String [] locations, XenonPropertyDescription [] properties) {
        this.name = name;
        this.description = description;
        this.supportedLocations = locations;
        this.supportedProperties = properties;
    }

    protected synchronized String getNewUniqueID() {
        String res = name + "." + currentID;
        currentID++;
        return res;
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
    public String[] getSupportedLocations() {
        return supportedLocations.clone();
    }

    @Override
    public XenonPropertyDescription[] getSupportedProperties() {
        return supportedProperties.clone();
    }
}
