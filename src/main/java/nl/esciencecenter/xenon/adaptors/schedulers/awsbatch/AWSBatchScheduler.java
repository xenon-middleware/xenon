package nl.esciencecenter.xenon.adaptors.schedulers.awsbatch;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.ComputeEnvironmentOrder;
import com.amazonaws.services.batch.model.DescribeJobQueuesRequest;
import com.amazonaws.services.batch.model.DescribeJobQueuesResult;
import com.amazonaws.services.batch.model.DescribeJobsRequest;
import com.amazonaws.services.batch.model.DescribeJobsResult;
import com.amazonaws.services.batch.model.JobDetail;
import com.amazonaws.services.batch.model.JobQueueDetail;
import com.amazonaws.services.batch.model.JobSummary;
import com.amazonaws.services.batch.model.ListJobsRequest;
import com.amazonaws.services.batch.model.ListJobsResult;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.SubmitJobResult;
import com.amazonaws.services.batch.model.TerminateJobRequest;
import com.amazonaws.services.batch.model.TerminateJobResult;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.schedulers.JobStatusImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.QueueStatusImplementation;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.Streams;

public class AWSBatchScheduler extends Scheduler {
    private final AWSBatch client;
    private boolean isShutdown = false;

    public AWSBatchScheduler(String uniqueID, String location, Credential credential, Map<String, String> properties) throws InvalidCredentialException {
        super(uniqueID, AWSBatchSchedulerAdaptor.ADAPTOR_NAME, location, new XenonProperties());
        AWSBatchClientBuilder builder = AWSBatchClientBuilder.standard();
        if (credential instanceof PasswordCredential) {
            AWSCredentials awsCredentials = new BasicAWSCredentials(credential.getUsername(), ((PasswordCredential) credential).getPassword().toString());
            AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            builder.withCredentials(credentialsProvider);
        } else if (credential instanceof DefaultCredential) {
            // use client default
        } else {
            throw new InvalidCredentialException(AWSBatchSchedulerAdaptor.ADAPTOR_NAME, "Password of Default credential required");
        }
        if (!location.isEmpty()) {
            builder.withRegion(location);
        }
        client = builder.build();
    }

    @Override
    public String[] getQueueNames() throws XenonException {
        DescribeJobQueuesResult queues = client.describeJobQueues(new DescribeJobQueuesRequest());
        return queues.getJobQueues().stream().map(JobQueueDetail::getJobQueueName).toArray(String[]::new);
    }

    @Override
    public void close() throws XenonException {
        client.shutdown();
        isShutdown = true;
    }

    @Override
    public boolean isOpen() throws XenonException {
        return !isShutdown;
    }

    @Override
    public String getDefaultQueueName() throws XenonException {
        return getQueueNames()[0];
    }

    @Override
    public String[] getJobs(String... queueNames) throws XenonException {
        // TODO fetch jobs for each queue or no queue
        ListJobsRequest request = new ListJobsRequest().withJobQueue(queueNames[0]);
        ListJobsResult result = client.listJobs(request);
        return result.getJobSummaryList().stream().map(JobSummary::getJobId).toArray(String[]::new);
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws XenonException {
        DescribeJobQueuesResult queues = client.describeJobQueues(new DescribeJobQueuesRequest().withJobQueues(queueName));
        JobQueueDetail queue = queues.getJobQueues().get(0);
        return mapQueueStatus(queue);
    }

    private QueueStatus mapQueueStatus(JobQueueDetail queue) {
        Map<String, String> queueInfo = new HashMap<>(6);
        queueInfo.put("state", queue.getState());
        queueInfo.put("status", queue.getStatus());
        queueInfo.put("statusReason", queue.getStatusReason());
        queueInfo.put("priority", queue.getPriority().toString());
        queueInfo.put("computeEnvironments", queue.getComputeEnvironmentOrder().stream().map(ComputeEnvironmentOrder::getComputeEnvironment).collect(Collectors.joining(", ")));
        queueInfo.put("arn", queue.getJobQueueArn());
        return new QueueStatusImplementation(
                this, queue.getJobQueueName(), null, queueInfo
        );
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException {
        DescribeJobQueuesResult queues = client.describeJobQueues(new DescribeJobQueuesRequest().withJobQueues(queueNames));
        return queues.getJobQueues().stream().map(this::mapQueueStatus).toArray(QueueStatus[]::new);
    }

    @Override
    public String submitBatchJob(JobDescription description) throws XenonException {
        // TODO call registerJobDefinition to setup command and environment.
        // Then create SubmitJobRequest with newly created JobDefinition
        if (description.getQueueName() == null) {
            throw new IncompleteJobDescriptionException(getAdaptorName(), "AWS Batch must have queue name");
        }
        SubmitJobRequest submitJobRequest = new SubmitJobRequest()
                .withJobDefinition(description.getExecutable()) // TODO Xenon <> AWS mismatch, must document
                .withJobQueue(description.getQueueName())
                .withParameters(description.getJobOptions()) // TODO Xenon <> AWS mismatch, must document
                ;
        // TODO in request set JobName, DependsOn, ContainerOverrides, RetryStrategy

        SubmitJobResult result = client.submitJob(submitJobRequest);
        return result.getJobId();
    }

    @Override
    public Streams submitInteractiveJob(JobDescription description) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(), "AWS Batch does not support submitInteractiveJob");
    }

    @Override
    public JobStatus getJobStatus(String jobIdentifier) throws XenonException {
        DescribeJobsRequest request = new DescribeJobsRequest().withJobs(jobIdentifier);
        DescribeJobsResult result = client.describeJobs(request);
        if (result.getJobs().isEmpty()) {
            throw new NoSuchJobException(getAdaptorName(), jobIdentifier + " not found");
        }
        JobDetail jobResult = result.getJobs().get(0);
        return mapJobStatus(jobResult);
    }

    private JobStatus mapJobStatus(JobDetail jobResult) {
        Integer exitCode = 0; // TODO Xenon <> AWS mismatch, must document
        com.amazonaws.services.batch.model.JobStatus awsJobStatus = com.amazonaws.services.batch.model.JobStatus.fromValue(jobResult.getStatus());
        boolean running = com.amazonaws.services.batch.model.JobStatus.RUNNING.equals(awsJobStatus);
        boolean done = com.amazonaws.services.batch.model.JobStatus.SUCCEEDED.equals(awsJobStatus) || com.amazonaws.services.batch.model.JobStatus.FAILED.equals(awsJobStatus);
        Map<String, String> info = new HashMap<>();
        info.put("createdAt", jobResult.getCreatedAt().toString());
        info.put("stoppedAt", jobResult.getStartedAt().toString());
        info.put("status", jobResult.getStatus());
        info.put("statusReason", jobResult.getStatusReason());
        info.put("definition", jobResult.getJobDefinition());
        info.put("name", jobResult.getJobName());
        info.put("queue", jobResult.getJobQueue());
        info.put("image", jobResult.getContainer().getImage());
        // TODO add more fields from jobResult to info
        return new JobStatusImplementation(jobResult.getJobId(), jobResult.getStatus(), exitCode, null, running, done, info);
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {
        TerminateJobRequest request = new TerminateJobRequest().withJobId(jobIdentifier).withReason("Xenon cancelJob");
        client.terminateJob(request);
        return getJobStatus(jobIdentifier);
    }

    @Override
    public JobStatus waitUntilDone(String jobIdentifier, long timeout) throws XenonException {
        // TODO implement
        return null;
    }

    @Override
    public JobStatus waitUntilRunning(String jobIdentifier, long timeout) throws XenonException {
        // TODO implement
        return null;
    }

}
