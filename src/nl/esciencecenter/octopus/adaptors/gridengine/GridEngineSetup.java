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

import java.util.ArrayList;
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.engine.util.CommandLineUtils;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds some info on the specifics of the machine we are connected to, such as queues and parallel environments.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineSetup {

    static final Logger logger = LoggerFactory.getLogger(GridEngineSetup.class);

    private final String[] queueNames;

    private final Map<String, QueueInfo> queues;

    private final Map<String, ParallelEnvironmentInfo> parallelEnvironments;

    public GridEngineSetup(SchedulerConnection schedulerConnection, GridEngineParser parser) throws OctopusIOException,
            OctopusException {

        String queueListOutput = schedulerConnection.runCheckedCommand(null, "qconf", "-sql");

        this.queueNames = parser.parseQconfList(queueListOutput);

        String queueDetailsOutput = schedulerConnection.runCheckedCommand(null, "qconf", "-sq", CommandLineUtils.asCSList(queueNames));

        this.queues = parser.parseQconfQueueInfo(queueDetailsOutput);

        String peListOutput = schedulerConnection.runCheckedCommand(null, "qconf", "-spl");

        String[] peNames = parser.parseQconfList(peListOutput);

        //build arguments for qconf to list parallel environments
        ArrayList<String> arguments = new ArrayList<String>();
        for (String name : peNames) {
            arguments.add("-sp");
            arguments.add(name);
        }

        String peDetailsOutput =
                schedulerConnection.runCheckedCommand(null, "qconf", arguments.toArray(new String[arguments.size()]));

        this.parallelEnvironments = parser.parseQconfParallelEnvironementInfo(peDetailsOutput);

        logger.debug("Created setup info, queues = {}, parallel environments = {}", queues, parallelEnvironments);
    }

    public String[] getQueueNames() {
        return queueNames;
    }

    /**
     * Get SGE to give us the required number of nodes. Since sge uses the rather abstract notion of slots, which number we need
     * to give is dependent on the parallel environment settings.
     */
    int calculateSlots(String parallelEnvironmentName, String queueName, int nodeCount) throws OctopusException {
        ParallelEnvironmentInfo environment = parallelEnvironments.get(parallelEnvironmentName);
        QueueInfo queue = queues.get(queueName);

        if (environment == null) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "requested parallel environment \""
                    + parallelEnvironmentName + "\" cannot be found at server");
        }

        if (queue == null) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "requested queue \"" + queueName
                    + "\" cannot be found at server");
        }

        String allocationRule = environment.getAllocationRule();

        logger.debug(
                "Calculating slots to get {} nodes in queue \"{}\" with parallel environment \"{}\" and allocation rule \"{}\"",
                nodeCount, queueName, parallelEnvironmentName, allocationRule);

        if (allocationRule == null) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME,
                    "Cannot determine allocation rule for parallel environment " + parallelEnvironmentName);
        } else if (allocationRule.equals("$pe_slots")) {
            if (nodeCount > 1) {
                throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "Parallel envrironment " + parallelEnvironmentName
                        + " only supports single node parallel jobs");
            }
            return 1;
        } else if (allocationRule.equals("$fill_up")) {
            //we need to request all slots of a node before we get a new node. The number of slots per node is listed in the
            //queue info.
            return nodeCount * queue.getSlots();
        } else if (environment.getAllocationRule().equals("$round_robin")) {
            //we should get a "new" node for each slot until no more slots remain.
            return nodeCount;
        } else {
            //the allocation rule should be an integer
            try {
                int processesPerHost = Integer.parseInt(allocationRule);

                //Multiply the number of nodes we require with the number of slots on each host.
                return nodeCount * processesPerHost;
            } catch (NumberFormatException e) {
                throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "Illegal allocation rule \"" + allocationRule
                        + "\" in parallel environment \"" + parallelEnvironmentName + "\"");
            }
        }

    }
}
