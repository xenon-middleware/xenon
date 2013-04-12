package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobs implements Jobs {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(LocalJobs.class);

    @SuppressWarnings("unused")
    private final OctopusEngine octopusEngine;

    private final LocalAdaptor localAdaptor;
    
    private final Scheduler localScheduler;
    
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
    
    public LocalJobs(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {

        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;
        
        URI uri = null;
        
        try {
            uri = new URI("local:///");
        } catch (URISyntaxException e) {
            throw new OctopusRuntimeException(LocalAdaptor.ADAPTOR_NAME, "Failed to create URI", e);
        }
        
        localScheduler = new SchedulerImplementation(LocalAdaptor.ADAPTOR_NAME, "LocalScheduler", uri, 
                new String[] { "single", "multi", "unlimited" }, null, properties, true, false);

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
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        localAdaptor.checkURI(location);

        String path = location.getPath();

        if (path != null && !path.equals("/")) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with path!");
        }

        if (credential != null) { 
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with credentials!");
        }
        
        if (properties != null && properties.size() > 0) { 
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with additional properties!");
        }
        
        return localScheduler;
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        return localScheduler;
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
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
       
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

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        
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
            throw new BadParameterException(LocalAdaptor.ADAPTOR_NAME, "queue \"" + queueName + "\" does not exist");
        }

        //purge jobs from q if needed (will not actually cancel execution of jobs)
        purgeQ(singleQ);
        purgeQ(multiQ);
        purgeQ(unlimitedQ);

        return result;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {

        if (job.getScheduler() != localScheduler) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot retrieve job status from other scheduler!");
        }
        
        LinkedList<LocalJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
        LocalJobExecutor executor = findJob(tmp, job);
        return executor.getStatus();
    }

    @Override
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
    
    private LinkedList<LocalJobExecutor> findQueue(String queueName) throws OctopusException {

        if (queueName == null || queueName.equals("single")) {
            return singleQ;
        } else if (queueName.equals("multi")) {
            return multiQ;
        } else if (queueName.equals("unlimited")) {
            return unlimitedQ;
        } else {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "queue \"" + queueName + "\" does not exist");
        }
    }

    private LocalJobExecutor findJob(LinkedList<LocalJobExecutor> queue, Job job) throws OctopusException {

        for (LocalJobExecutor e : queue) {
            if (e.getJob().equals(job)) {
                return e;
            }
        }

        throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Job not found: " + job.getIdentifier());
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        
        if (job.getScheduler() != localScheduler) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot cancel jobs descriptions from other scheduler!"); 
        }

        // FIXME: What if job is already gone?
        LinkedList<LocalJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
        findJob(tmp, job).kill();
    }
    
    public void end() {
        singleExecutor.shutdownNow();
        multiExecutor.shutdownNow();
        unlimitedExecutor.shutdownNow();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        if (queueName == null || queueName.equals("single")) {
            return new QueueStatusImplementation(scheduler, queueName, null, null);
        } else if (queueName.equals("multi")) {
            return new QueueStatusImplementation(scheduler, queueName, null, null);
        } else if (queueName.equals("unlimited")) {
            return new QueueStatusImplementation(scheduler, queueName, null, null);
        } else {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "No such queue: " + queueName);
        }
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        
        QueueStatus[] result = new QueueStatus[queueNames.length];
        
        for (int i=0;i<queueNames.length;i++) {
            try { 
                result[i] = getQueueStatus(scheduler, queueNames[i]);
            } catch (OctopusException e) { 
                result[i] = new QueueStatusImplementation(null, queueNames[i], e, null);
            }
        }
        
        return result;
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Local Scheduler cannot be closed!");
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return true;
    }    
}