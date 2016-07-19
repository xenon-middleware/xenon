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

package nl.esciencecenter.xenon.clouds;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class VirtualMachineDescription {

    /** The image to start */
    private String imageID;
    
    /** The region to start the image in */
    private String region;
    
    /** The node type to start the image on */
    private String nodeType;
    
    /** The options of this virtual machine */
    private final Map<String, String> vmOptions = new HashMap<>(5);

    /** The number of nodes to start */
    private int nodeCount;
    
    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    /**
     * Get an unmodifiable copy of the options of this VirtualMachineDescription.
     * 
     * The options consist of a {@link Map} of options variables with their values.
     * 
     * @return an unmodifiable copy of the options of the VirtualMachineDescription.
     */
    public Map<String, String> getVirtualMachineOptions() {
        return Collections.unmodifiableMap(vmOptions);
    }

    /**
     * Sets the options of the VirtualMachineDescription.
     * 
     * The options consist of a {@link Map} of options variables with their values.
     * 
     * @param options
     *            options of the VirtualMachineDescription.
     */
    public void setVirtualMachineOptions(Map<String, String> options) {

        vmOptions.clear();

        if (options != null) {
            for (Entry<String, String> entry : options.entrySet()) {
                addVirtualMachineOption(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Add an option to the VirtualMachineDescription
     * 
     * The options consist of a key-value pairs.
     * 
     * Neither the key or value of an option may be <code>null</code> or empty.
     * 
     * @param key
     *            the unique key under which to store the option.
     * @param value
     *            the value of the option to store.
     */
    public void addVirtualMachineOption(String key, String value) {

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("VirtualMachine option key may not be null or empty!");
        }

        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("VirtualMachine option value may not be null or empty!");
        }

        vmOptions.put(key, value);
    }
    
    
    
    
}
