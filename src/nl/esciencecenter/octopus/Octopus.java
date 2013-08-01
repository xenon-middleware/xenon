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

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

/**
 * Main interface to Octopus.
 * 
 * Provides an access point to all packages of Octopus and several utility functions.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Octopus {

    /**
     * Returns the properties that where used to create this Octopus.
     * 
     * @return the properties used to create this Octopus.
     */
    Map<String,String> getProperties();

    /**
     * Returns information about the specified adaptor.
     * 
     * @param adaptorName
     *            the adaptor for which to return the information.
     * @return an AdaptorInfo containing information about the specified adaptor.
     * @throws OctopusException
     *             when the adaptor does not exist, or no information could be retrieved.
     */
    AdaptorStatus getAdaptorStatus(String adaptorName) throws OctopusException;

    /**
     * Returns information on all adaptors available to this Octopus.
     * 
     * @return information on all adaptors.
     */
    AdaptorStatus[] getAdaptorStatuses();

    /**
     * Get a reference to the Files package interface.
     * 
     * @return a reference to the Files interface.
     */
    Files files();

    /**
     * Get a reference to the Jobs package interface.
     * 
     * @return a reference to the Files package interface.
     */
    Jobs jobs();

    /**
     * Get a reference to the Credentials package interface.
     * 
     * @return a reference to the Credentials package interface.
     */
    Credentials credentials();

    //Future extension: clouds
    //public Clouds clouds();

    //Future extension: bandwidth on demand
    //public Networks networks();

    //public ??

}
