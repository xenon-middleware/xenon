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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Read-only properties implementation. Also contains some utility functions for getting typed properties.
 */
public class OctopusProperties extends Properties {

    private static final long serialVersionUID = 1L;

    /** Constructs a properties object. */
    public OctopusProperties(Properties... content) {
        super();
        for (Properties properties : content) {
            addProperties(properties);
        }
    }

    /** Constructs a properties object. */
    public OctopusProperties(String[][] defaults, Properties... content) {
        super();

        for (String[] element : defaults) {

            if (element[1] != null) {
                //    System.out.println("Adding property: " + element[0]  + " " + element[1]);
                super.put(element[0], element[1]);
            }
        }

        for (Properties properties : content) {
            addProperties(properties);
        }
    }

    /**
     * Adds the specified properties to the current ones.
     * 
     * @param properties
     *            the properties to add.
     */
    private void addProperties(Properties properties) {
        if (properties == null) {
            return;
        }

        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = properties.getProperty(key);

            // System.out.println("** Adding property: " + key  + " " + value);

            super.put(key, value);
        }
    }

    /**
     * Loads properties from a properties file on the classpath.
     * 
     * @param resourceName
     *            the name of the resource to load properties from.
     */
    //    public static Properties loadFromClassPath(String resourceName) throws OctopusException {
    //        Properties result = new Properties();
    //
    //        ClassLoader classLoader = result.getClass().getClassLoader();
    //        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
    //            if (inputStream == null) {
    //                throw new OctopusException("cannot find resource " + resourceName, null, null);
    //            }
    //
    //            result.load(inputStream);
    //        } catch (IOException e) {
    //            throw new OctopusException("OctopusProperties", "cannot load properties from stream", e);
    //        }
    //
    //        return new Properties(result);
    //    }
    //
    /**
     * Loads ImmutableTypedProperties from a file.
     * 
     * @param fileName
     *            name of file to load from.
     */
    //    public static Properties loadFromFile(String fileName) throws OctopusException {
    //        Properties result = new Properties();
    //
    //        try (FileInputStream inputStream = new FileInputStream(fileName)) {
    //            result.load(inputStream);
    //        } catch (IOException e) {
    //            throw new OctopusException("OctopusProperties", "cannot load properties from stream", e);
    //        }
    //
    //        return new Properties(result);
    //    }

    /**
     * Tries to load properties from a file, which is located relative to the users home directory. Does not throw any exceptions
     * if unsuccessful.
     * 
     * @param fileName
     *            name of file to load from.
     */
    //    public static void loadFromHomeFile(String fileName) throws OctopusException {
    //        loadFromFile(System.getProperty("user.home") + File.separator + fileName);
    //    }

    /**
     * Returns true if property <code>name</code> is defined and has a value that is conventionally associated with 'true' (as in
     * Ant): any of 1, on, true, yes, or nothing.
     * 
     * @return true if property is defined and set
     * @param name
     *            property name
     */
    public boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }

    /**
     * Returns true if property <code>name</code> has a value that is conventionally associated with 'true' (as in Ant): any of 1,
     * on, true, yes, or nothing. If the property is not defined, return the specified default value.
     * 
     * @return true if property is defined and set
     * @param key
     *            property name
     * @param defaultValue
     *            the value that is returned if the property is absent
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);

        if (value != null) {
            return value.equals("1") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("yes");
        }

        return defaultValue;
    }

    /**
     * Returns the integer value of property.
     * 
     * @return the integer value of property
     * @param key
     *            property name
     * @throws NumberFormatException
     *             if the property is undefined or not an integer
     */
    public int getIntProperty(String key) {
        String value = getProperty(key);

        if (value == null) {
            throw new NumberFormatException("property undefined: " + key);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Integer expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the integer value of property.
     * 
     * @return the integer value of property
     * @param key
     *            property name
     * @param defaultValue
     *            default value if the property is undefined
     * @throws NumberFormatException
     *             if the property defined and not an integer
     */
    public int getIntProperty(String key, int defaultValue) {

        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Integer expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the long value of property.
     * 
     * @return the long value of property
     * @param key
     *            property name
     * @throws NumberFormatException
     *             if the property is undefined or not an long
     */
    public long getLongProperty(String key) {
        String value = getProperty(key);

        if (value == null) {
            throw new NumberFormatException("property undefined: " + key);
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Long expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the long value of property.
     * 
     * @return the long value of property
     * @param key
     *            property name
     * @param defaultValue
     *            default value if the property is undefined
     * @throws NumberFormatException
     *             if the property defined and not an Long
     */
    public long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Long expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the short value of property.
     * 
     * @return the short value of property
     * @param key
     *            property name
     * @throws NumberFormatException
     *             if the property is undefined or not an short
     */
    public short getShortProperty(String key) {
        String value = getProperty(key);

        if (value == null) {
            throw new NumberFormatException("property undefined: " + key);
        }

        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Short expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the short value of property.
     * 
     * @return the short value of property
     * @param key
     *            property name
     * @param defaultValue
     *            default value if the property is undefined
     * @throws NumberFormatException
     *             if the property defined and not an Short
     */
    public short getShortProperty(String key, short defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Short expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the double value of property.
     * 
     * @return the double value of property
     * @param key
     *            property name
     * @throws NumberFormatException
     *             if the property is undefined or not an double
     */
    public double getDoubleProperty(String key) {
        String value = getProperty(key);

        if (value == null) {
            throw new NumberFormatException("property undefined: " + key);
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Double expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the double value of property.
     * 
     * @return the double value of property
     * @param key
     *            property name
     * @param defaultValue
     *            default value if the property is undefined
     * @throws NumberFormatException
     *             if the property defined and not an Double
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Double expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the float value of property.
     * 
     * @return the float value of property
     * @param key
     *            property name
     * @throws NumberFormatException
     *             if the property is undefined or not an float
     */
    public float getFloatProperty(String key) {
        String value = getProperty(key);

        if (value == null) {
            throw new NumberFormatException("property undefined: " + key);
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Float expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the float value of property.
     * 
     * @return the float value of property
     * @param key
     *            property name
     * @param defaultValue
     *            default value if the property is undefined
     * @throws NumberFormatException
     *             if the property defined and not an Float
     */
    public float getFloatProperty(String key, float defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Float expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the long value of a size property. Valid values for the property are a long, a long followed by K, a long followed
     * by M or a long followed by G. Size modifiers multiply the value by 1024, 1024^2 and 1024^3 respectively.
     * 
     * @return the size value of property
     * @param key
     *            property name
     * @throws NumberFormatException
     *             if the property is undefined or not a valid size
     */
    public long getSizeProperty(String key) {
        String value = getProperty(key);

        if (value == null) {
            throw new NumberFormatException("property undefined: " + key);
        }

        return getSizeProperty(key, 0);
    }

    /**
     * Returns the long value of a size property. Valid values for the property are a long, a long followed by K, a long followed
     * by M or a long followed by G. Size modifiers multiply the value by 1024, 1024^2 and 1024^3 respectively. Returns the
     * default value if the property is undefined.
     * 
     * @return the size value of property
     * @param key
     *            property name
     * @param defaultValue
     *            the default value
     * @throws NumberFormatException
     *             if the property is not a valid size
     */
    public long getSizeProperty(String key, long defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

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
            throw new NumberFormatException("Long[G|g|M|m|K|k] expected for property " + key + ", not \"" + value + "\"");
        }
    }

    /**
     * Returns the split-up value of a string property. The value is supposed to be a comma-separated string, with each comma
     * preceded and followed by any amount of whitespace. See {@link java.lang.String#split(String)} for details of the splitting.
     * If the property is not defined, an empty array of strings is returned.
     * 
     * @param key
     *            the property name
     * @return the split-up property value.
     */
    public String[] getStringList(String key) {
        return getStringList(key, "\\s*,\\s*", new String[0]);
    }

    /**
     * Returns the split-up value of a string property. The value is split up according to the specified delimiter. See
     * {@link java.lang.String#split(String)} for details of the splitting. If the property is not defined, an empty array of
     * strings is returned.
     * 
     * @param key
     *            the property name
     * @param delim
     *            the delimiter
     * @return the split-up property value.
     */
    public String[] getStringList(String key, String delim) {
        return getStringList(key, delim, new String[0]);
    }

    /**
     * Returns the split-up value of a string property. The value is split up according to the specified delimiter. See
     * {@link java.lang.String#split(String)} for details of the splitting. If the property is not defined, the specified default
     * value is returned.
     * 
     * @param key
     *            the property name
     * @param delim
     *            the delimiter
     * @param defaultValue
     *            the default value
     * @return the split-up property value.
     */
    public String[] getStringList(String key, String delim, String[] defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return value.split(delim);
    }

    /**
     * Returns true if the given element is a member of the given list.
     * 
     * @param list
     *            the given list.
     * @param element
     *            the given element.
     * @return true if the given element is a member of the given list.
     */
    //    private static boolean contains(String[] list, String element) {
    //        if (list == null) {
    //            return false;
    //        }
    //        for (int i = 0; i < list.length; i++) {
    //            if (element.equalsIgnoreCase(list[i])) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

    /**
     * Returns true if the given string starts with one of the given prefixes.
     * 
     * @param string
     *            the given string.
     * @param prefixes
     *            the given prefixes.
     * @return true if the given string starts with one of the given prefixes.
     */
    //    private static boolean startsWith(String string, String[] prefixes) {
    //        if (prefixes == null) {
    //            return false;
    //        }
    //        for (int i = 0; i < prefixes.length; i++) {
    //            if (string.startsWith(prefixes[i])) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

    /**
     * Checks all properties with the given prefix for validity.
     * 
     * @return a Property object containing all unrecognized properties.
     * @param prefix
     *            the prefix that should be checked
     * @param validKeys
     *            the set of valid keys (all with the prefix).
     * @param validSubPrefixes
     *            if a property starts with one of these prefixes, it is declared valid
     * @param printWarning
     *            if true, a warning is printed to standard error for each unknown property
     */
    //    private Properties checkProperties(String prefix, String[] validKeys, String[] validSubPrefixes, boolean printWarning) {
    //
    //        Properties result = new Properties();
    //
    //        if (prefix == null) {
    //            prefix = "";
    //        }
    //
    //        for (Enumeration<?> e = propertyNames(); e.hasMoreElements();) {
    //            String key = (String) e.nextElement();
    //
    //            if (key.startsWith(prefix)) {
    //                String suffix = key.substring(prefix.length());
    //                String value = getProperty(key);
    //
    //                if (!startsWith(suffix, validSubPrefixes) && !contains(validKeys, key)) {
    //                    if (printWarning) {
    //                        System.err.println("Warning, unknown property: " + key + " with value: " + value);
    //                    }
    //                    result.put(key, value);
    //                }
    //            }
    //        }
    //
    //        return result;
    //    }

    /**
     * Returns all properties who's key start with a certain prefix.
     * 
     * @return a Property object containing all matching properties.
     * @param prefix
     *            the desired prefix
     */
    public OctopusProperties filter(String prefix) {

        Properties result = new Properties();

        if (prefix == null) {
            prefix = "";
        }

        for (Enumeration<?> e = propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();

            if (key.startsWith(prefix)) {
                result.put(key, getProperty(key));
            }
        }

        return new OctopusProperties(result);
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
        if (prefix == null) {
            prefix = "";
        }

        for (Enumeration<?> e = propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = getProperty(key);

            if (key.toLowerCase().startsWith(prefix.toLowerCase())) {
                out.println(key + " = " + value);
            }
        }
    }

    /**
     * @return Sorted list of property names.
     */
    String[] getPropertyNames() {
        ArrayList<String> list = new ArrayList<String>();
        for (Enumeration<?> e = propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            list.add(key);
        }
        String[] result = list.toArray(new String[list.size()]);
        Arrays.sort(result);
        return result;
    }

    /**
     * Compares this object to the specified object. They are equal if they have the same property names and values.
     * 
     * @param object
     *            object to compare to.
     * @return <code>true</code> if equal.
     */
    //
    //    public boolean equals(Object object) {
    //        if (!(object instanceof Properties)) {
    //            return false;
    //        }
    //
    //        Properties other = (Properties) object;
    //
    //        if (other.size() != size()) {
    //            return false;
    //        }
    //
    //        for (Map.Entry<Object, Object> entry : entrySet()) {
    //            if (!other.containsKey(entry.getKey())) {
    //                return false;
    //            }
    //
    //            Object value = entry.getValue();
    //            Object otherValue = other.get(entry.getKey());
    //
    //            if (value == null && otherValue != null | value != null && otherValue == null || value != null
    //                    && !value.equals(otherValue)) {
    //                return false;
    //            }
    //        }
    //        return true;
    //    }

    // Override all functions that change the content of this object.

    @Override
    public Object setProperty(String key, String value) {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public void putAll(Map<? extends Object, ? extends Object> t) {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public void load(Reader reader) throws IOException {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public void load(InputStream inStream) throws IOException {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        throw new UnsupportedOperationException("setting properties unsupported in ImmutableTypedProperties");
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("OctopusProperties [properties={");

        Set<Entry<Object, Object>> tmp = entrySet();

        String comma = "";

        for (Entry<Object, Object> e : tmp) {
            sb.append(comma);
            sb.append(e.getKey().toString());
            sb.append("=");
            sb.append(e.getValue().toString());
            comma = ", ";
        }

        sb.append("}]");
        return sb.toString();
    }

}
