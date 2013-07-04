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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;

public class GridengineAdaptor extends Adaptor {

    public static final String ADAPTOR_NAME = "gridengine";

    private static final String ADAPTOR_DESCRIPTION =
            "The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local "
                    + "or the ssh adaptor to gain access to the scheduler machine.";

    public static final String[] ADAPTOR_SCHEMES = new String[] { "ge", "sge" };

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. No properties exist for this adaptor. */
    private static final String[][] validPropertiesList = new String[0][0];

    private final GridEngineJobs jobsAdaptor;

    private final GridEngineCredentials credentialsAdaptor;

    public GridengineAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEMES, validPropertiesList, properties);

        this.jobsAdaptor = new GridEngineJobs(getProperties(), octopusEngine);
        this.credentialsAdaptor = new GridEngineCredentials(octopusEngine);
    }

    public GridEngineJobs jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public void end() {
        jobsAdaptor.end();
    }

    @Override
    public Files filesAdaptor() throws OctopusException {
        throw new OctopusException(ADAPTOR_NAME, "Adaptor does not support files.");
    }

    @Override
    public Credentials credentialsAdaptor() throws OctopusException {
        return credentialsAdaptor;
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return new HashMap<String, String>();
    }

}
