/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.octopus.adaptors.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nl.esciencecenter.octopus.engine.util.ProcessWrapper;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;

/**
 * LocalBatchProcess implements a {@link ProcessWrapper} for local batch processes. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalBatchProcess implements ProcessWrapper {

    private final LocalInteractiveProcess process;
    
    private StreamForwarder stdinForwarder;
    private StreamForwarder stdoutForwarder;
    private StreamForwarder stderrForwarder;

    LocalBatchProcess(LocalInteractiveProcess process, JobDescription description) throws IOException { 

        this.process = process;
        
        String workingDirectory = description.getWorkingDirectory();
        
        String stdout = description.getStdout();
        String stderr = description.getStderr();
        String stdin = description.getStdin();
        
        if (workingDirectory == null) { 
            workingDirectory = System.getProperty("user.dir");
        }
        
        if (!stdout.startsWith(File.separator)) { 
            stdout = workingDirectory + File.separator + stdout;
        }
        
        if (!stderr.startsWith(File.separator)) { 
            stderr = workingDirectory + File.separator + stderr;
        }

        if (stdin != null && !stdin.startsWith(File.separator)) { 
            stdin = workingDirectory + File.separator + stdin;
        }
        
        Streams s = process.getStreams();
        
        stdoutForwarder = new StreamForwarder(s.getStdout(), new FileOutputStream(stdout));
        stderrForwarder = new StreamForwarder(s.getStderr(), new FileOutputStream(stderr));
        
        if (stdin == null) { 
            stdinForwarder = null;
            s.getStdin().close();
        } else { 
            stdinForwarder = new StreamForwarder(new FileInputStream(stdin), s.getStdin());
        }
    }

    private synchronized void closeStreams() { 
        
        if (stdinForwarder != null) { 
            stdinForwarder.close();
            stdinForwarder = null;
        }
        
        if (stdoutForwarder != null) { 
            stdoutForwarder.close();
            stdoutForwarder = null;
        }
        
        if (stderrForwarder != null) { 
            stderrForwarder.close();
            stderrForwarder = null;
        }
    }
    
    public boolean isDone() {
        
        if (process.isDone()) {
            closeStreams();
            return true;
        }
        
        return false;
    }
   
    public int getExitStatus() {
        return process.getExitStatus();
    }
    
    public void destroy() {
        process.destroy();
        closeStreams();
    }

    @Override
    public Streams getStreams() {
        return null;
    }
}
