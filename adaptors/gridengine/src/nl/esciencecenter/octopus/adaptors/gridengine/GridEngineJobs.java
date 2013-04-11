package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class GridEngineJobs implements Jobs {

    private final OctopusProperties properties;

    private final OctopusEngine octopusEngine;

    private final Map<Scheduler, SchedulerConnection> connections;

    public GridEngineJobs(OctopusProperties properties, OctopusEngine octopusEngine) {
        this.properties = properties;
        this.octopusEngine = octopusEngine;

        connections = new HashMap<Scheduler, SchedulerConnection>();

    }

    private synchronized SchedulerConnection getConnection(Scheduler scheduler) throws OctopusRuntimeException {
        if (!connections.containsKey(scheduler)) {
            throw new OctopusRuntimeException(GridengineAdaptor.ADAPTOR_NAME, "cannot find scheduler " + scheduler.toString()
                    + " did you close it already?");
        }

        return connections.get(scheduler);
    }

    @Override
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
        SchedulerConnection connection = new SchedulerConnection(location, credential, properties);

        Scheduler result = new SchedulerImplementation(GridengineAdaptor.ADAPTOR_NAME, connection.getUniqueID(), location,
                connection.getQueueNames(), connection.getProperties());
        
        synchronized (this) {
            connections.put(result, connection);
        }

        return result;
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        throw new OctopusRuntimeException(GridengineAdaptor.ADAPTOR_NAME,
                "Error in engine: getLocalScheduler() should not be called on this adaptor");
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(scheduler);
        
        Map<String, Map<String, String>> result = connection.getQueueStatus();
        
        if (!result.containsKey(queueName)) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue from server");
        }
        
        return new QueueStatusImplementation(scheduler, queueName, null, result.get(queueName));
        
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(scheduler);
        
        Map<String, Map<String, String>> status = connection.getQueueStatus();
        
        QueueStatus[] result = new QueueStatus[queueNames.length];
        
        for(int i = 0; i < queueNames.length; i++) {
            if (!status.containsKey(queueNames[i])) {
                throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queueNames[i] + " from server");
            }
            
            result[i] = new QueueStatusImplementation(scheduler, queueNames[i], null, status.get(queueNames[i])); 
        }
        
        return result;
    }
    
    @Override
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        // TODO Auto-generated method stub

    }

    public void end() {
        // TODO Auto-generated method stub

    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        return false;
    }
}
