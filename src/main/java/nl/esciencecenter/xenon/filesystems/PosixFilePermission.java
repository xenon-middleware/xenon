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
package nl.esciencecenter.xenon.filesystems;

import java.util.HashSet;
import java.util.Set;

/**
 * PosixFilePermission is an enumeration containing all supported Posix file permissions.
 *
 * @version 1.0
 * @since 1.0
 */
public enum PosixFilePermission {

    /**
     * Execute/search permission, group.
     */
    GROUP_EXECUTE,
    /**
     * Read permission, group.
     */
    GROUP_READ,
    /**
     * Write permission, group.
     */
    GROUP_WRITE,
    /**
     * Execute/search permission, others.
     */
    OTHERS_EXECUTE,
    /**
     * Read permission, others.
     */
    OTHERS_READ,
    /**
     * Write permission, others.
     */
    OTHERS_WRITE,
    /**
     * Execute/search permission, owner.
     */
    OWNER_EXECUTE,
    /**
     * Read permission, owner.
     */
    OWNER_READ,
    /**
     * Write permission, owner.
     */
    OWNER_WRITE;

    private static int OTHERS_EXECUTE_BIT = 0x1;
    private static int OTHERS_WRITE_BIT = 0x2;
    private static int OTHERS_READ_BIT = 0x4;

    private static int GROUP_EXECUTE_BIT = 0x1 << 3;
    private static int GROUP_WRITE_BIT = 0x2 << 3;
    private static int GROUP_READ_BIT = 0x4 << 3;

    private static int OWNER_EXECUTE_BIT = 0x1 << 6;
    private static int OWNER_WRITE_BIT = 0x2 << 6;
    private static int OWNER_READ_BIT = 0x4 << 6;

    /**
     * Check if a sequence of <code>PosixFilePermission</code>s contains a specific option.
     *
     * If <code>option</code> or <code>options</code> is <code>null</code>, <code>false</code> will be returned.
     *
     * @param toFind
     *            the option to find.
     * @param options
     *            the options to check.
     *
     * @return if <code>options</code> contains <code>option</code>.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static boolean contains(PosixFilePermission toFind, PosixFilePermission... options) {

        if (toFind == null || options == null || options.length == 0) {
            return false;
        }

        for (PosixFilePermission curr : options) {
            if (curr == toFind) {
                return true;
            }
        }
        return false;
    }

    public static String convertToOctal(Set<PosixFilePermission> permissions) {

        int result = 0;

        if (permissions == null) {
            throw new IllegalArgumentException("Permissions may not be null");
        }

        if (permissions.contains(OTHERS_EXECUTE)) {
            result |= OTHERS_EXECUTE_BIT;
        }

        if (permissions.contains(OTHERS_WRITE)) {
            result |= OTHERS_WRITE_BIT;
        }

        if (permissions.contains(OTHERS_READ)) {
            result |= OTHERS_READ_BIT;
        }

        if (permissions.contains(GROUP_EXECUTE)) {
            result |= GROUP_EXECUTE_BIT;
        }

        if (permissions.contains(GROUP_WRITE)) {
            result |= GROUP_WRITE_BIT;
        }

        if (permissions.contains(GROUP_READ)) {
            result |= GROUP_READ_BIT;
        }

        if (permissions.contains(OWNER_EXECUTE)) {
            result |= OWNER_EXECUTE_BIT;
        }

        if (permissions.contains(OWNER_WRITE)) {
            result |= OWNER_WRITE_BIT;
        }

        if (permissions.contains(OWNER_READ)) {
            result |= OWNER_READ_BIT;
        }

        return Integer.toOctalString(result);
    }

    public static Set<PosixFilePermission> convertFromOctal(String octal) {

        if (octal == null || octal.length() != 4) {
            throw new IllegalArgumentException("Expected 4 digit octal file mode string");
        }

        Set<PosixFilePermission> result = new HashSet<>();

        int mode = Integer.decode("0" + octal);

        if ((mode & OTHERS_EXECUTE_BIT) != 0) {
            result.add(OTHERS_EXECUTE);
        }

        if ((mode & OTHERS_WRITE_BIT) != 0) {
            result.add(OTHERS_WRITE);
        }

        if ((mode & OTHERS_READ_BIT) != 0) {
            result.add(OTHERS_READ);
        }

        if ((mode & GROUP_EXECUTE_BIT) != 0) {
            result.add(GROUP_EXECUTE);
        }

        if ((mode & GROUP_WRITE_BIT) != 0) {
            result.add(GROUP_WRITE);
        }

        if ((mode & GROUP_READ_BIT) != 0) {
            result.add(GROUP_READ);
        }

        if ((mode & OWNER_EXECUTE_BIT) != 0) {
            result.add(OWNER_EXECUTE);
        }

        if ((mode & OWNER_WRITE_BIT) != 0) {
            result.add(OWNER_WRITE);
        }

        if ((mode & OWNER_READ_BIT) != 0) {
            result.add(OWNER_READ);
        }

        return result;
    }
}
