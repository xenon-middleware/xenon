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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.PropertyTypeException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;

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

    /**
     * Creates empty mutable Map with sufficient initial capacity.
     *
     * @param <K>
     *            key type
     * @param <V>
     *            value type
     * @param capacity
     *            maximum size without resizing underlying data structure
     * @return an empty map
     */
    public static <K, V> Map<K, V> emptyMap(int capacity) {
        return new HashMap<>((capacity + 1) * 4 / 3);
    }

    /** Contains a description of all properties this XenonProperties should accept, including their type, default, etc. */
    private final Map<String, XenonPropertyDescription> propertyDescriptions;

    /** The properties that are actually set. */
    private final Map<String, String> properties;

    /**
     * Private constructor for XenonProperties using in copying and filtering. The <code>properties</code> parameter is assumed to only contain valid supported
     * properties and have values of the correct type.
     *
     * @param propertyDescriptions
     *            a map containing a description of all supported properties.
     * @param properties
     *            a map containing valid properties and their values.
     */
    private XenonProperties(Map<String, XenonPropertyDescription> propertyDescriptions, Map<String, String> properties) {
        this.propertyDescriptions = propertyDescriptions;
        this.properties = properties;
    }

    /**
     * Creates an empty XenonProperties.
     */
    public XenonProperties() {
        propertyDescriptions = emptyMap(0);
        properties = emptyMap(0);
    }

    /**
     * Create a new XenonProperties that will support the properties in <code>supportedProperties</code>. All properties in <code>properties</code> will be
     * added.
     *
     * @param propertyDescriptions
     *            the properties to support
     * @param properties
     *            the set of properties to store
     * @throws UnknownPropertyException
     *             if key is found in <code>properties</code> that is not listed in <code>supportedProperties</code>
     * @throws InvalidPropertyException
     *             if a key from <code>properties</code> has a value that does not match the type as listed in <code>supportedProperties</code>
     */
    public XenonProperties(XenonPropertyDescription[] propertyDescriptions, Map<String, String> properties)
            throws UnknownPropertyException, InvalidPropertyException {

        this.propertyDescriptions = new HashMap<>(propertyDescriptions.length);
        this.properties = new HashMap<>(propertyDescriptions.length);

        for (XenonPropertyDescription d : propertyDescriptions) {
            this.propertyDescriptions.put(d.getName(), d);
        }

        addProperties(properties);
    }

    /**
     * Adds the specified properties to the current ones and checks if their names and types are correct.
     *
     * @param properties
     *            the properties to add.
     * @throws UnknownPropertyException
     *             if the property can not be fonud
     * @throws InvalidPropertyException
     *             if the type of the value does not match the expected type
     */
    private void addProperties(Map<String, String> properties) throws UnknownPropertyException, InvalidPropertyException {

        if (properties == null) {
            return;
        }

        for (Entry<String, String> e : properties.entrySet()) {

            String key = e.getKey();

            XenonPropertyDescription d = propertyDescriptions.get(key);

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
                Integer.parseInt(value);
                break;
            case DOUBLE:
                Double.parseDouble(value);
                break;
            case LONG:
                Long.parseLong(value);
                break;
            case NATURAL:
                long tmp = Long.parseLong(value);

                if (tmp < 0) {
                    throw new IllegalArgumentException("Not a natural value: " + value);
                }
                break;
            case SIZE:
                parseSizeValue(value);
                break;
            case STRING:
                break;
            default:
                // All cases should have been handled above
                throw new InvalidPropertyException(NAME, "Unknown property \"" + key + "=" + value + " provided");
            }
        } catch (IllegalArgumentException | InvalidPropertyException e) {
            throw new InvalidPropertyException(NAME, "Property \"" + key + "\" has invalid value: " + value + " (expected " + t + ")", e);
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
        return propertyDescriptions.containsKey(name);
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

        if (!propertyDescriptions.containsKey(name)) {
            throw new UnknownPropertyException(NAME, "No such property: " + name);
        }

        return properties.containsKey(name);
    }

    /**
     * Retrieves the value of a property with the given name without checking its type.
     *
     * If the property is not set, its default value will be returned. That the type of the value is not checked. Instead its <code>String</code> representation
     * is always returned.
     *
     * @param name
     *            the name of the property.
     * @return the value of the property with the given name or its default value if it is not set.
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     */
    public String getProperty(String name) throws UnknownPropertyException {

        XenonPropertyDescription d = propertyDescriptions.get(name);

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

        XenonPropertyDescription d = propertyDescriptions.get(name);

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
    public boolean getBooleanProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

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
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected INTEGER)", e);
        }
    }

    /**
     * Retrieves the value of an integer property with the given name.
     *
     * @return the value of an integer property with the given name.
     * @param name
     *            the name of the property
     * @param defaultValue
     *            the value to return if the property is not found
     * @throws UnknownPropertyException
     *             if the given name is not a supported property.
     * @throws PropertyTypeException
     *             if the property is not of type integer.
     * @throws InvalidPropertyException
     *             if the property value cannot be converted into a integer.
     */
    public int getIntegerProperty(String name, int defaultValue) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.INTEGER);

        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected INTEGER)", e);
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
     * Retrieves the value of an natural number property (e.g. a long with value &ge; 0) with the given name.
     *
     * @return the value of an natural number property with the given name.
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
    public long getNaturalProperty(String name) throws UnknownPropertyException, PropertyTypeException, InvalidPropertyException {

        String value = getProperty(name, Type.NATURAL);

        try {
            long result = Long.parseLong(value);

            if (result < 0) {
                throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected NATURAL NUMBER)");
            }

            return result;
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected NATURAL NUMBER)", e);
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
            throw new InvalidPropertyException(NAME, "Property " + name + " has invalid value: " + value + " (expected DOUBLE)", e);
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
     * Valid values for the property are a long or a long a long followed by either a K, M or G. These size modifiers multiply the value by 1024, 1024^2 and
     * 1024^3 respectively.
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
    public XenonProperties filter(final String prefix) {
        return filterUsingPredicate(propertyKey -> propertyKey.startsWith(prefix));
    }

    /**
     * Returns a copy of this XenonProperties that contains all properties except the properties that start with the given prefix. Note that these properties
     * are also removed from the supported properties set.
     *
     * @param prefix
     *            the prefix of the properties to exclude
     * @return an XenonProperties containing all properties except the properties with the given prefix.
     */
    public XenonProperties exclude(final String prefix) {
        return filterUsingPredicate(propertyKey -> !propertyKey.startsWith(prefix));
    }

    private XenonProperties filterUsingPredicate(Predicate<String> predicate) {

        // first determine the relevant subset of propertyDescriptions
        Map<String, XenonPropertyDescription> remainingDescriptions = filterOnKey(propertyDescriptions, predicate);

        Map<String, String> remainingProperties = filterOnKey(properties, key -> remainingDescriptions.containsKey(key));

        return new XenonProperties(remainingDescriptions, remainingProperties);
    }

    private <K, V> Map<K, V> filterOnKey(Map<K, V> map, Predicate<K> predicate) {
        Map<K, V> remaining = emptyMap(map.size());

        for (Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (predicate.test(key)) {
                remaining.put(key, value);
            }
        }
        return remaining;
    }

    /**
     * Returns a copy of this XenonProperties that contains all properties but clears the properties that start with the given prefix. Note that these
     * properties are not removed from the supported properties set.
     *
     * @param prefix
     *            the prefix of the properties to exclude
     * @return an XenonProperties containing all properties except the properties with the given prefix.
     */
    public XenonProperties clear(final String prefix) {

        Map<String, XenonPropertyDescription> toInclude = filterOnKey(propertyDescriptions, propertyKey -> !propertyKey.startsWith(prefix));

        Map<String, String> propertiesRemaining = filterOnKey(properties, key -> toInclude.containsKey(key));

        return new XenonProperties(propertyDescriptions, propertiesRemaining);
    }

    /**
     * Returns the descriptions of all supported properties.
     *
     * @return the descriptions of all supported properties.
     */
    public XenonPropertyDescription[] getSupportedProperties() {
        Collection<XenonPropertyDescription> tmp = propertyDescriptions.values();
        return tmp.toArray(new XenonPropertyDescription[tmp.size()]);
    }

    /**
     * Returns a sorted list of all supported property names.
     *
     * @return Sorted list of supported property names.
     */
    public String[] getPropertyNames() {
        ArrayList<String> list = new ArrayList<>(propertyDescriptions.keySet());
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns all properties that are set in a new Map.
     *
     * @return all properties that are set in a new Map.
     */
    public Map<String, String> toMap() {
        return new HashMap<>(properties);
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

        for (XenonPropertyDescription d : propertyDescriptions.values()) {
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
        StringBuilder sb = new StringBuilder(propertyDescriptions.size() * 60);
        sb.append('{');

        String comma = "";

        for (XenonPropertyDescription d : propertyDescriptions.values()) {

            sb.append(comma);

            String key = d.getName();

            if (properties.containsKey(key)) {
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
