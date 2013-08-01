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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Level;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Type;
import nl.esciencecenter.octopus.exceptions.InvalidPropertyException;
import nl.esciencecenter.octopus.exceptions.PropertyTypeException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;

/**
 * Read-only properties implementation. Also contains some utility functions for getting typed properties.
 */
public class OctopusProperties {

    /** Contains a description of all properties this OctopusProperties should accept, including their type, default, etc. */
    private final Map<String, OctopusPropertyDescription> supportedProperties;
       
    /** The properties that are actually set. */
    private final Map<String, String> properties;
    
    /** 
     * Private constructor for OctopusProperties using in copying and filtering. The <code>properties</code> parameter is assumed
     * to only contain valid supported properties and have values of the correct type.   
     * 
     * @param supportedProperties a map containing a description of all supported properties. 
     * @param properties a map containing valid properties and their values. 
     */    
    private OctopusProperties(Map<String, OctopusPropertyDescription> supportedProperties, Map<String,String> properties) { 
        this.supportedProperties = supportedProperties;
        this.properties = properties;
    } 

    /** 
     * Creates an empty OctopusProperties.
     */
    public OctopusProperties() {
        supportedProperties = new HashMap<>();
        properties = new HashMap<>();
    }
    
    public OctopusProperties(OctopusPropertyDescription [] supportedProperties, Map<String,String> properties) 
            throws UnknownPropertyException, InvalidPropertyException {
        
        super();
        
        this.supportedProperties = new HashMap<>();
        this.properties = new HashMap<>();
        
        for (OctopusPropertyDescription d : supportedProperties) {
            this.supportedProperties.put(d.getName(), d);
        }
        
        addProperties(properties);
    }
    
    
    /**
     * Adds the specified properties to the current ones and checks if their names and types are correct.
     * 
     * @param properties
     *            the properties to add.
     * @throws UnknownPropertyException 
     * @throws InvalidPropertyException 
     */
    private final void addProperties(Map<String, String> properties) throws UnknownPropertyException, InvalidPropertyException {
        
        if (properties == null) {
            return;
        }

        for (Entry<String,String> e : properties.entrySet()) { 
            
            String key = e.getKey();

            OctopusPropertyDescription d = supportedProperties.get(key);
            
            if (d == null) { 
                throw new UnknownPropertyException("OctopusProperties", "Unknown property " + key); 
            }

            String value = e.getValue();
            
            checkType(d, key, value);
            
            this.properties.put(key, value);
        }
    }
    
    private final void checkType(OctopusPropertyDescription description, String key, String value) 
            throws InvalidPropertyException { 
        
        Type t = description.getType();
        
        try { 
            switch (t) { 
            case BOOLEAN:
                Boolean.valueOf(value);
                break;            
            case INTEGER:
                Integer.valueOf(value);
                break;            
            case DOUBLE:
                Double.valueOf(value);
                break;
            case LONG:
                Long.valueOf(value);
                break;
            case SIZE:
                getSizeProperty(value);
                break;
            case STRING:
                break;
            }
        } catch (Exception e) { 
            throw new InvalidPropertyException("OctopusProperties", "Property " + key + " has invalid value: " + value + 
                    " (expected " + t + ")", e);            
        }        
    }
    
    /**
     * Check if this OctopusProperties supports a property with the given name.
     *   
     * @param name the name of the property.
     * @return <code>true</code> if this OctopusProperties supports a property with the given name, <code>false</code> otherwise.
     */
    public boolean supportsProperty(String name) {
        return supportedProperties.containsKey(name);
    }

    /**
     * Check if the property with the given name is set. 
     *   
     * @param name the name of the property.
     * @return <code>true</code> if the property with the given name is set, <code>false</code> otherwise.
     * @throws UnknownPropertyException if the given name is not a supported property.  
     */
    public boolean propertySet(String name) throws UnknownPropertyException { 
        
        if (!supportedProperties.containsKey(name)) { 
            throw new UnknownPropertyException("OctopusProperties", "No such property: " + name);
        }
        
        return properties.containsKey(name);
    }
    
    /** 
     * Retrieves the value of a property with the given name without checking its type. 
     * 
     * If the property is not set, its default value will be returned. That the type of the value is not checked. Instead its 
     * <code>String</code> representation is always returned.
     * 
     * @param name the name of the property.
     * @return the value of the property with the given name or its default value if it is not set. 
     * @throws UnknownPropertyException if the given name is not a supported property.
     */
    public String getProperty(String name) throws UnknownPropertyException {

        OctopusPropertyDescription d = supportedProperties.get(name);
        
        if (d == null) {
            throw new UnknownPropertyException("OctopusProperties", "No such property: " + name);
        }
        
        String value = d.getDefaultValue();
        
        if (properties.containsKey(name)) { 
            value = properties.get(name);
        }
        
        return value;
    }
    
    private String getProperty(String name, Type type) throws UnknownPropertyException, PropertyTypeException { 

        OctopusPropertyDescription d = supportedProperties.get(name);
        
        if (d == null) {
            throw new UnknownPropertyException("OctopusProperties", "No such property: " + name);
        }
        
        if (d.getType() != type) { 
            throw new PropertyTypeException("OctopusProperties", "Property " + name + " is of type " + d.getType() + 
                    " not " + type);
        }
        
        String value = d.getDefaultValue();
        
        if (properties.containsKey(name)) { 
            value = properties.get(name);
        }

        return value;        
    }
    
    
    /**
     * Retrieves the value of a boolean property with the given name.
     * 
     * @return the value of a boolean property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type boolean.
     * @throws InvalidPropertyException if the property value cannot be converted into a boolean. 
     */
    public boolean getBooleanProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {
        
        String value = getProperty(name, Type.BOOLEAN);

        try { 
            return Boolean.parseBoolean(value);
        } catch (Exception e) { 
            throw new InvalidPropertyException("OctopusProperties", "Property " + name + " has invalid value: " + value + 
                    " (expected BOOLEAN)", e); 
        }
    }

    /**
     * Retrieves the value of an integer property with the given name.
     * 
     * @return the value of an integer property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type integer.
     * @throws InvalidPropertyException if the property value cannot be converted into a integer. 
     */
    public int getIntegerProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.INTEGER);

        try { 
            return Integer.parseInt(value);
        } catch (Exception e) { 
            throw new InvalidPropertyException("OctopusProperties", "Property " + name + " has invalid value: " + value + 
                    " (expected INTEGER)", e); 
        }
    }

    /**
     * Retrieves the value of an integer property with the given name.
     * 
     * @return the value of an integer property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type integer.
     * @throws InvalidPropertyException if the property value cannot be converted into a integer. 
     */
    public int getIntegerProperty(String name, int defaultValue) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.INTEGER);

        if (value == null || value.length() == 0) { 
            return defaultValue;
        }
            
        try { 
            return Integer.parseInt(value);
        } catch (Exception e) { 
            throw new InvalidPropertyException("OctopusProperties", "Property " + name + " has invalid value: " + value + 
                    " (expected INTEGER)", e); 
        }
    }
    
    /**
     * Retrieves the value of an long property with the given name.
     * 
     * @return the value of an long property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type long.
     * @throws InvalidPropertyException if the property value cannot be converted into a long. 
     */
    public long getLongProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {
        
        String value = getProperty(name, Type.LONG);

        try { 
            return Long.parseLong(value);
        } catch (Exception e) { 
            throw new InvalidPropertyException("OctopusProperties", "Property " + name + " has invalid value: " + value + 
                    " (expected LONG)", e); 
        }
    }
    
    /**
     * Retrieves the value of an double property with the given name.
     * 
     * @return the value of an double property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type double.
     * @throws InvalidPropertyException if the property value cannot be converted into a double. 
     */
    public double getDoubleProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.DOUBLE);

        try { 
            return Double.parseDouble(value);
        } catch (Exception e) { 
            throw new InvalidPropertyException("OctopusProperties", "Property " + name + " has invalid value: " + value + 
                    " (expected DOUBLE)", e); 
        }
    }

    /**
     * Retrieves the value of a string property with the given name.
     * 
     * @return the value of an string property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type string.
     */
    public String getStringProperty(String name) throws UnknownPropertyException, PropertyTypeException {
        return getProperty(name, Type.STRING);
    }

    /**
     * Retrieves the value of a size property with the given name.
     * 
     * Valid values for the property are a long or a long a long followed by either a K, M or G. These size modifiers multiply 
     * the value by 1024, 1024^2 and 1024^3 respectively.
     * 
     * @return the value of an size property with the given name.
     * @param name the name of the property
     *            
     * @throws UnknownPropertyException if the given name is not a supported property.
     * @throws PropertyTypeException if the property is not of type size.
     * @throws InvalidPropertyException if the property value cannot be converted into a long. 
     */
    public long getSizeProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {
        
        String value = getProperty(name, Type.SIZE);

        try {
            if (value.endsWith("G") || value.endsWith("g")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1024 * 1024 * 1024;
            }

            if (value.endsWith("M") || value.endsWith("m")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1024 * 1024;
            }

            if (value.endsWith("K") || value.endsWith("k")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * 1024;
            }

            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("OctopusProperties", "Property " + name + " has invalid value: " + value + 
                    " (expected SIZE)", e); 
        }
    }

    /**
     * Returns a new OctopusProperties that contains only the properties whose key start with a certain prefix.
     * 
     * @return an OctopusProperties containing only the matching properties.
     * @param tmp the desired prefix
     */
    public OctopusProperties filter(String prefix) {

        String tmp = prefix;
        
        if (tmp == null) {
            tmp = "";
        }
        
        HashMap<String, OctopusPropertyDescription> remaining = new HashMap<>();
        HashMap<String, String> p = new HashMap<>();
        
        for (String key : supportedProperties.keySet()) { 
            if (key.startsWith(tmp)) { 
                remaining.put(key, supportedProperties.get(key));
                
                if (properties.containsKey(key)) { 
                    p.put(key, properties.get(key));
                }               
            }
        }

        return new OctopusProperties(remaining, p);
    }
      
    public OctopusProperties filter(Level level) {

        HashMap<String, OctopusPropertyDescription> remaining = new HashMap<>();
        HashMap<String, String> p = new HashMap<>();
        
        for (OctopusPropertyDescription d : supportedProperties.values()) {
            
            if (d.getLevels().contains(level)) { 
                
                String key = d.getName();
                remaining.put(key, d);
   
                if (properties.containsKey(key)) { 
                    p.put(key, properties.get(key));
                }               
            }
        }

        return new OctopusProperties(remaining, p);
    }    

    /**
     * Returns the descriptions of all supported properties.
     * 
     * @return the descriptions of all supported properties. 
     */
    public OctopusPropertyDescription [] getSupportedProperties() { 
        return supportedProperties.values().toArray(new OctopusPropertyDescription[0]);
    }
    
    /**
     * Returns a sorted list of all supported property names.
     * 
     * @return Sorted list of supported property names.
     */
    public String[] getPropertyNames() {
        ArrayList<String> list = new ArrayList<String>(supportedProperties.keySet()); 
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }
    
    /**
     * Returns all properties that are set in a Map.  
     *  
     * @return all properties that are set in a Map.  
     */
    public Map<String,String> toMap() { 
       return Collections.unmodifiableMap(properties);
    }

    /**
     * Prints properties (including default properties) to a stream.
     * 
     * @param out
     *            The stream to write output to.
     * @param tmp
     *            Only print properties which start with the given prefix. If null, will print all properties
     */
    public void printProperties(PrintStream out, String prefix) {
        
        String tmp = prefix;
        
        if (tmp == null) {
            tmp = "";
        }
        
        tmp = tmp.toLowerCase();
        
        for (OctopusPropertyDescription d : supportedProperties.values()) {
        
            String key = d.getName();
            
            if (key.toLowerCase().startsWith(tmp)) {
            
                String value = d.getDefaultValue();

                if (properties.containsKey(key)) { 
                    value = properties.get(key);
                }
            
                out.println(key + " = " + value);
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("{");

        String comma = "";

        for (OctopusPropertyDescription d : supportedProperties.values()) {

            sb.append(comma);

            String key = d.getName();

            if (properties.containsKey(d.getName())) { 
                sb.append(key);
                sb.append("=");
                sb.append(properties.get(key));
            } else { 
                sb.append("<<");
                sb.append(key);
                sb.append("=");
                sb.append(d.getDefaultValue());
                sb.append(">>");
            }
            comma = ", ";
        }

        sb.append("}");
        
        return sb.toString();
    }

}
