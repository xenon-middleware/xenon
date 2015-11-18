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
package nl.esciencecenter.xenon;

import java.util.Set;

/**
 * <p>XenonPropertyDescription contains all necessary information about a property that is recognized by Xenon or one of its
 * components.</p> 
 * 
 * <p>Each XenonPropertyDescription contains the following information:</p>
 * 
 * <ul>
 * <li>
 * A name that uniquely identifies the property.
 * This name should be used as a key when passing properties to Xenon in a {@link java.util.Map}.
 * </li> 
 * 
 * <li>
 * A (human-readable) description that explains the use of the property.  
 * </li>
 * <li>
 * 
 * The type of values that are accepted for the property (one of the {@link Type} enum).
 * Even though it is customary to pass the values of properties as <code>String</code>s, the user should ensure the values can be
 * converted into the expected type. 
 * </li>
 * 
 * <li>
 * The default value of the property. 
 * </li>
 * 
 * <li>
 * The components by which this property is accepted (a <code>Set</code> of {@link Component}).
 * Properties are only valid for certain components of Xenon. For example, some properties may be used when creating an new 
 * <code>Xenon</code>, while others can be used when creating a new <code>Scheduler</code>, <code>FileSystem</code>, or 
 * <code>Credential</code>. 
 * </li>
 * </ul>
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface XenonPropertyDescription {

    /**
     * The Component enumeration lists all possible parts of Xenon for which a property can be provided. 
     */
    enum Component {
        /** 
         * Properties for <code>XENON</code> components can be passed to 
         * {@link nl.esciencecenter.xenon.XenonFactory#newXenon(Map)}. 
         */        
        XENON, 
        
        /** 
         * Properties for <code>SCHEDULER</code> components can be passed to 
         * {@link nl.esciencecenter.xenon.jobs.Jobs#newScheduler(String, String, Credential, Map)}. 
         */        
        SCHEDULER, 
        
        /** 
         * Properties for <code>FILESYSTEM</code> components can be passed to 
         * {@link nl.esciencecenter.xenon.files.Files#newFileSystem(String, String, Credential, Map)}. 
         */        
        FILESYSTEM, 
        
        /** 
         * Properties for <code>CREDENTIAL</code> components can be passed to the various <code>newCredential</code> calls in 
         * {@link nl.esciencecenter.xenon.credentials.Credentials}. 
         */        
        CREDENTIALS,
    }

    /**
     * This Type enumeration lists all possible types of properties recognized by Xenon.
     */
    enum Type {
        /** 
         * Properties of type <code>BOOLEAN</code> can be either <code>"true"</code> or <code>"false"</code>.  
         */        
        BOOLEAN, 
        
        /** 
         * Properties of type <code>INTEGER</code> can be converted into a 32-bit signed integer using 
         * {@link java.lang.Integer#valueOf(String)}.  
         */                
        INTEGER, 

        /** 
         * Properties of type <code>LONG</code> can be converted into a 64-bit signed long using 
         * {@link java.lang.Long#valueOf(String)}.  
         */                
        LONG, 
        
        /** 
         * Properties of type <code>DOUBLE</code> can be converted into a 64-bit floating point number using 
         * {@link java.lang.Double#valueOf(String)}.  
         */                
        DOUBLE, 

        /** 
         * Properties of type <code>STRING</code> are directly stored in a String without conversion. 
         */                
        STRING,
        
        /** 
         * Properties of type <code>SIZE</code> can be converted into a 64-bit signed long using 
         * {@link java.lang.Long#valueOf(String)}. In addition, the postfixes <code>"K"</code>, <code>"M"</code> and <code>"G"</code> may 
         * be used to multiply the value by <code>1024</code>, <code>1024*1024</code>, or <code>1024*1024*1024</code> 
         * respectively.   
         */                
        SIZE,
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