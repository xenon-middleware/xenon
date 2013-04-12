// TODO document: not possible: set working dir
// TODO document: not supported: private int processesPerNode;

package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshProcess {
    private SshAdaptor adaptor;
    @SuppressWarnings("unused")
    private SchedulerImplementation scheduler;
    private Session session;
    private String executable;
    private List<String> arguments;
    private Map<String, String> environment;
    private String stdin;
    private String stdout;
    private String stderr;

    public SshProcess(SshAdaptor adaptor, SchedulerImplementation scheduler, Session session,
            String executable, List<String> arguments, Map<String, String> environment, String stdin,
            String stdout, String stderr) {
        super();
        this.adaptor = adaptor;
        this.scheduler = scheduler;
        this.executable = executable;
        this.arguments = arguments;
        this.environment = environment;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    void run() throws OctopusException, OctopusIOException {

        ChannelExec channel = adaptor.getExecChannel(session);

        String command = executable;

        for(String s : arguments) {
            command += " " + s;
        }
        
        channel.setCommand(command);

        // TODO make property for X Forwarding
        // channel.setXForwarding(true);

        // set the streams first, then connect the channel.

        if (stdin != null && stdin.length() != 0) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(stdin);
            } catch (FileNotFoundException e) {
                throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
            }
            channel.setInputStream(fis);
        } else {
            channel.setInputStream(null);
        }

        if (stdout != null && stdout.length() != 0) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(stdout);
            } catch (FileNotFoundException e) {
                throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
            }
            channel.setOutputStream(fos);
        } else {
            channel.setOutputStream(null);
        }

        if (stderr != null && stderr.length() != 0) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(stderr);
            } catch (FileNotFoundException e) {
                throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
            }
            channel.setErrStream(fos);
        } else {
            channel.setErrStream(null);
        }
        
        for (Map.Entry<String,String> entry : environment.entrySet()) {
            channel.setEnv(entry.getKey(), entry.getValue());
        }
        
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }
        
        try {
            channel.start();
        } catch (JSchException e) {
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }
    }

    int waitFor() throws InterruptedException {
        return -1;
    }

    void destroy() {
    }
}
