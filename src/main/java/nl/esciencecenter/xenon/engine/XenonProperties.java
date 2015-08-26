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
package nl.esciencecenter.xenon.engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.util.Utils;

/**
 * Read-only properties implementation. Also contains some utility functions for getting typed properties.
 */
public class XenonProperties {

    private static final String NAME = "XenonProperties";

    /** One kilo is 1024 */
    private static final int KILO = 1024;

    /** One mega is a kilo*kilo */
    private static final int MEGA = KILO * KILO;

    /** One giga is a kilo*kilo*kilo */
    private static final int GIGA = KILO * MEGA;

    /** Contains a description of all properties this XenonProperties should accept, including their type, default, etc. */
    private final Map<String, XenonPropertyDescription> supportedProperties;

    /** The properties that are actually set. */
    private final Map<String, String> properties;

    /**
     * Private constructor for XenonProperties using in copying and filtering. The <code>properties</code> parameter is assumed
     * to only contain valid supported properties and have values of the correct type.
     * 
     * @param supportedProperties
     *            a map containing a description of all supported properties.
     * @param properties
     *            a map containing valid properties and their values.
     */
    private XenonProperties(Map<String, XenonPropertyDescription> supportedProperties, Map<String, String> properties) {
        this.supportedProperties = supportedProperties;
        this.properties = properties;
    }

    /**
     * Creates an empty XenonProperties.
     */
    public XenonProperties() {
        supportedProperties = Utils.emptyMap(0);
        properties = Utils.emptyMap(0);
    }

    /**
     * Create a new XenonProperties that will support the properties in <code>supportedProperties</code>. All properties in
     * <code>properties</code> will be added.
     * 
     * @param supportedProperties
     *            the properties to support
     * @param properties
     *            the set of properties to store
     * @throws UnknownPropertyException
     *             if key is found in <code>properties</code> that is not listed in <code>supportedProperties</code>
     * @throws InvalidPropertyException
     *             if a key from <code>properties</code> has a value that does not match the type as listed in
     *             <code>supportedProperties</code>
     */
    public XenonProperties(ImmutableArray<XenonPropertyDescription> supportedProperties, Map<String, String> properties)
            throws UnknownPropertyException, InvalidPropertyException {

        super();

        this.supportedProperties = Utils.emptyMap(supportedProperties.length());
        this.properties = Utils.emptyMap(supportedProperties.length());

        for (XenonPropertyDescription d : supportedProperties) {
            this.supportedProperties.put(d.getName(), d);
        }

        addProperties(properties);
    }

    /**
     * Create a new XenonProperties that will support the properties in <code>supportedProperties</code> that are valid at level
     * <code>level</code>. All properties in <code>properties</code> will be added.
     * 
     * @param supportedProperties
     *            the properties to support
     * @param level
     * @param properties
     *            the set of properties to store
     * @throws UnknownPropertyException
     *             if key is found in <code>properties</code> that is not listed in <code>supportedProperties</code>, or not
     *             listed at level <code>level</code>.
     * @throws InvalidPropertyException
     *             if a key from <code>properties</code> has a value that does not match the type as listed in
     *             <code>supportedProperties</code>.
     */
    public XenonProperties(ImmutableArray<XenonPropertyDescription> supportedProperties, Component level, 
            Map<String, String> properties) throws UnknownPropertyException, InvalidPropertyException {

        super();

        this.supportedProperties = Utils.emptyMap(supportedProperties.length());
        this.properties = Utils.emptyMap(supportedProperties.length());

        for (XenonPropertyDescription d : supportedProperties) {
            if (d.getLevels().contains(level)) {
                this.supportedProperties.put(d.getName(), d);
            }
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
    private void addProperties(Map<String, String> properties) throws UnknownPropertyException, InvalidPropertyException {

        if (properties == null) {
            return;
        }

        for (Entry<String, String> e : properties.entrySet()) {

            String key = e.getKey();

            XenonPropertyDescription d = supportedProperties.get(key);

            if (d == null) {
                throw new UnknownPropertyException(NAME, "Unknown property " + key);
            }

            String value = e.getValue();

            checkType(d, key, value);

            this.properties.put(key, value);
        }
    }

    private void checkType(XenonPropertyDescription description, String key, String value) throws InvalidPropertyException {

        Type t = description.getType();

        try {
            switch (t) {
            case BOOLEAN:
                if (!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
                    throw new IllegalArgumentException("Not a boolean value: " + value);
                }
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
                parseSizeValue(value);
                break;
            case STRING:
                break;
            }
        } catch (IllegalArgumentException | InvalidPropertyException e) {
            throw new InvalidPropertyException(NAME, "Property \"" + key + "\" has invalid value: " + value + " (expected " + t 
                    + ")", e);
        }
    }

    /**
     * Check if this XenonProperties supports a property with the given name.
     * 
     * @param name
     *            the name of the property.
     * @return <code>true</code> if this XenonProperties supports a property with the given name, <code>false</code> otherwise.
     */
    public boolean supportsProperty(String name) {
        return supportedProperties.containsKey(name);
    }

    /**
     * Check if the property with the given name is set.
     * 
     * @param name
     *            the name of the property.
     * @return <code>true</code> if the property with the given name is set, <code>false</code> otherwise.
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     */
    public boolean propertySet(String name) throws UnknownPropertyException {

        if (!supportedProperties.containsKey(name)) {
            throw new UnknownPropertyException(NAME, "No such property: " + name);
        }

        return properties.containsKey(name);
    }

    /**
     * Retrieves the value of a property with the given name without checking its type.
     * 
     * If the property is not set, its default value will be returned. That the type of the value is not checked. Instead its
     * <code>String</code> representation is always returned.
     * 
     * @param name
     *            the name of the property.
     * @return the value of the property with the given name or its default value if it is not set.
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     */
    public String getProperty(String name) throws UnknownPropertyException {

        XenonPropertyDescription d = supportedProperties.get(name);

        if (d == null) {
            throw new UnknownPropertyException(NAME, "No such property: " + name);
        }

        String value = d.getDefaultValue();

        if (properties.containsKey(name)) {
            value = properties.get(name);
        }

        return value;
    }

    private String getProperty(String name, Type type) throws UnknownPropertyException, PropertyTypeException {

        XenonPropertyDescription d = supportedProperties.get(name);

        if (d == null) {
            throw new UnknownPropertyException(NAME, "No such property: " + name);
        }

        if (d.getType() != type) {
            throw new PropertyTypeException(NAME, "Property " + name + " is of type " + d.getType() + " not " + type);
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
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type boolean.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a boolean.
     */
    public boolean getBooleanProperty(String name) throws UnknownPropertyException, PropertyTypeException,
            InvalidPropertyException {

        String value = getProperty(name, Type.BOOLEAN);
            
        if (value.equalsIgnoreCase("true")) { 
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false; 
        } else { 
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected BOOLEAN)");
        }
    }

    /**
     * Retrieves the value of an integer property with the given name.
     * 
     * @return the value of an integer property with the given name.
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type integer.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a integer.
     */
    public int getIntegerProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.INTEGER);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected INTEGER)",
                    e);
        }
    }

    /**
     * Retrieves the value of an integer property with the given name.
     * 
     * @return the value of an integer property with the given name.
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type integer.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a integer.
     */
    public int getIntegerProperty(String name, int defaultValue) throws UnknownPropertyException, PropertyTypeException,
            InvalidPropertyException {

        String value = getProperty(name, Type.INTEGER);

        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected INTEGER)",
                    e);
        }
    }

    /**
     * Retrieves the value of an long property with the given name.
     * 
     * @return the value of an long property with the given name.
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type long.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a long.
     */
    public long getLongProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.LONG);

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected LONG)", e);
        }
    }

    /**
     * Retrieves the value of an double property with the given name.
     * 
     * @return the value of an double property with the given name.
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type double.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a double.
     */
    public double getDoubleProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.DOUBLE);

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected DOUBLE)",
                    e);
        }
    }

    /**
     * Retrieves the value of a string property with the given name.
     * 
     * @return the value of an string property with the given name.
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type string.
     */
    public String getStringProperty(String name) throws UnknownPropertyException, PropertyTypeException {
        return getProperty(name, Type.STRING);
    }

    private long parseSizeValue(String value) throws InvalidPropertyException {  
        try {
            if (value.endsWith("G") || value.endsWith("g")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * GIGA;
            }

            if (value.endsWith("M") || value.endsWith("m")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * MEGA;
            }

            if (value.endsWith("K") || value.endsWith("k")) {
                return Long.parseLong(value.substring(0, value.length() - 1)) * KILO;
            }

            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Invalid SIZE value: " + value, e);
        }
    }
    
    /**
     * Retrieves the value of a size property with the given name.
     * 
     * Valid values for the property are a long or a long a long followed by either a K, M or G. These size modifiers multiply the
     * value by 1024, 1024^2 and 1024^3 respectively.
     * 
     * @return the value of an size property with the given name.
     * @param name
     *            the name of the property
     * 
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type size.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a long.
     */
    public long getSizeProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {
        return parseSizeValue(getProperty(name, Type.SIZE));
    }

    /**
     * Returns a new XenonProperties that contains only the properties whose key start with a certain prefix.
     * 
     * @return an XenonProperties containing only the matching properties.
     * @param prefix
     *            the desired prefix
     */
    public XenonProperties filter(String prefix) {
        String tmp = prefix;
        if (tmp == null) {
            tmp = "";
        }

        Map<String, XenonPropertyDescription> remaining = Utils.emptyMap(supportedProperties.size());
        Map<String, String> p = Utils.emptyMap(properties.size());

        for (String key : supportedProperties.keySet()) {
            if (key.startsWith(tmp)) {
                remaining.put(key, supportedProperties.get(key));

                if (properties.containsKey(key)) {
                    p.put(key, properties.get(key));
                }
            }
        }

        return new XenonProperties(remaining, p);
    }

    /**
     * Returns a new XenonProperties that contains only the properties with a given level.
     * 
     * @return an XenonProperties containing only the properties with the matching level.
     * @param level
     *            the desired prefix
     */
    public XenonProperties filter(Component level) {
        Map<String, XenonPropertyDescription> remaining = Utils.emptyMap(supportedProperties.size());
        Map<String, String> p = Utils.emptyMap(properties.size());

        for (XenonPropertyDescription d : supportedProperties.values()) {
            if (d.getLevels().contains(level)) {
                String key = d.getName();
                remaining.put(key, d);

                if (properties.containsKey(key)) {
                    p.put(key, properties.get(key));
                }
            }
        }

        return new XenonProperties(remaining, p);
    }

    /**
     * Returns a copy of this XenonProperties that contains all properties except the properties that start with the given
     * prefix. Note that these properties are also removed from the supported properties set.
     * 
     * @param prefix
     *            the prefix of the properties to exclude
     * @return an XenonProperties containing all properties except the properties with the given prefix.
     */
    public XenonProperties exclude(String prefix) {
        String tmp = prefix;

        if (tmp == null) {
            tmp = "";
        }

        Map<String, XenonPropertyDescription> remaining = Utils.emptyMap(supportedProperties.size());
        Map<String, String> p = Utils.emptyMap(properties.size());

        for (String key : supportedProperties.keySet()) {
            if (!key.startsWith(tmp)) {
                remaining.put(key, supportedProperties.get(key));

                if (properties.containsKey(key)) {
                    p.put(key, properties.get(key));
                }
            }
        }

        return new XenonProperties(remaining, p);
    }

    /**
     * Returns a copy of this XenonProperties that contains all properties but clears the properties that start with the given
     * prefix. Note that these properties are not removed from the supported properties set.
     * 
     * @param prefix
     *            the prefix of the properties to exclude
     * @return an XenonProperties containing all properties except the properties with the given prefix.
     */
    public XenonProperties clear(String prefix) {
        String tmp = prefix;
        if (tmp == null) {
            tmp = "";
        }

        Map<String, XenonPropertyDescription> remaining = Utils.emptyMap(supportedProperties.size());
        Map<String, String> p = Utils.emptyMap(properties.size());

        for (String key : supportedProperties.keySet()) {
            remaining.put(key, supportedProperties.get(key));

            if (!key.startsWith(tmp)) {
                if (properties.containsKey(key)) {
                    p.put(key, properties.get(key));
                }
            }
        }

        return new XenonProperties(remaining, p);
    }

    /**
     * Returns the descriptions of all supported properties.
     * 
     * @return the descriptions of all supported properties.
     */
    public XenonPropertyDescription[] getSupportedProperties() {
        Collection<XenonPropertyDescription> tmp = supportedProperties.values();
        return tmp.toArray(new XenonPropertyDescription[tmp.size()]);
    }

    /**
     * Returns a sorted list of all supported property names.
     * 
     * @return Sorted list of supported property names.
     */
    public String[] getPropertyNames() {
        ArrayList<String> list = new ArrayList<>(supportedProperties.keySet());
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns all properties that are set in a Map.
     * 
     * @return all properties that are set in a Map.
     */
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Prints properties (including default properties) to a stream.
     * 
     * @param out
     *            The stream to write output to.
     * @param prefix
     *            Only print properties which start with the given prefix. If null, will print all properties
     */
    public void printProperties(PrintStream out, String prefix) {
        String tmp;
        if (prefix == null) {
            tmp = "";
        } else {
            tmp = prefix.toLowerCase();
        }

        for (XenonPropertyDescription d : supportedProperties.values()) {
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
        StringBuilder sb = new StringBuilder(supportedProperties.size() * 60);
        sb.append('{');

        String comma = "";

        for (XenonPropertyDescription d : supportedProperties.values()) {

            sb.append(comma);

            String key = d.getName();

            if (properties.containsKey(d.getName())) {
                sb.append(key);
                sb.append('=');
                sb.append(properties.get(key));
            } else {
                sb.append("<<");
                sb.append(key);
                sb.append('=');
                sb.append(d.getDefaultValue());
                sb.append(">>");
            }
            comma = ", ";
        }

        sb.append('}');

        return sb.toString();
    }
}
