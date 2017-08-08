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

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;

/**
 * Class that holds some info on parallel environments used in Grid Engine.
 *
 */
class ParallelEnvironmentInfo {

    public enum AllocationRule {
        INTEGER, PE_SLOTS, ROUND_ROBIN, FILL_UP
    }

    private static final String ALLOCATION_PE_SLOTS = "$pe_slots";
    private static final String ALLOCATION_ROUND_ROBIN = "$round_robin";
    private static final String ALLOCATION_FILL_UP = "$fill_up";

    private final String name;
    private final int slots;
    private final AllocationRule allocationRule;
    private final int ppn;

    ParallelEnvironmentInfo(Map<String, String> info) throws XenonException {
        name = info.get("pe_name");

        if (name == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find name for parallel environment");
        }

        String slotsValue = info.get("slots");

        if (slotsValue == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find slots for parallel environment " + name);
        }

        try {
            slots = Integer.parseInt(slotsValue);
        } catch (NumberFormatException e) {
            throw new XenonException(ADAPTOR_NAME, "Cannot parse slots for parallel environment " + name
                    + ", got " + slotsValue, e);
        }

        String allocationValue = info.get("allocation_rule");

        if (allocationValue == null) {
            throw new XenonException(ADAPTOR_NAME, "Cannot find allocation rule for parallel environment "
                    + name);
        } else if (allocationValue.equals(ALLOCATION_PE_SLOTS)) {
            allocationRule = AllocationRule.PE_SLOTS;
            ppn = 0;
        } else if (allocationValue.equals(ALLOCATION_ROUND_ROBIN)) {
            allocationRule = AllocationRule.ROUND_ROBIN;
            ppn = 0;
        } else if (allocationValue.equals(ALLOCATION_FILL_UP)) {
            allocationRule = AllocationRule.FILL_UP;
            ppn = 0;
        } else {
            allocationRule = AllocationRule.INTEGER;
            try {
                ppn = Integer.parseInt(allocationValue);
            } catch (NumberFormatException e) {
                throw new XenonException(ADAPTOR_NAME, "Cannot parse allocation for parallel environment \""
                        + name + "\", expected a number, got \"" + allocationValue + "\"", e);
            }
        }
    }

    /*
     * Testing constructor.
     */
    ParallelEnvironmentInfo(String name, int slots, AllocationRule allocationRule, int ppn) {
        this.name = name;
        this.slots = slots;
        this.allocationRule = allocationRule;
        this.ppn = ppn;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public AllocationRule getAllocationRule() {
        return allocationRule;
    }

    public int getPpn() {
        return ppn;
    }

    @Override
    public String toString() {
        return "ParallelEnvironmentInfo [name=" + name + ", slots=" + slots + ", allocationRule=" + allocationRule + ", ppn="
                + ppn + "]";
    }
}
