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
package nl.esciencecenter.xenon.adaptors.schedulers;

public class Deadline {

    private Deadline() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Calculate the epoch timestamp when a timeout will expire.
     *
     * This deadline is computed by adding the <code>timeout</code> to <code>System.currentTimeMillis()</code>. This computation
     * is protected against overflow, that is, the deadline will never exceed <code>Long.MAX_VALUE</code>.
     *
     * This allows the user to simply test if the deadline has passed by performing a check against the current epoch time:
     *
     *    <code>if (deadline &lt;= System.currentTimeMillis()) { // deadline has passed }</code>
     *
     * @param timeout
     *          the timeout to compute the deadline with. Must be &gt;= 0 or an IllegalArgumentException will be thrown.
     * @return
     *          the timestamp at which the timeout will expire, or <code>Long.MAX_VALUE</code> if the timeout causes an overflow.
     */
    public static long getDeadline(long timeout) {

        long deadline;

        if (timeout > 0) {

            long time = System.currentTimeMillis();

            deadline = time + timeout;

            if (deadline < time) {
                // Timeout overflow. Partial fix by setting timeout to end of epoch.
                deadline = Long.MAX_VALUE;
            }
        } else if (timeout == 0) {
            deadline = Long.MAX_VALUE;
        } else {
            throw new IllegalArgumentException("Illegal timeout " + timeout);
        }

        return deadline;
    }
}
