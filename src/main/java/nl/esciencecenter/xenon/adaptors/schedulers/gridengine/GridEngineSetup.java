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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.ADAPTOR_NAME;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;

/**
 * Holds some info on the specifics of the machine we are connected to, such as queues and parallel environments.
 *
 */
public class GridEngineSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineSetup.class);

    private final String[] queueNames;

    private final Map<String, QueueInfo> queues;

    private final Map<String, ParallelEnvironmentInfo> parallelEnvironments;

    private final int defaultRuntime;

    /**
     * generate arguments to list details of all parallel environments given
     *
     * @param parallelEnvironmentNames
     *            names of parallel environments to list
     * @return a list of all qconf arguments needed to list all parallel environments
     */
    protected static String[] qconfPeDetailsArguments(String[] parallelEnvironmentNames) {
        String[] result = new String[parallelEnvironmentNames.length * 2];
        for (int i = 0; i < parallelEnvironmentNames.length; i++) {
            result[2 * i] = "-sp";
            result[(2 * i) + 1] = parallelEnvironmentNames[i];
        }
        return result;
    }

    private static String[] getQueueNames(GridEngineScheduler scheduler) throws XenonException {
        String queueListOutput = scheduler.runCheckedCommand(null, "qconf", "-sql");

        return ScriptingParser.parseList(queueListOutput);
    }

    private static Map<String, QueueInfo> getQueues(String[] queueNames, GridEngineScheduler scheduler) throws XenonException {
        String output = scheduler.runCheckedCommand(null, "qconf", "-sq", ScriptingUtils.asCSList(queueNames));

        Map<String, Map<String, String>> maps = ScriptingParser.parseKeyValueRecords(output, "qname", ScriptingParser.WHITESPACE_REGEX, ADAPTOR_NAME);

        Map<String, QueueInfo> result = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) {
            result.put(entry.getKey(), new QueueInfo(entry.getValue()));
        }

        return result;
    }

    public GridEngineSetup(GridEngineScheduler scheduler) throws XenonException {

        this.queueNames = getQueueNames(scheduler);

        this.queues = getQueues(queueNames, scheduler);

        this.parallelEnvironments = getParallelEnvironments(scheduler);

        this.defaultRuntime = scheduler.getDefaultRuntime();

        LOGGER.debug("Created setup info, queues = {}, parallel environments = {}", this.queues, this.parallelEnvironments);
    }

    private static Map<String, ParallelEnvironmentInfo> getParallelEnvironments(GridEngineScheduler scheduler) throws XenonException {
        // first retrieve a list of parallel environments
        RemoteCommandRunner runner = scheduler.runCommand(null, "qconf", "-spl");

        // qconf returns an error if there are no parallel environments
        if (runner.getExitCode() == 1 && runner.getStderr().contains("no parallel environment defined")) {
            return new HashMap<>();
        }

        if (!runner.success()) {
            throw new XenonException(ADAPTOR_NAME, "Could not get parallel environment info from scheduler: " + runner);
        }

        String[] parallelEnvironmentNames = ScriptingParser.parseList(runner.getStdout());

        // then get the details of each parallel environment
        String peDetailsOutput = scheduler.runCheckedCommand(null, "qconf", qconfPeDetailsArguments(parallelEnvironmentNames));

        Map<String, Map<String, String>> maps = ScriptingParser.parseKeyValueRecords(peDetailsOutput, "pe_name", ScriptingParser.WHITESPACE_REGEX,
                ADAPTOR_NAME);

        Map<String, ParallelEnvironmentInfo> result = new HashMap<>();

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
     * @param defaultRuntime
     *            the default runtime to use.
     */
    GridEngineSetup(String[] queueNames, Map<String, QueueInfo> queues, Map<String, ParallelEnvironmentInfo> parallelEnvironments, int defaultRuntime) {
        this.queueNames = queueNames.clone();
        this.queues = queues;
        this.parallelEnvironments = parallelEnvironments;
        this.defaultRuntime = defaultRuntime;
    }

    public String[] getQueueNames() {
        return queueNames.clone();
    }

    public int getDefaultRuntime() {
        return defaultRuntime;
    }

    /**
     * Try to find a parallel environment that can be used to get a number of cores on a single node
     *
     * @param coresPerNode number of cores to reserve on a node
     * @param queueName Name of the queue
     * @return optional parallel environment
     */
    Optional<ParallelEnvironmentInfo> getSingleNodeParallelEnvironment(int coresPerNode, String queueName) {
        Stream<ParallelEnvironmentInfo> stream = this.parallelEnvironments.values().stream()
            .filter(pe -> pe.canAllocateSingleNode(coresPerNode));
        // Filter pe on queue
        QueueInfo queue = queues.get(queueName);
        if (queue == null) {
            Set<String> pesOfQueues = new HashSet<>();
            for (QueueInfo q : queues.values()) {
                pesOfQueues.addAll(Arrays.asList(q.getParallelEnvironments()));
            }
            stream = stream.filter(pe -> pesOfQueues.contains(pe.getName()));
        } else {
            // don't know which queue the scheduler will pick, make sure atleast one queue has the candidate pe
            Set<String> pesOfQueue = new HashSet<>(Arrays.asList(queue.getParallelEnvironments()));
            stream = stream.filter(pe -> pesOfQueue.contains(pe.getName()));
        }
        Optional<ParallelEnvironmentInfo> r = stream.findFirst();
        LOGGER.debug("Gridengine choose to use following pe: " + r.toString());
        return r;
    }

    /**
     * Try to find a parallel environment that can be used to get X number of cores per node on Y number of nodes
     *
     * @param coresPerNode number of cores to reserve on each node
     * @param nodes number of nodes to reserve
     * @param queueName Name of the queue
     * @return optional parallel environment
     */
    Optional<ParallelEnvironmentInfo> getMultiNodeParallelEnvironment(int coresPerNode, int nodes, String queueName) {
        return this.parallelEnvironments.values().stream().filter(s -> s.canAllocateMultiNode(coresPerNode, nodes)
        ).findFirst();
    }
}
