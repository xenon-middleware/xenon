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

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.xenon.files.PosixFilePermission;

/**
 * PosixFileUtils contains several utility functions related to Posix files. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 *
 */
public class PosixFileUtils {
    
    // FIXME: Are these correct ? Some seem to be missing ?  

    /** read by owner */
    public static final int READ_OWNER = 00400;

    /** write by owner */
    public static final int WRITE_OWNER = 00200;

    /** execute/search by owner */
    public static final int EXEC_OWNER = 00100;

    /** read by group */
    public static final int READ_GROUP = 00040;

    /** write by group */
    public static final int WRITE_GROUP = 00020;

    /** execute/search by group */
    public static final int EXEC_GROUP = 00010;

    /** read by others */
    public static final int READ_OTHERS = 00004;

    /** write by others */
    public static final int WRITE_OTHERS = 00002;

    /** execute/search by others */
    public static final int EXEC_OTHERS = 00001;

    protected PosixFileUtils() {
        // do not use
    }

    @SuppressWarnings("PMD.NPathComplexity")
    public static Set<PosixFilePermission> bitsToPermissions(int bit) {

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

    public static int permissionsToBits(Set<PosixFilePermission> permissions) {

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
            default:
                // The above should catch all options.
                break;
            }
        }

        return bits;
    }

    public static boolean isExecutable(int permissions) {
        return (permissions & EXEC_OWNER) != 0;
    }

    public static boolean isReadable(int permissions) {
        return (permissions & READ_OWNER) != 0;
    }

    public static boolean isWritable(int permissions) {
        return (permissions & WRITE_OWNER) != 0;
    }
}
