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
 * OctopusPropertyDescription contains a description of a property that is recognized by octopus or one of its components.  
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface OctopusPropertyDescription {

    /** 
     * Level names all possible levels at which a property can be provided.  
     */
    public enum Level {
        OCTOPUS, 
        SCHEDULER, 
        FILESYSTEM,
        CREDENTIALS, 
//        JOBS, 
//        FILES, 
//        CREDENTIAL, 
    }
    
    /** 
     * Type names all possible types of properties. 
     */
    public enum Type {
        BOOLEAN, 
        INTEGER, 
        LONG, 
        DOUBLE,
        STRING, 
        SIZE,
    }

    /** 
     * Returns the name of the property.
     * 
     * @return the name of the property.
     */
    public String getName();

    /**
     * Returns the type of the property. 
     * 
     * @return the type of the property.
     */
    public Type getType();
    
    /**
     * Return a set containing all levels at which this property can be set. 
     * 
     * @return  a set containing all levels at which this property can be set.
     */
    public Set<Level> getLevels();

    /** 
     * Returns the default value for this property. 
     * 
     * @return the default value for this property or <code>null</code> is no default is set.
     */
    public String getDefaultValue();
    
    /** 
     * Returns a human readable description of this property.
     *  
     * @return a human readable description of this property.
     */
    public String getDescription();
}