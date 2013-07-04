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

package nl.esciencecenter.octopus.engine.files;

import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.CopyStatus;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class CopyStatusImplementation implements CopyStatus {

    private final Copy copy;
    private final String state;
    private final Exception exception;

    private final boolean isRunning;
    private final boolean isDone;

    private final long bytesToCopy;
    private final long bytesCopied;

    public CopyStatusImplementation(Copy copy, String state, boolean isRunning, boolean isDone, long bytesToCopy,
            long bytesCopied, Exception exception) {
        super();
        this.copy = copy;
        this.isRunning = isRunning;
        this.isDone = isDone;
        this.state = state;
        this.bytesToCopy = bytesToCopy;
        this.bytesCopied = bytesCopied;
        this.exception = exception;
    }

    @Override
    public Copy getCopy() {
        return copy;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean hasException() {
        return exception != null;
    }

    @Override
    public long bytesToCopy() {
        return bytesToCopy;
    }

    @Override
    public long bytesCopied() {
        return bytesCopied;
    }
}
