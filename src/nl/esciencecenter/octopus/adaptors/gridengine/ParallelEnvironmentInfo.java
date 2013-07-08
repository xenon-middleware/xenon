package nl.esciencecenter.octopus.adaptors.gridengine;

class ParallelEnvironmentInfo {

    private final String name;
    private final int slots;
    private final String allocationRule;

    public ParallelEnvironmentInfo(String name, int slots, String allocationRule) {
        this.name = name;
        this.slots = slots;
        this.allocationRule = allocationRule;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public String getAllocationRule() {
        return allocationRule;
    }

    @Override
    public String toString() {
        return "ParallelEnvironmentInfo [name=" + name + ", slots=" + slots + ", allocationRule=" + allocationRule + "]";
    }
}