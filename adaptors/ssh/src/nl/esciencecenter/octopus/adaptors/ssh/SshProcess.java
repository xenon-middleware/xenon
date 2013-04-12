package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.List;
import java.util.Map;

public class SshProcess {
    private int processesPerNode;
    private String executable;
    private List<String> arguments;
    private Map<String, String> environment;
    private String workingDirectory;
    private String stdin;
    private String stdout;
    private String stderr;

    public SshProcess(int processesPerNode, String executable, List<String> arguments, Map<String, String> environment,
            String workingDirectory, String stdin, String stdout, String stderr) {
        super();
        this.processesPerNode = processesPerNode;
        this.executable = executable;
        this.arguments = arguments;
        this.environment = environment;
        this.workingDirectory = workingDirectory;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    int waitFor() throws InterruptedException {
        return -1;
    }

    void destroy() {
    }
}
