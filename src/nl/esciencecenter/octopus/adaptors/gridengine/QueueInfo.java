package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.Arrays;

import nl.esciencecenter.octopus.exceptions.OctopusException;

class QueueInfo {
    private final String name;
    private final int slots;
    private final String[] parallelEnvironments;

    QueueInfo(String name, int slots, String[] parallelEnvironments) throws OctopusException {
        this.name = name;
        this.slots = slots;
        this.parallelEnvironments = parallelEnvironments;
    }

    @Override
    public String toString() {
        return "QueueInfo [name=" + name + ", slots=" + slots + ", parallelEnvironments=" + Arrays.toString(parallelEnvironments)
                + "]";
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
}