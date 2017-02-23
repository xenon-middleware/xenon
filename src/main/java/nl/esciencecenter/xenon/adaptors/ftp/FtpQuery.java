/**
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
package nl.esciencecenter.xenon.adaptors.ftp;

/**
 * Wrapper class for executing a single operation on an FTPClient that returns a result. The wrapper takes care of checking the
 * status after execution and throwing an exception if necessary.
 *
 *
 */
public abstract class FtpQuery<T> extends FtpCommand {
    private T result;

    /*
     * Gets the result of the query execution when called after calling execute.
     *
     */
    public T getResult() {
        return result;
    }

    protected void setResult(T result) {
        this.result = result;
    }
}
