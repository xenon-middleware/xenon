package nl.esciencecenter.octopus.adaptors.local;


public class LocalJobsTest {

    //@Rule
   // public TemporaryFolder folder = new TemporaryFolder();

//    @Test
//    public void submitJob_WithoutStdin_NoException() throws OctopusException {
//        LocalAdaptor adaptor = mock(LocalAdaptor.class);
//        OctopusEngine octopus = mock(OctopusEngine.class);
//        Scheduler scheduler = mock(Scheduler.class);
//
//        String[][] defaults = new String[][] { { LocalAdaptor.MULTIQ_MAX_CONCURRENT, "1"}, { LocalAdaptor.MAX_HISTORY, "10"}, 
//                {LocalAdaptor.POLLING_DELAY, "5000"}};
//        
//        OctopusProperties props = new OctopusProperties(defaults);
//        LocalJobs lj = new LocalJobs(props, adaptor, octopus);
//
//        JobDescription description = new JobDescription();
//        description.setExecutable("/bin/sleep");
//        description.setArguments("30");
//        String workdir = folder.getRoot().getPath();
//        description.setWorkingDirectory(workdir);
//        description.setStderr("stderr.txt");
//        description.setStdout("stdout.txt");
//        description.setQueueName("multi");
//
//        Job job = lj.submitJob(scheduler, description);
//
//        String id = job.getUUID().toString();
//        assertFalse("Job has UUID", id.isEmpty());
//        assertEquals(scheduler, job.getScheduler());
//        assertTrue("Identifier starts with localjob-", job.getIdentifier().startsWith("localjob-"));
//
//        lj.end();
//    }


}
