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

import java.util.Map;

/**
 * @version 2.0
 * @since 2.0
 *
 */
public interface Cloud {

    /**
     * Get the name of the adaptor that created this Cloud.
     * 
     * @return the name of the adaptor.
     */
    String getAdaptorName();

    /**
     * Get the location of this Cloud.
     * 
     * @return the location of the Cloud.
     */
    String getLocation();

    /**
     * Get the scheme used to access the Cloud.
     * 
     * @return the scheme used to access the Cloud.
     */
    String getScheme();

    /**
     * Get the properties used to create this Cloud.
     * 
     * @return the properties used to create this Cloud.
     */
    Map<String, String> getProperties();
    
    
    
    
}
