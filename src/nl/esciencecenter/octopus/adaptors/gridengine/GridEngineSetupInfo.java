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

import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * Holds some info on the specifics of the machine we are connected to, such as queues and parallel environments.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineSetupInfo {

    private final String[] queueNames;

    public GridEngineSetupInfo(SchedulerConnection schedulerConnection, GridEngineParser parser) throws OctopusIOException,
            OctopusException {

        String qstatOutput = schedulerConnection.runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        this.queueNames = allMap.keySet().toArray(new String[0]);

        //FETCH Queue info

        //Fetch Parallel environment info
    }

    public String[] getQueueNames() {
        return queueNames;
    }

}
