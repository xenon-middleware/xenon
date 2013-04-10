package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobs implements JobsAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(LocalJobs.class);

    private final OctopusEngine octopusEngine;

    private final LinkedList<LocalJobExecutor> singleQ;

    private final LinkedList<LocalJobExecutor> multiQ;

    private final LinkedList<LocalJobExecutor> unlimitedQ;

    private final ExecutorService singleExecutor;

    private final ExecutorService multiExecutor;

    private final ExecutorService unlimitedExecutor;

    private final int maxQSize;

    private final LocalAdaptor localAdaptor;

    private static int jobID = 0;

    private static synchronized int getNextJobID() {
        return jobID++;
    }

    public LocalJobs(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {

        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;

        singleQ = new LinkedList<LocalJobExecutor>();
        multiQ = new LinkedList<LocalJobExecutor>();
        unlimitedQ = new LinkedList<LocalJobExecutor>();

        unlimitedExecutor = Executors.newCachedThreadPool();
        singleExecutor = Executors.newSingleThreadExecutor();

        int processors = Runtime.getRuntime().availableProcessors();
        int multiQThreads = properties.getIntProperty(LocalAdaptor.MULTIQ_MAX_CONCURRENT, processors);
        multiExecutor = Executors.newFixedThreadPool(multiQThreads);

        maxQSize = properties.getIntProperty(LocalAdaptor.MAX_HISTORY);

        if (maxQSize < 0 && maxQSize != -1) {
            throw new BadParameterException("max q size cannot be negative (excluding -1 for unlimited)", "local", null);
        }
    }

    @Override
    public Scheduler newScheduler(OctopusProperties properties, URI location) throws OctopusException {

        localAdaptor.checkURI(location);

        if (location.getPath() != null && location.getPath().length() > 0) {
            throw new OctopusException("local", "Non-empty path in a local scheduler URI is not allowed");
        }

        // FIXME: This simply returns a new SchedulerImplementation, but ignores properties and credentials completely.    	
        return new SchedulerImplementation(location, properties, "local");
    }

    public void end() {
        singleExecutor.shutdownNow();
        multiExecutor.shutdownNow();
        unlimitedExecutor.shutdownNow();
    }

    @Override
    public String[] getQueueNames(Scheduler scheduler) throws OctopusException {
        return new String[] { "single", "multi", "unlimited" };
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

    private Job[] getJobs(LocalJobExecutor[] executors) {

        LocalJobExecutor[] tmp = singleQ.toArray(new LocalJobExecutor[0]);

        Job[] result = new Job[tmp.length];

        for (int i = 0; i < tmp.length; i++) {
            result[i] = tmp[i].getJob();
        }

        return result;
    }

    @Override
    public synchronized Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException {

        
        
        
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
        
    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        
        Scheduler s = job.getScheduler();
        
        if (!s.getAdaptorName().equals(localAdaptor.getName())) { 
            throw new OctopusException("local", "Job was not started by adaptor " + localAdaptor.getName());
        }
        
        LinkedList<LocalJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
        LocalJobExecutor executor = findJob(tmp, job);
        return executor.getStatus();
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusException {
        
        JobStatus[] result = new JobStatus[jobs.length];

        for (int i=0;i<jobs.length;i++) { 
            result[i] = getJobStatus(jobs[i]);
        }
        
        return result;
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {   
        // FIXME: What if job is already gone?
        LinkedList<LocalJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
        findJob(tmp, job).kill();
    }        

    @Override
    public void cancelJobs(Job... jobs) throws OctopusException {
        // FIXME: What if job is already gone?
        for (Job j : jobs) { 
            cancelJob(j);
        }
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {

        Job result = new JobImplementation(description, scheduler, "localjob-" + getNextJobID());

        LocalJobExecutor executor = new LocalJobExecutor(result, octopusEngine);

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

    @Override
    public Job[] submitJobs(Scheduler scheduler, JobDescription... descriptions) throws OctopusException {

        Job[] result = new Job[descriptions.length];

        for (int i = 0; i < descriptions.length; i++) {
            result[i] = submitJob(scheduler, descriptions[i]);
        }

        return result;
    }

}