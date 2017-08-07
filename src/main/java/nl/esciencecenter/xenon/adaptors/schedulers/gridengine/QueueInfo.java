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
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser;

/**
 * Class that holds some info on queues used in Grid Engine.
 *
 *
 */
class QueueInfo {
    private final String name;
    private final int slots;
    private final String[] parallelEnvironments;

    QueueInfo(Map<String, String> info) throws XenonException {
        name = info.get("qname");

        if (name == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find name of queue in output");
        }

        String slotsValue = info.get("slots");

        if (slotsValue == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find slots for queue \"" + name + "\"");
        }

        try {
            slots = Integer.parseInt(slotsValue);
        } catch (NumberFormatException e) {
            throw new XenonException(ADAPTOR_NAME, "Cannot parse slots for queue \"" + name + "\", got \""
                    + slotsValue + "\"", e);
        }

        String peValue = info.get("pe_list");

        if (peValue == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find parallel environments for queue \"" + name + "\"");
        }
        parallelEnvironments = ScriptingParser.WHITESPACE_REGEX.split(peValue);
    }

    /**
     * Testing constructor
     *
     * @param name
     *            name of the queue
     * @param slots
     *            number of slots
     * @param parallelEnvironments
     *            parallel environment names
     */
    QueueInfo(String name, int slots, String... parallelEnvironments) {
        this.name = name;
        this.slots = slots;
        this.parallelEnvironments = parallelEnvironments;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public String[] getParallelEnvironments() {
        return parallelEnvironments;
    }

    @Override
    public String toString() {
        return "QueueInfo [name=" + name + ", slots=" + slots + ", parallelEnvironments=" + Arrays.toString(parallelEnvironments)
                + "]";
    }
}
