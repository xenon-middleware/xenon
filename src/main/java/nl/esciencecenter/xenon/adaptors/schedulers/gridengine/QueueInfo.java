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

        // The slotsValue that is returned my contain multiple values, for example:
        //
        // 12,[@cores_36=36],[@cores_12=12],[@cores_48=48], [@cores_24=24],[@cores_40=40]
        //
        // This means that 12 slots is default, but for specific processor groups (like cores_36 or cores_24) they may be different.
        // For now, we educate the parser to accept this string, but only use the number in the list.

        String[] split = slotsValue.split(",");

        if (split.length == 0) {
            throw new XenonException(ADAPTOR_NAME, "Cannot parse slots for queue \"" + name + "\", got \"" + slotsValue + "\"");
        }

        for (int i = 0; i < split.length; i++) {
            if (!split[i].contains("@")) {
                slotsValue = split[i];
                break;
            }
        }

        try {
            slots = Integer.parseInt(slotsValue);
        } catch (NumberFormatException e) {
            throw new XenonException(ADAPTOR_NAME, "Cannot parse slots for queue \"" + name + "\", got \"" + slotsValue + "\"", e);
        }

        String peValue = info.get("pe_list");

        if (peValue == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find parallel environments for queue \"" + name + "\"");
        }
        // The pe_list can like the slots contain multiple values, for example:
        //
        // mpi threaded,[n0061.compute.hpc=NONE]
        //
        // This means that all hosts except n0061.compute.hpc can run mpi and threaded jobs.
        // For now, we educate the parser to accept this string, but only use the first value before the comma
        String[] peValue2 = peValue.split(",");
        parallelEnvironments = ScriptingParser.WHITESPACE_REGEX.split(peValue2[0]);
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
        return "QueueInfo [name=" + name + ", slots=" + slots + ", parallelEnvironments=" + Arrays.toString(parallelEnvironments) + "]";
    }
}
