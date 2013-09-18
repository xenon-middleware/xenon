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

package nl.esciencecenter.cobalt.adaptors.ssh;

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.cobalt.files.PosixFilePermission;

import com.jcraft.jsch.SftpATTRS;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 */
public class SshUtil {

    /** read by owner */
    static final int READ_OWNER = 00400;

    /** write by owner */
    static final int WRITE_OWNER = 00200;

    /** execute/search by owner */
    static final int EXEC_OWNER = 00100;

    /** read by group */
    static final int READ_GROUP = 00040;

    /** write by group */
    static final int WRITE_GROUP = 00020;

    /** execute/search by group */
    static final int EXEC_GROUP = 00010;

    /** read by others */
    static final int READ_OTHERS = 00004;

    /** write by others */
    static final int WRITE_OTHERS = 00002;

    /** execute/search by others */
    static final int EXEC_OTHERS = 00001;

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

    static Set<PosixFilePermission> bitsToPermissions(int bit) {

        HashSet<PosixFilePermission> result = new HashSet<PosixFilePermission>();

        if ((bit & READ_OWNER) != 0) {
            result.add(PosixFilePermission.OWNER_READ);
        }
        if ((bit & WRITE_OWNER) != 0) {
            result.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((bit & EXEC_OWNER) != 0) {
            result.add(PosixFilePermission.OWNER_EXECUTE);
        }

        if ((bit & READ_GROUP) != 0) {
            result.add(PosixFilePermission.GROUP_READ);
        }
        if ((bit & WRITE_GROUP) != 0) {
            result.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((bit & EXEC_GROUP) != 0) {
            result.add(PosixFilePermission.GROUP_EXECUTE);
        }

        if ((bit & READ_OTHERS) != 0) {
            result.add(PosixFilePermission.OTHERS_READ);
        }
        if ((bit & WRITE_OTHERS) != 0) {
            result.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((bit & EXEC_OTHERS) != 0) {
            result.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        return result;
    }

    static int permissionsToBits(Set<PosixFilePermission> permissions) {

        int bits = 0;

        for (PosixFilePermission p : permissions) {

            switch (p) {
            case OWNER_READ:
                bits |= READ_OWNER;
                break;
            case OWNER_WRITE:
                bits |= WRITE_OWNER;
                break;
            case OWNER_EXECUTE:
                bits |= EXEC_OWNER;
                break;
            case GROUP_READ:
                bits |= READ_GROUP;
                break;
            case GROUP_WRITE:
                bits |= WRITE_GROUP;
                break;
            case GROUP_EXECUTE:
                bits |= EXEC_GROUP;
                break;
            case OTHERS_READ:
                bits |= READ_OTHERS;
                break;
            case OTHERS_WRITE:
                bits |= WRITE_OTHERS;
                break;
            case OTHERS_EXECUTE:
                bits |= EXEC_OTHERS;
                break;
            }
        }

        return bits;
    }

    static boolean isExecutable(int permissions) {
        return (permissions & EXEC_OWNER) != 0;
    }

    static boolean isReadable(int permissions) {
        return (permissions & READ_OWNER) != 0;
    }

    static boolean isWritable(int permissions) {
        return (permissions & WRITE_OWNER) != 0;
    }

}
