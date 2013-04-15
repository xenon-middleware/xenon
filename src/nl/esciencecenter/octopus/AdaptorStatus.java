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

import java.util.Map;

/**
 * AdaptorStatus contains status information of a specific adaptors.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface AdaptorStatus {

    /**
     * Get the supported schemes for this adaptor.
     * 
     * @return the schemes supported by this adaptor.
     */
    public String[] getSupportedSchemes();

    /**
     * Get the name of the adaptor.
     * 
     * @return the name of the adaptor.
     */
    public String getName();

    /**
     * Get the description of the adaptor.
     * 
     * @return the description of the adaptor.
     */
    public String getDescription();

    /**
     * Returns a map containing supported properties and a short description for each.
     * 
     * @return a map containing supported properties and a short description for each.
     */
    public Map<String, String> getSupportedProperties();

    /**
     * Returns a map containing scheduler specific status information.
     * 
     * @return a map containing scheduler specific status information.
     */
    public Map<String, String> getAdaptorSpecificInformation();
}