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

import nl.esciencecenter.octopus.adaptors.gridengine.ParallelEnvironmentInfo.AllocationRule;
import nl.esciencecenter.octopus.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.ScriptingParser;
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

    static final Logger LOGGER = LoggerFactory.getLogger(GridEngineSetup.class);

    private final String[] queueNames;

    private final Map<String, QueueInfo> queues;

    private final Map<String, ParallelEnvironmentInfo> parallelEnvironments;
    
    /**
     * generate arguments to list details of all parallel environments given
     * 
     * @param parallelEnvironmentNames
     *            names of parallel environments to list
     * @return a list of all qconf arguments needed to list all parallel environments
     */
    static String[] qconfPeDetailsArguments(String[] parallelEnvironmentNames) {
        String[] result = new String[parallelEnvironmentNames.length * 2];
        for (int i = 0; i < parallelEnvironmentNames.length; i++) {
            result[2 * i] = "-sp";
            result[(2 * i) + 1] = parallelEnvironmentNames[i];
        }
        return result;
    }
    
    private static String[] getQueueNames(SchedulerConnection schedulerConnection) throws OctopusIOException, OctopusException {
        String queueListOutput = schedulerConnection.runCheckedCommand(null, "qconf", "-sql");

        return ScriptingParser.parseList(queueListOutput);
    }

    private static Map<String, QueueInfo> getQueues(String[] queueNames, SchedulerConnection schedulerConnection)
            throws OctopusIOException, OctopusException {
        String output = schedulerConnection.runCheckedCommand(null, "qconf", "-sq", CommandLineUtils.asCSList(queueNames));

        Map<String, Map<String, String>> maps = ScriptingParser.parseKeyValueRecords(output, "qname",

        ScriptingParser.WHITESPACE_REGEX, GridEngineAdaptor.ADAPTOR_NAME);

        Map<String, QueueInfo> result = new HashMap<String, QueueInfo>();

        for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) {
            result.put(entry.getKey(), new QueueInfo(entry.getValue()));
        }

        return result;
    }

    public GridEngineSetup(SchedulerConnection schedulerConnection) throws OctopusIOException,
            OctopusException {

        this.queueNames = getQueueNames(schedulerConnection);

        this.queues = getQueues(queueNames, schedulerConnection);

        this.parallelEnvironments = getParallelEnvironments(schedulerConnection);

        LOGGER.debug("Created setup info, queues = {}, parallel environments = {}", this.queues, this.parallelEnvironments);
    }
    
    private static Map<String, ParallelEnvironmentInfo> getParallelEnvironments(SchedulerConnection schedulerConnection)
            throws OctopusIOException, OctopusException {
        //first retrieve a list of parallel environments
        RemoteCommandRunner runner = schedulerConnection.runCommand(null, "qconf", "-spl");

        //qconf returns an error if there are no parallel environments
        if (runner.getExitCode() == 1 && runner.getStderr().contains("no parallel environment defined")) {
            return new HashMap<String, ParallelEnvironmentInfo>();
        }

        if (!runner.success()) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "Could not get parallel environment info from scheduler: "
                    + runner);
        }

        String[] parallelEnvironmentNames = ScriptingParser.parseList(runner.getStdout());

        //then get the details of each parallel environment
        String peDetailsOutput = schedulerConnection.runCheckedCommand(null, "qconf",
                qconfPeDetailsArguments(parallelEnvironmentNames));

        Map<String, Map<String, String>> maps = ScriptingParser.parseKeyValueRecords(peDetailsOutput, "pe_name",
                ScriptingParser.WHITESPACE_REGEX, GridEngineAdaptor.ADAPTOR_NAME);

        Map<String, ParallelEnvironmentInfo> result = new HashMap<String, ParallelEnvironmentInfo>();

        for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) {
            result.put(entry.getKey(), new ParallelEnvironmentInfo(entry.getValue()));
        }

        return result;
    }

    /**
     * Testing constructor.
     * 
     * @param queueNames
     *            queue names to use.
     * @param queues
     *            queues to use.
     * @param parallelEnvironments
     *            parallel environments to use.
     */
    GridEngineSetup(String[] queueNames, Map<String, QueueInfo> queues, Map<String, ParallelEnvironmentInfo> parallelEnvironments) {
        this.queueNames = queueNames;
        this.queues = queues;
        this.parallelEnvironments = parallelEnvironments;
    }

    public String[] getQueueNames() {
        return queueNames.clone();
    }

    /**
     * Get SGE to give us the required number of nodes. Since sge uses the rather abstract notion of slots, the number we need to
     * give is dependent on the parallel environment settings.
     */
    int calculateSlots(String parallelEnvironmentName, String queueName, int nodeCount) throws OctopusException {
        ParallelEnvironmentInfo pe = parallelEnvironments.get(parallelEnvironmentName);
        QueueInfo queue = queues.get(queueName);

        if (pe == null) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "requested parallel environment \""
                    + parallelEnvironmentName + "\" cannot be found at server");
        }

        if (queue == null) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "requested queue \"" + queueName
                    + "\" cannot be found at server");
        }

        LOGGER.debug("Calculating slots to get {} nodes in queue \"{}\" with parallel environment \"{}\""
                + " and allocation rule \"{}\" with ppn {}", nodeCount, queueName, parallelEnvironmentName,
                pe.getAllocationRule(), pe.getPpn());

        AllocationRule allocationRule = pe.getAllocationRule();
        if (allocationRule == AllocationRule.PE_SLOTS) {
            if (nodeCount > 1) {
                throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "Parallel environment " + parallelEnvironmentName
                        + " only supports single node parallel jobs");
            }
            return 1;
        } else if (allocationRule == AllocationRule.FILL_UP) {
            //we need to request all slots of a node before we get a new node. The number of slots per node is listed in the
            //queue info.
            return nodeCount * queue.getSlots();
        } else if (allocationRule == AllocationRule.ROUND_ROBIN) {
            //we should get a "new" node for each slot until no more slots remain.
            return nodeCount;
        } else if (allocationRule == AllocationRule.INTEGER) {
            //Multiply the number of nodes we require with the number of slots on each host.
            return nodeCount * pe.getPpn();
        } else {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "unknown pe allocation rule: " + pe.getAllocationRule());
        }
    }
}
