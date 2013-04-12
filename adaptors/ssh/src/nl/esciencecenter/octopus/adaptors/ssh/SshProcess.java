package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.MergingOutputStream;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class SshProcess {
    private SshAdaptor adaptor;
    private SchedulerImplementation scheduler;
    private int processesPerNode;
    private String executable;
    private List<String> arguments;
    private Map<String, String> environment;
    private String workingDirectory;
    private String stdin;
    private String stdout;
    private String stderr;

    public SshProcess(SshAdaptor adaptor, SchedulerImplementation scheduler, int processesPerNode, String executable, List<String> arguments, Map<String, String> environment,
            String workingDirectory, String stdin, String stdout, String stderr) {
        super();
        this.adaptor = adaptor;
        this.scheduler = scheduler;
        this.processesPerNode = processesPerNode;
        this.executable = executable;
        this.arguments = arguments;
        this.environment = environment;
        this.workingDirectory = workingDirectory;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    void run() throws OctopusException {
        
/*        
        builder.environment().putAll(environment);
        builder.directory(new java.io.File(workingDirectory));

        // buffered streams, will also synchronize
        stdoutStream = new MergingOutputStream(new FileOutputStream(workingDirectory + File.separator + stdout));
        stderrStream = new MergingOutputStream(new FileOutputStream(workingDirectory + File.separator + stderr));

        processes = new Process[count];
        stdinForwarders = new StreamForwarder[count];
        stdoutForwarders = new StreamForwarder[count];
        stderrForwarders = new StreamForwarder[count];
        
        for (int i = 0; i < count; i++) {
            processes[i] = builder.start();
            
            if (stdin == null) { 
                stdinForwarders[i] = null;
                processes[i].getOutputStream().close();
            } else { 
                stdinForwarders[i] = new StreamForwarder(new FileInputStream(workingDirectory + File.separator + stdin), 
                        processes[i].getOutputStream());
            }
        
            stdoutForwarders[i] = new StreamForwarder(processes[i].getInputStream(), stdoutStream);
            stderrForwarders[i] = new StreamForwarder(processes[i].getErrorStream(), stderrStream);
        }
        */
    }

    int waitFor() throws InterruptedException {
        return -1;
    }

    void destroy() {
    }
}
