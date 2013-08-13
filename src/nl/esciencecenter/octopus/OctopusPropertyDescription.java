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

package nl.esciencecenter.octopus;

import java.util.Set;

/**
 * OctopusPropertyDescription contains a all necessary information about a property that is recognized by octopus or one of its
 * components. 
 * 
 * Each OctopusPropertyDescription contains the following information:
 * 
 * A name that uniquely identifies the property. This name should be used as a key when passing properties to octopus in a 
 * {@link java.util.Map}. 
 * 
 * A (human-readable) description that explains the use of the property.  
 * 
 * The type of values that are accepted for the property (one of the {@link Type} enum). Even though it is customary to pass 
 * the values of properties as <code>String</code>s, the user should ensure the values can be converted into the expected type. 
 * 
 * The default value of the property. 
 * 
 * The components by which this property is accepted (a <code>Set</code> of {@link Component}). Properties are only valid for 
 * certain components of octopus. For example, some properties may be used when creating an new <code>Octopus</code>, while 
 * others can be used when creating a new <code>Scheduler</code>, <code>FileSystem</code>, or <code>Credential</code>. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface OctopusPropertyDescription {

    /**
     * Level names all possible levels at which a property can be provided.
     */
    public enum Component {
        OCTOPUS, SCHEDULER, FILESYSTEM, CREDENTIALS,
    }

    /**
     * Type names all possible types of properties.
     */
    public enum Type {
        BOOLEAN, INTEGER, LONG, DOUBLE, STRING, SIZE,
    }

    /**
     * Returns the name of the property.
     * 
     * @return the name of the property.
     */
    String getName();

    /**
     * Returns the type of the property.
     * 
     * @return the type of the property.
     */
    Type getType();

    /**
     * Return a set containing all components that accept this property.
     * 
     * @return a set containing all components that accept this property.
     */
    Set<Component> getLevels();

    /**
     * Returns the default value for this property.
     * 
     * @return the default value for this property or <code>null</code> is no default is set.
     */
    String getDefaultValue();

    /**
     * Returns a human readable description of this property.
     * 
     * @return a human readable description of this property.
     */
    String getDescription();
}