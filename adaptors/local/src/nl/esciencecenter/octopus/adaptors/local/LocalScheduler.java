package nl.esciencecenter.octopus.adaptors.local;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class LocalScheduler {

    private final Scheduler scheduler;
    
    private final LinkedList<LocalJobExecutor> singleQ;

    private final LinkedList<LocalJobExecutor> multiQ;

    private final LinkedList<LocalJobExecutor> unlimitedQ;

    private final ExecutorService singleExecutor;

    private final ExecutorService multiExecutor;

    private final ExecutorService unlimitedExecutor;

    private final int maxQSize;

    private static int jobID = 0;

    private static synchronized int getNextJobID() {
        return jobID++;
    }

    protected LocalScheduler(Scheduler scheduler) throws OctopusException {

        this.scheduler = scheduler;
        
        singleQ = new LinkedList<LocalJobExecutor>();
        multiQ = new LinkedList<LocalJobExecutor>();
        unlimitedQ = new LinkedList<LocalJobExecutor>();

        unlimitedExecutor = Executors.newCachedThreadPool();
        singleExecutor = Executors.newSingleThreadExecutor();

        int processors = Runtime.getRuntime().availableProcessors();
        
        OctopusProperties properties = scheduler.getProperties();
        
        int multiQThreads = properties.getIntProperty(LocalAdaptor.MULTIQ_MAX_CONCURRENT, processors);
        multiExecutor = Executors.newFixedThreadPool(multiQThreads);

        maxQSize = properties.getIntProperty(LocalAdaptor.MAX_HISTORY);

        if (maxQSize < 0 && maxQSize != -1) {
            throw new BadParameterException("max q size cannot be negative (excluding -1 for unlimited)", "local", null);
        }
    }
    
    public Scheduler getScheduler() { 
        return scheduler;
    }

    protected String[] getQueueNames() throws OctopusException {
        return new String[] { "single", "multi", "unlimited" };
    }

    protected synchronized Job[] getJobs(String queueName) throws OctopusException {

        if (queueName == null || queueName.equals("single")) {
            return getJobs(singleQ.toArray(new LocalJobExecutor[0]));
        } else if (queueName.equals("multi")) {
            return getJobs(multiQ.toArray(new LocalJobExecutor[0]));
        } else if (queueName.equals("unlimited")) {
            return getJobs(unlimitedQ.toArray(new LocalJobExecutor[0]));
        } else {
            throw new BadParameterException("queue \"" + queueName + "\" does not exist", "local", null);
        }
    }

    private Job[] getJobs(LocalJobExecutor[] executors) {

        LocalJobExecutor[] tmp = singleQ.toArray(new LocalJobExecutor[0]);

        Job[] result = new Job[tmp.length];

        for (int i = 0; i < tmp.length; i++) {
            result[i] = tmp[i].getJob();
        }

        return result;
    }

    //remove finished jobs from a q until the maximum number of jobs limit is met.
    private synchronized void purgeQ(LinkedList<LocalJobExecutor> q) {
        if (maxQSize == -1) {
            return;
        }

        //how many jobs do we need to remove
        int purgeCount = q.size() - maxQSize;

        if (purgeCount <= 0) {
            return;
        }

        Iterator<LocalJobExecutor> iterator = q.iterator();

        while (iterator.hasNext() && purgeCount > 0) {
            if (iterator.next().isDone()) {
                iterator.remove();
                purgeCount--;
            }
        }
    }

    protected Job submitJob(JobDescription description) throws OctopusException {

        Job result = new JobImplementation(description, scheduler, "localjob-" + getNextJobID());

        LocalJobExecutor executor = new LocalJobExecutor(result);

        String queueName = description.getQueueName();

        if (queueName == null || queueName.equals("single")) {
            singleQ.add(executor);
            singleExecutor.execute(executor);
        } else if (queueName.equals("multi")) {
            multiQ.add(executor);
            multiExecutor.execute(executor);
        } else if (queueName.equals("unlimited")) {
            unlimitedQ.add(executor);
            unlimitedExecutor.execute(executor);
        } else {
            throw new BadParameterException("queue \"" + queueName + "\" does not exist", "local", null);
        }

        //purge jobs from q if needed (will not actually cancel execution of jobs)
        purgeQ(singleQ);
        purgeQ(multiQ);
        purgeQ(unlimitedQ);

        return result;
    }

    private LinkedList<LocalJobExecutor> findQueue(String queueName) throws OctopusException {

        if (queueName == null || queueName.equals("single")) {
            return singleQ;
        } else if (queueName.equals("multi")) {
            return multiQ;
        } else if (queueName.equals("unlimited")) {
            return unlimitedQ;
        } else {
            throw new OctopusException("queue \"" + queueName + "\" does not exist", "local", null);
        }
    }

    private LocalJobExecutor findJob(LinkedList<LocalJobExecutor> queue, Job job) throws OctopusException {

        for (LocalJobExecutor e : queue) {
            if (e.getJob().equals(job)) {
                return e;
            }
        }

        throw new OctopusException("local", "Job not found: " + job.getIdentifier());
    }

    public void cancelJob(Job job) throws OctopusException {

        if (job.getScheduler() != this) {
            throw new OctopusException("Cannot cancel jobs descriptions from other scheduler!", 
                    scheduler.getAdaptorName() + "/" + scheduler.getUniqueID());
        }

        // FIXME: What if job is already gone?
        LinkedList<LocalJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
        findJob(tmp, job).kill();
    }

    public JobStatus getJobStatus(Job job) throws OctopusException {
        
        if (job.getScheduler() != this) {
            throw new OctopusException("Cannot cancel jobs descriptions from other scheduler!", 
                    scheduler.getAdaptorName() + "/" + scheduler.getUniqueID());
        }
        
        LinkedList<LocalJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
        LocalJobExecutor executor = findJob(tmp, job);
        return executor.getStatus();
    }

    public JobStatus[] getJobStatuses(Job... jobs) {
        
        JobStatus[] result = new JobStatus[jobs.length];

        for (int i=0;i<jobs.length;i++) { 
            try {
                result[i] = getJobStatus(jobs[i]);
            } catch (OctopusException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, null);
            }
        }
        
        return result;
    }

    public void end() {
        singleExecutor.shutdownNow();
        multiExecutor.shutdownNow();
        unlimitedExecutor.shutdownNow();
    }
}
