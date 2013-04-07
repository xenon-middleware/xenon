package nl.esciencecenter.octopus.adaptors.local;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStateListener;
import nl.esciencecenter.octopus.jobs.Scheduler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalScheduler implements Scheduler {

    protected static Logger logger = LoggerFactory.getLogger(LocalScheduler.class);
    
    private final OctopusEngine engine;

    private final LinkedList<LocalJob> singleQ;

    private final LinkedList<LocalJob> multiQ;

    private final LinkedList<LocalJob> unlimitedQ;

    private final ExecutorService singleExecutor;

    private final ExecutorService multiExecutor;

    private final ExecutorService unlimitedExecutor;
    
    private final int maxQSize;

    public LocalScheduler(ImmutableTypedProperties properties, OctopusEngine engine) throws OctopusException {
        this.engine = engine;
        singleQ = new LinkedList<LocalJob>();
        multiQ = new LinkedList<LocalJob>();
        unlimitedQ = new LinkedList<LocalJob>();

        unlimitedExecutor = Executors.newCachedThreadPool();
        singleExecutor = Executors.newSingleThreadExecutor();

        int processors = Runtime.getRuntime().availableProcessors();
        int multiQThreads = properties.getIntProperty(LocalAdaptor.LOCAL_MULTIQ_MAX_JOBS, processors);
        multiExecutor = Executors.newFixedThreadPool(multiQThreads);
        
        maxQSize = properties.getIntProperty(LocalAdaptor.LOCAL_Q_HISTORY_SIZE, LocalAdaptor.DEFAULT_LOCAL_Q_HISTORY_SIZE);
        
        if (maxQSize < 0 && maxQSize != -1) {
            throw new BadParameterException("max q size cannot be negative (excluding -1 for unlimited)", "local", null);
        }
    }

    public String[] getQueueNames() throws OctopusException {
        return new String[] { "single", "multi", "unlimited" };
    }

    @Override
    public synchronized Job submitJob(JobDescription description, JobStateListener listener) throws OctopusException {
        LocalJob result = new LocalJob(description, listener, engine);

        String queueName = description.getQueueName();

        if (queueName == null || queueName.equals("single")) {
            singleQ.add(result);
            singleExecutor.execute(result);
        } else if (queueName.equals("multi")) {
            multiQ.add(result);
            multiExecutor.execute(result);
        } else if (queueName.equals("unlimited")) {
            unlimitedQ.add(result);
            unlimitedExecutor.execute(result);
        } else {
            throw new BadParameterException("queue \"" + queueName + "\" does not exist", "local", null);
        }

        //purge jobs from q if needed (will not actually cancel execution of jobs)
        purgeQ(singleQ);
        purgeQ(multiQ);
        purgeQ(unlimitedQ);
        
        return result;
    }

    //remove finished jobs from a q until the maximum number of jobs limit is met.
    private synchronized void purgeQ(LinkedList<LocalJob> q) {
        if (maxQSize == -1) {
            return;
        }
        
        //how many jobs do we need to remove
        int purgeCount = q.size() - maxQSize; 
        
        if (purgeCount <= 0) {
            return;
        }
        
        Iterator<LocalJob> iterator = q.iterator();
        
        while(iterator.hasNext() && purgeCount > 0) {
            if (iterator.next().isDone()) {
                iterator.remove();
                purgeCount--;
            }
        }
    }

    @Override
    public synchronized Job submitJob(JobDescription description) throws OctopusException {
        return submitJob(description, null);
    }

    @Override
    public synchronized LocalJob[] getJobs(String queueName) throws OctopusException {
        if (queueName == null || queueName.equals("single")) {
            return singleQ.toArray(new LocalJob[0]);
        } else if (queueName.equals("multi")) {
            return multiQ.toArray(new LocalJob[0]);
        } else if (queueName.equals("unlimited")) {
            return unlimitedQ.toArray(new LocalJob[0]);
        } else {
            throw new BadParameterException("queue \"" + queueName + "\" does not exist", "local", null);
        }
    }

    void end() {
        singleExecutor.shutdownNow();
        multiExecutor.shutdownNow();
        unlimitedExecutor.shutdownNow();
    }

}
