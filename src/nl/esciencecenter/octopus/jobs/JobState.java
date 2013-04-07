package nl.esciencecenter.octopus.jobs;

/**
 * An instance of this enumeration indicates the state of a {@link Job}.
 * 
 */
public enum JobState {
    /**
     * Initial state indicator.
     * 
     * The {@link Job} has been constructed.
     */
    INITIAL,
    /**
     * The input files of the {@link Job} are being pre staged.
     */
    PRE_STAGING,
    /**
     * Scheduled state indicator.
     * 
     * The {@link Job} has been submitted to a scheduler and is scheduled to be
     * executed.
     */
    SCHEDULED,
    /**
     * Running state indicator.
     * 
     * The {@link Job} is executing.
     */
    RUNNING,
    /**
     * The output files of the {@link Job} are being post staged.
     */
    POST_STAGING,
    /**
     * Stopped state indicator.
     * 
     * The {@link Job} has properly run. All the cleanup and administration of
     * the {@link Job} is completely done.
     */
    DONE,
    /**
     * Error state indicator.
     * 
     * The {@link Job} hasn't properly run.
     */
    ERROR,

    /**
     * The {@link Job} state is unknown for some reason. It might be a network
     * problem.
     */
    UNKNOWN
}