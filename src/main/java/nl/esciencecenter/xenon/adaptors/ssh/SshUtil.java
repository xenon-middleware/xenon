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

package nl.esciencecenter.xenon.adaptors.ssh;

import com.jcraft.jsch.SftpATTRS;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 */
public class SshUtil {

    protected SshUtil() {
        // do not use
    }

    static boolean equals(SftpATTRS a1, SftpATTRS a2) {

        // Handles case of aliased object + both null
        if (a1 == a2) {
            return true;
        }

        // Handle case of one of the two null
        if (a1 == null || a2 == null) {
            return false;
        }

        // Test all fields
        if (a1.getATime() != a2.getATime()) {
            return false;
        }

        if (a1.getMTime() != a2.getMTime()) {
            return false;
        }

        if (a1.getFlags() != a2.getFlags()) {
            return false;
        }

        if (a1.getGId() != a2.getGId()) {
            return false;
        }

        if (a1.getUId() != a2.getUId()) {
            return false;
        }

        if (a1.getPermissions() != a2.getPermissions()) {
            return false;
        }

        if (a1.getSize() != a2.getSize()) {
            return false;
        }

        return true;
    }
}
