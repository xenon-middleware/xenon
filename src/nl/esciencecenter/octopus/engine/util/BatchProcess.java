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
package nl.esciencecenter.octopus.engine.util;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;

/**
 * BatchProcess wraps an {@link InteractiveProcess} to emulate a batch process. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class BatchProcess implements InteractiveProcess {

    private final InteractiveProcess process;
    
    private StreamForwarder stdinForwarder;
    private StreamForwarder stdoutForwarder;
    private StreamForwarder stderrForwarder;

    private AbsolutePath processPath(Files files, AbsolutePath root, String path) throws OctopusIOException, OctopusException { 
        
        AbsolutePath result = null;
        
        if (path == null) { 
            result = root;
        } else if (path.startsWith("/")) {  
            result = files.newPath(root.getFileSystem(), new RelativePath(path));
        } else { 
            result = root.resolve(new RelativePath(path));
        }

        return result;
    }
    
    
    public BatchProcess(Files files, FileSystem filesystem, JobImplementation job, InteractiveProcessFactory factory) throws Exception { 
        
        JobDescription description = job.getJobDescription();
        
        // Retrieve the filesystem that goes with the scheduler.
        AbsolutePath workdir = processPath(files, filesystem.getEntryPath(), description.getWorkingDirectory());
        
        if (!files.exists(workdir)) { 
            files.createDirectories(workdir);
        }
        
        AbsolutePath stdout = processPath(files, workdir, description.getStdout());
        AbsolutePath stderr = processPath(files, workdir, description.getStderr());
        
        // Create the files for stdout and stderr. Will fail if the files already exist!
        files.createFile(stdout);
        files.createFile(stderr);
        
        // If needed create a file for stdin, and make sure it exists!
        AbsolutePath stdin = null;
        
        if (description.getStdin() != null) {
            stdin = processPath(files, workdir, description.getStdin());
            
            if (!files.exists(stdin)) { 
                throw new IOException("Stdin cannot be redirected from " + stdin.getPath() + " (file does not exist!)");
            }
        }
        
        process = factory.createInteractiveProcess(job);
        Streams streams = process.getStreams();
        
        stdoutForwarder = new StreamForwarder(streams.getStdout(), 
                files.newOutputStream(stdout, OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.TRUNCATE));
        
        stderrForwarder = new StreamForwarder(streams.getStderr(),
                files.newOutputStream(stderr, OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.TRUNCATE));
        
        if (stdin == null) { 
            stdinForwarder = null;
            streams.getStdin().close();
        } else { 
            stdinForwarder = new StreamForwarder(files.newInputStream(stdin), streams.getStdin()); 
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
