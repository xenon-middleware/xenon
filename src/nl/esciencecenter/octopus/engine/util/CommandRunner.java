package nl.esciencecenter.octopus.engine.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import nl.esciencecenter.octopus.exceptions.CommandNotFoundException;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a command. Constructor waits for command to finish.
 * 
 * @author Niels Drost
 * 
 */
public class CommandRunner {

    protected static Logger logger = LoggerFactory.getLogger(CommandRunner.class);

    private final int exitCode;

    private final OutputReader out;

    private final OutputReader err;

    public CommandRunner(String... command) throws OctopusException {
        this(null, command);
    }

    // determine location of exe file using path, will return given location if
    // not found in path
    private String getExeFile(String exe) {
        String path = System.getenv("PATH");

        if (path != null) {
            for (String pathElement : path.split(File.pathSeparator)) {
                if (!pathElement.isEmpty()) {
                    String candidateLocation = pathElement + File.separator + exe;
                    File file = new File(candidateLocation);
                    if (file.canExecute()) {
                        return candidateLocation;
                    }
                } else {
                    // special case empty path element
                    File f = new File(exe);
                    if (f.canExecute()) {
                        return exe;
                    }
                }
            }
        }
        return exe;
    }

    public CommandRunner(File workingDir, String... command) throws CommandNotFoundException {
        if (command.length == 0) {
            throw new ArrayIndexOutOfBoundsException("runCommand: command array has length 0");
        }

        // expand command using path
        command[0] = getExeFile(command[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("CommandRunner running: " + Arrays.toString(command));
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDir != null) {
            builder.directory(workingDir);
        }
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            throw new CommandNotFoundException(getClass().getName(), "CommandRunner cannot run command " + Arrays.toString(command), e);
        }

        // close stdin.
        try {
            p.getOutputStream().close();
        } catch (Throwable e) {
            // ignore
        }

        // we must always read the output and error streams to avoid deadlocks
        out = new OutputReader(p.getInputStream());
        err = new OutputReader(p.getErrorStream());

        int exitCode = 0;
        try {
            exitCode = p.waitFor();

            out.waitUntilFinished();
            err.waitUntilFinished();

            if (logger.isDebugEnabled()) {
                logger.debug("CommandRunner out: " + out.getResult() + "\n" + "CommandRunner err: " + err.getResult());
            }

        } catch (InterruptedException e) {
            // IGNORE
        }
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return out.getResult();
    }

    public String getStderr() {
        return err.getResult();
    }

    public int getExitCode() {
        return exitCode;
    }

}
