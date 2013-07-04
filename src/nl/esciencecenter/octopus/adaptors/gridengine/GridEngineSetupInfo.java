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

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * Holds some info on the specifics of the machine we are connected to, such as queues and parallel environments.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineSetupInfo {

    private static class PEInfo {
        String name;
    }

    private static class QueueInfo {
        final String name;
        final int slots;
        final String[] parallelEnvironments;

        QueueInfo(String name, String slots, String parallelEnvironments) throws OctopusException {
            this.name = name;

            if (slots == null) {
                throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "Cannot find slots for queue " + name);
            }

            try {
                this.slots = Integer.parseInt(slots);
            } catch (NumberFormatException e) {
                throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "Cannot parse slots for queue " + name + ", got "
                        + slots, e);
            }

            if (parallelEnvironments == null) {
                throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "Cannot find slots for queue " + name);
            }
            this.parallelEnvironments = parallelEnvironments.split("\\s+");

            logger.debug("found queue details {} slots {} pe {}", this.name, this.slots, this.parallelEnvironments);
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(GridEngineSetupInfo.class);

    private final String[] queueNames;

    //private final QueueInfo[] queues;

    //    private final PEInfo parallelEnvironments;

    public GridEngineSetupInfo(SchedulerConnection schedulerConnection, GridEngineParser parser) throws OctopusIOException,
            OctopusException {

        String listOutput = schedulerConnection.runCommand(null, "qconf", "-sql");

        this.queueNames = parser.parseQconfQueueList(listOutput);

        //this.queues = fetchQueueInfo(queueNames, schedulerConnection, parser);

        //FETCH Queue info

        //Fetch Parallel environment info
    }

    public String[] getQueueNames() {
        return queueNames;
    }

    private static QueueInfo[] fetchQueueInfo(String[] queueNames, SchedulerConnection schedulerConnection,
            GridEngineParser parser) throws OctopusIOException, OctopusException {
        QueueInfo[] result = new QueueInfo[queueNames.length];

        logger.debug("queue names: {}", Arrays.asList(queueNames));

        String queueList = null;
        for (String queueName : queueNames) {
            if (queueList == null) {
                queueList = queueName;
            } else {
                queueList += "," + queueName;
            }
        }

        String detailsOutput = schedulerConnection.runCommand(null, "qconf", "-sq", queueList);

        Map<String, Map<String, String>> allQueueDetails = parser.parseQconfQueueInfo(detailsOutput);

        for (int i = 0; i < queueNames.length; i++) {
            Map<String, String> queueDetails = allQueueDetails.get(queueNames[i]);

            if (queueDetails == null) {
                throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get queue details for queue " + queueNames[i]);
            }

            result[i] = new QueueInfo(queueNames[i], queueDetails.get("slots"), queueDetails.get("pe_list"));
        }

        return result;
    }

}
