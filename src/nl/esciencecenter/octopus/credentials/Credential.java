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
package nl.esciencecenter.octopus.credentials;

import java.util.Map;

/**
 * Credential represents a user credential uses to gain access to a resource.
 * 
 * @author Rob van Nieuwpoort <R.vanNieuwpoort@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Credential {

    /**
     * Get the name of the adaptor attached to this Credential.
     * 
     * @return the name of the adaptor.
     */
    String getAdaptorName();

    /**
     * Get the properties used to create this Credential.
     * 
     * @return the properties used to create this Credential.
     */
    Map<String,String> getProperties();
}
