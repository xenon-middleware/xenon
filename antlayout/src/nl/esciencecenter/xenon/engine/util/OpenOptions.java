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

package nl.esciencecenter.xenon.engine.util;

import nl.esciencecenter.xenon.files.InvalidOpenOptionsException;
import nl.esciencecenter.xenon.files.OpenOption;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class OpenOptions {

    private OpenOption openMode;
    private OpenOption appendMode;
    private OpenOption readMode;
    private OpenOption writeMode;

    public OpenOption getOpenMode() {
        return openMode;
    }

    public void setOpenMode(String adaptorName, OpenOption openMode) throws InvalidOpenOptionsException {

        if (this.openMode != null && openMode != this.openMode) {
            throw new InvalidOpenOptionsException(adaptorName, "Conflicting open options: " + openMode + " and " + this.openMode);
        }

        this.openMode = openMode;
    }

    public OpenOption getAppendMode() {
        return appendMode;
    }

    public void setAppendMode(String adaptorName, OpenOption appendMode) throws InvalidOpenOptionsException {

        if (this.appendMode != null && appendMode != this.appendMode) {
            throw new InvalidOpenOptionsException(adaptorName, "Conflicting append options: " + appendMode + " and "
                    + this.appendMode);
        }

        this.appendMode = appendMode;
    }

    public OpenOption getReadMode() {
        return readMode;
    }

    public void setReadMode(OpenOption readMode) {
        this.readMode = readMode;
    }

    public OpenOption getWriteMode() {
        return writeMode;
    }

    public void setWriteMode(OpenOption writeMode) {
        this.writeMode = writeMode;
    }

    public static OpenOptions processOptions(String adaptorName, OpenOption... options) throws InvalidOpenOptionsException {

        if (options == null || options.length == 0) {
            throw new InvalidOpenOptionsException(adaptorName, "Missing open options!");
        }

        OpenOptions result = new OpenOptions();

        for (OpenOption opt : options) {
            switch (opt) {
            case CREATE:
            case OPEN:
            case OPEN_OR_CREATE:
                result.setOpenMode(adaptorName, opt);
                break;

            case APPEND:
            case TRUNCATE:
                result.setAppendMode(adaptorName, opt);
                break;

            case WRITE:
                result.setWriteMode(opt);
                break;
            case READ:
                result.setReadMode(opt);
                break;
            }
        }

        if (result.getOpenMode() == null) {
            throw new InvalidOpenOptionsException(adaptorName, "No open mode provided!");
        }

        return result;
    }
}
