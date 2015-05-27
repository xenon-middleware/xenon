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
package nl.esciencecenter.xenon.adaptors.torque;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds some info on the specifics of the machine we are connected to, such as queues and parallel environments.
 * 
 * @author Niels Drost
 * 
 */
public class TorqueSetup {

    static final Logger LOGGER = LoggerFactory.getLogger(TorqueSetup.class);

    private final String[] queueNames;

    private final Map<String, Map<String, String>> queues;

    private static String[] queryQueueNames(SchedulerConnection schedulerConnection) throws XenonException {
        String queueListOutput = schedulerConnection.runCheckedCommand(null, "qstat", "-Q");

        Map<String, Map<String, String>> accTable = ScriptingParser.parseTable(queueListOutput, "Queue",
                ScriptingParser.WHITESPACE_REGEX, TorqueAdaptor.ADAPTOR_NAME);
        return accTable.keySet().toArray(new String[accTable.size()]);
    }

    public TorqueSetup(TorqueSchedulerConnection schedulerConnection) throws XenonException {
        this.queueNames = queryQueueNames(schedulerConnection);
        this.queues = schedulerConnection.queryQueues(queueNames);

        LOGGER.debug("Created setup info, queues = {}", this.queues);
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
    TorqueSetup(String[] queueNames, Map<String, Map<String,String>> queues) {
        this.queueNames = queueNames.clone();
        this.queues = queues;
    }

    public String[] getQueueNames() {
        return queueNames.clone();
    }
    
    /**
     * Get the queue information for the current setup.
     * @return map of queue names with their info.
     */
    Map<String, Map<String,String>> getQueues() {
        return this.queues;
    }
}
